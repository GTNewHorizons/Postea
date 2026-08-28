package com.gtnewhorizons.postea.utility;

import static com.gtnewhorizons.postea.utility.TransformerRegistry.getBlockReplacement;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ReportedException;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import com.gtnewhorizons.postea.api.ChunkTransformContext;
import com.gtnewhorizons.postea.api.IVersionedTransformer;
import com.gtnewhorizons.postea.api.TriFunction;
import com.gtnewhorizons.postea.api.VersionedReplacementManager;
import com.gtnewhorizons.postea.compat.Compat;
import com.gtnewhorizons.postea.compat.SubChunkAccess;
import com.gtnewhorizons.postea.mixins.interfaces.IChunkMixin;
import com.gtnewhorizons.postea.mixins.interfaces.IMixinNBTTagList;

public class ChunkFixerUtility {

    // This will not change between runs, unless a mod is updated or added.
    public static final int POSTEA_UPDATE_CODE = PosteaUtilities.getModListHash();

    public static boolean hasChunkBeenUpdated(long chunkHash) {
        // We are in a dev env.
        if (POSTEA_UPDATE_CODE == -1) return false;
        return chunkHash == POSTEA_UPDATE_CODE;
    }

    private static final int AIR_ID = 0;

    public static void onChunkRead(Chunk chunk, World world, NBTTagCompound tag) {
        IChunkMixin chunkMixin = (IChunkMixin) chunk;
        runVersionedTransformers(chunk, world, tag);
        chunkMixin.Postea$setPosteaCode(tag.hasKey("POSTEA", 4) ? tag.getLong("POSTEA") : -1);
        // This checks if the chunk has been run with the current POSTEA_UPDATE_CODE and skips it if so.
        if (hasChunkBeenUpdated(chunkMixin.Postea$getPosteaCode())) return;
        List<ConversionInfo> conversionInfoList = adjustTileEntities(tag.getTagList("TileEntities", 10), world, chunk);

        for (ExtendedBlockStorage ebs : chunk.getBlockStorageArray()) {
            if (ebs == null) continue;
            int sectionY = ebs.getYLocation();

            List<ConversionInfo> filteredList = conversionInfoList.stream()
                .filter(info -> info.y >= sectionY && info.y < (sectionY + 16))
                .collect(Collectors.toList());

            for (ConversionInfo info : filteredList) {
                int localX = info.x & 15;
                int localY = info.y & 15;
                int localZ = info.z & 15;
                ebs.func_150818_a(localX, localY, localZ, info.blockInfo.block);
                ebs.setExtBlockMetadata(localX, localY, localZ, info.blockInfo.metadata);
            }

            transformNormalBlocksInSubChunk(chunk, ebs, world);
        }
        chunkMixin.Postea$setPosteaCode(ChunkFixerUtility.POSTEA_UPDATE_CODE);
    }

    private static void runVersionedTransformers(Chunk chunk, World world, NBTTagCompound tag) {
        IChunkMixin chunkMixin = (IChunkMixin) chunk;
        NBTTagCompound stamps = VersionStamps.read(tag);
        chunkMixin.Postea$setVersionStamps(stamps == null ? null : (NBTTagCompound) stamps.copy());
        boolean blockIdsChanged = false;
        for (IVersionedTransformer transformer : VersionedReplacementManager.transformers()) {
            int current = transformer.currentVersion();
            int stored = VersionStamps.stored(stamps, transformer.key());
            if (stored == current) continue;
            ChunkTransformContext ctx = new ChunkTransformContext(chunk, world, tag, stored, current);
            VersionedTransformerLog.announce(transformer, stored, current);
            try {
                transformer.transformChunk(ctx);
            } catch (Throwable t) {
                throw new ReportedException(
                    describe(
                        t,
                        transformer,
                        stored,
                        current,
                        "Chunk",
                        chunk.xPosition + "," + chunk.zPosition + " in dimension " + world.provider.dimensionId));
            }
            chunkMixin.Postea$setVersionedDirty(true);
            blockIdsChanged |= ctx.blockIdsChanged();
        }
        if (!blockIdsChanged) return;
        // A section's block count is what decides whether it is saved at all, and writing through SubChunkAccess
        // bypasses the bookkeeping ExtendedBlockStorage does in its own setters.
        for (ExtendedBlockStorage section : chunk.getBlockStorageArray()) {
            if (section != null) section.removeInvalidBlocks();
        }
        regenerateHeightMap(chunk);
        chunk.isLightPopulated = false;
    }

    // Chunk.generateHeightMap is client-only and generateSkylightMap reaches into the world, so the height map is
    // rebuilt here from the chunk's own storage, which is safe on the chunk I/O thread.
    private static void regenerateHeightMap(Chunk chunk) {
        int top = chunk.getTopFilledSegment() + 16 - 1;
        chunk.heightMapMinimum = Integer.MAX_VALUE;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                chunk.precipitationHeightMap[x + (z << 4)] = -999;
                int y = top;
                while (y > 0 && chunk.func_150808_b(x, y - 1, z) == 0) {
                    y--;
                }
                if (y > 0) {
                    chunk.heightMap[z << 4 | x] = y;
                    chunk.heightMapMinimum = Math.min(chunk.heightMapMinimum, y);
                }
            }
        }
    }

    static CrashReport describe(Throwable cause, IVersionedTransformer transformer, int stored, int current,
        String subjectName, String subject) {
        CrashReport report = CrashReport.makeCrashReport(cause, "Running versioned transformer " + transformer.key());
        CrashReportCategory category = report.makeCategory("Postea versioned transformer");
        category.addCrashSection("Key", transformer.key());
        category.addCrashSection("Stored version", stored);
        category.addCrashSection("Current version", current);
        category.addCrashSection(subjectName, subject);
        return report;
    }

    public static void onChunkLoaded(Chunk chunk) {
        if (chunk instanceof IChunkMixin iChunkMixin) {
            boolean stale = !hasChunkBeenUpdated(iChunkMixin.Postea$getPosteaCode());
            if (stale) iChunkMixin.Postea$setPosteaCode(ChunkFixerUtility.POSTEA_UPDATE_CODE);
            if (stale || iChunkMixin.Postea$isVersionedDirty()) chunk.setChunkModified();
        }
    }

    public static void onChunkWrite(Chunk chunk, NBTTagCompound tag) {
        if (chunk instanceof IChunkMixin iChunkMixin) {
            tag.setLong("POSTEA", iChunkMixin.Postea$getPosteaCode());
            VersionStamps.write(tag, iChunkMixin.Postea$getVersionStamps(), VersionedReplacementManager.transformers());
            iChunkMixin.Postea$setVersionedDirty(false);
        }
    }

    public static void transformNormalBlocksInSubChunk(Chunk chunk, ExtendedBlockStorage ebs, World world) {

        int chunkXPos = chunk.xPosition * 16;
        int chunkZPos = chunk.zPosition * 16;

        int sectionY = ebs.getYLocation();

        SubChunkAccess access = Compat.getSubChunkAccess(ebs);

        for (int y = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++) {
                for (int x = 0; x < 16; x++) {
                    int blockId = access.getBlockId(x, y, z);
                    int metadata = access.getMeta(x, y, z);

                    // Skip air.
                    if (blockId == AIR_ID) continue;

                    BlockConversionInfo output = getBlockReplacement(
                        blockId,
                        metadata,
                        world,
                        x + chunkXPos + 1,
                        y + sectionY,
                        z + chunkZPos + 1);

                    if (output != null) {
                        access.setBlockId(x, y, z, output.blockID);
                        access.setMeta(x, y, z, output.metadata);
                    }
                }
            }
        }
    }

    private static List<ConversionInfo> adjustTileEntities(NBTTagList tileEntities, World world, Chunk chunk) {
        List<ConversionInfo> conversionInfo = new ArrayList<>();

        for (int i = 0; i < tileEntities.tagCount(); i++) {
            NBTTagCompound tileEntity = tileEntities.getCompoundTagAt(i);
            String tileEntityId = tileEntity.getString("id");

            // Check if we have a transformer registered for this tile entity ID
            for (TriFunction<NBTTagCompound, World, Chunk, BlockInfo> transformationFunction : TransformerRegistry
                .getTileEntityToNormalBlockTransformerFunction(tileEntityId)) {

                int x = tileEntity.getInteger("x");
                int y = tileEntity.getInteger("y");
                int z = tileEntity.getInteger("z");

                BlockInfo blockInfo = transformationFunction.apply(tileEntity, world, chunk);
                if (blockInfo == null) continue;

                NBTTagCompound newTag;
                if (blockInfo.tileTransformer != null
                    && (newTag = blockInfo.tileTransformer.apply(tileEntity)) != null) {
                    ((IMixinNBTTagList) tileEntities).Postea$replaceCompoundTagAt(i, newTag);
                } else {
                    tileEntities.removeTag(i--);
                }

                conversionInfo.add(new ConversionInfo(x, y, z, blockInfo));
            }
        }

        return conversionInfo;
    }

    private static class ConversionInfo {

        public final int x;
        public final int y;
        public final int z;
        public final BlockInfo blockInfo;

        public ConversionInfo(int x, int y, int z, BlockInfo blockInfo) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.blockInfo = blockInfo;
        }
    }
}
