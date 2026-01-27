package com.gtnewhorizons.postea.utility;

import static com.gtnewhorizons.postea.utility.TransformerRegistry.getBlockReplacement;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import com.gtnewhorizons.postea.api.TriFunction;
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
        chunkMixin.Postea$setPosteaCode(-1);
        if (tag.hasKey("POSTEA", 4)) {
            chunkMixin.Postea$setPosteaCode(tag.getLong("POSTEA"));
        }
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

    public static void onChunkLoaded(Chunk chunk) {
        if (chunk instanceof IChunkMixin iChunkMixin) {
            if (!hasChunkBeenUpdated(iChunkMixin.Postea$getPosteaCode())) {
                iChunkMixin.Postea$setPosteaCode(ChunkFixerUtility.POSTEA_UPDATE_CODE);
                chunk.setChunkModified();
            }
        }
    }

    public static void onChunkWrite(Chunk chunk, NBTTagCompound tag) {
        if (chunk instanceof IChunkMixin iChunkMixin) {
            tag.setLong("POSTEA", iChunkMixin.Postea$getPosteaCode());
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
                        (byte) metadata,
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

                if (blockInfo.tileTransformer == null) {
                    tileEntities.removeTag(i--);
                } else {
                    ((IMixinNBTTagList) tileEntities)
                        .Postea$replaceCompoundTagAt(i, blockInfo.tileTransformer.apply(tileEntity));
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
