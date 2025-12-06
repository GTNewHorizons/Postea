package com.gtnewhorizons.postea.utility;

import static com.gtnewhorizons.postea.api.BlockReplacementManager.blockNotConvertible;
import static com.gtnewhorizons.postea.utility.PosteaUtilities.getModListHash;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import com.gtnewhorizons.postea.api.BlockReplacementManager;
import com.gtnewhorizons.postea.api.TileEntityReplacementManager;
import com.gtnewhorizons.postea.api.TriFunction;
import com.gtnewhorizons.postea.compat.Compat;
import com.gtnewhorizons.postea.compat.SubChunkAccess;

import akka.japi.Pair;
import cpw.mods.fml.common.registry.GameRegistry;

public class ChunkFixerUtility {

    // This will not change between runs, unless a mod is updated or added.
    public static final int POSTEA_UPDATE_CODE = getModListHash();

    private static final int AIR_ID = 0;
    private static final HashMap<Block, String> loadedBlocks = new HashMap<>();

    public static void transformNormalBlocks(Chunk chunk, ExtendedBlockStorage ebs, World world) {

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
                    // If this block has no registered Postea conversion, skip it.
                    if (blockNotConvertible(blockId)) continue;

                    // Cache block names to improve performance, as findUniqueIdentifierFor is expensive.
                    Block block = Block.getBlockById(blockId);
                    String blockName = loadedBlocks.computeIfAbsent(
                        block,
                        b -> GameRegistry.findUniqueIdentifierFor(b)
                            .toString());

                    BlockConversionInfo blockConversionInfo = new BlockConversionInfo();
                    blockConversionInfo.blockName = blockName;
                    blockConversionInfo.blockID = blockId;
                    blockConversionInfo.metadata = (byte) metadata; // Updated

                    blockConversionInfo.world = world;

                    blockConversionInfo.x = x + chunkXPos + 1;
                    blockConversionInfo.y = y + sectionY;
                    blockConversionInfo.z = z + chunkZPos + 1;

                    BlockConversionInfo output = BlockReplacementManager
                        .getBlockReplacement(blockConversionInfo, world);

                    if (output != null) {
                        access.setBlockId(x, y, z, output.blockID);
                        access.setMeta(x, y, z, output.metadata);
                    }
                }
            }
        }
    }

    public static void transformTileEntities(NBTTagCompound levelCompoundTag, Chunk chunk, World world) {

        Pair<List<ConversionInfo>, NBTTagList> output = adjustTileEntities(
            levelCompoundTag.getTagList("TileEntities", 10),
            world,
            chunk);
        List<ConversionInfo> conversionInfoList = output.first();
        NBTTagList tileEntities = output.second();

        if (tileEntities.tagCount() > 0) {
            levelCompoundTag.setTag("TileEntities", tileEntities);
        }

        for (ExtendedBlockStorage ebs : chunk.getBlockStorageArray()) {
            if (ebs == null) continue;
            processSection(ebs, conversionInfoList);
        }
    }

    private static void processSection(ExtendedBlockStorage ebs, List<ConversionInfo> conversionInfoList) {
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
    }

    private static Pair<List<ConversionInfo>, NBTTagList> adjustTileEntities(NBTTagList tileEntities, World world,
        Chunk chunk) {
        List<ConversionInfo> conversionInfo = new ArrayList<>();

        NBTTagList tileEntitiesCopy = new NBTTagList();

        for (int i = 0; i < tileEntities.tagCount(); i++) {
            NBTTagCompound tileEntity = tileEntities.getCompoundTagAt(i);
            String tileEntityId = tileEntity.getString("id");

            // Check if we have a transformer registered for this tile entity ID
            TriFunction<NBTTagCompound, World, Chunk, BlockInfo> transformationFunction = TileEntityReplacementManager
                .getTileEntityToNormalBlockTransformerFunction(tileEntityId);

            if (transformationFunction != null) {
                int x = tileEntity.getInteger("x");
                int y = tileEntity.getInteger("y");
                int z = tileEntity.getInteger("z");

                BlockInfo blockInfo = transformationFunction.apply(tileEntity, world, chunk);
                if (blockInfo == null) {
                    // Do nothing.
                    tileEntitiesCopy.appendTag(tileEntity);
                    continue;
                }

                if (blockInfo.tileTransformer != null) {
                    tileEntitiesCopy.appendTag(blockInfo.tileTransformer.apply(tileEntity));
                } // Otherwise they are removed, therefore not appended.

                conversionInfo.add(new ConversionInfo(x, y, z, blockInfo));
            } else {
                // Do nothing.
                tileEntitiesCopy.appendTag(tileEntity);
            }
        }

        return new Pair<>(conversionInfo, tileEntitiesCopy);
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
