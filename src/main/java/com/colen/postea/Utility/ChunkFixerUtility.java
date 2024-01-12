package com.colen.postea.Utility;

import static com.colen.postea.Utility.PosteaUtilities.getModListHash;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;

import com.colen.postea.API.BlockReplacementManager;
import com.colen.postea.API.TileEntityReplacementManager;

import akka.japi.Pair;
import cpw.mods.fml.common.registry.GameRegistry;

public class ChunkFixerUtility {

    public static void processChunkNBT(NBTTagCompound compound, World world) {
        NBTTagCompound levelCompoundTag = compound.getCompoundTag("Level");

        if (processChunk(levelCompoundTag)) {
            transformTileEntities(levelCompoundTag, world);
            transformNormalBlocks(levelCompoundTag, world);
            levelCompoundTag.setInteger("POSTEA", POSTEA_UPDATE_CODE);
        }
    }

    // This will not change between runs, unless a mod is updated or added.
    public static final int POSTEA_UPDATE_CODE = getModListHash();

    // If a chunk has the same postea code, then do not update it. Return false.
    private static boolean processChunk(NBTTagCompound compound) {
        // I'm minimising the string length here since this
        // will be on every single chunk in every world... Rather not duplicate too much.
        if (compound.hasKey("POSTEA")) {
            return compound.getInteger("POSTEA") != POSTEA_UPDATE_CODE;
        }
        return true;
    }

    private static void transformNormalBlocks(NBTTagCompound levelCompoundTag, World world) {
        NBTTagList sections = levelCompoundTag.getTagList("Sections", 10);

        int chunkXPos = levelCompoundTag.getInteger("xPos") * 16;
        int chunkZPos = levelCompoundTag.getInteger("zPos") * 16;

        for (int i = 0; i < sections.tagCount(); i++) {
            NBTTagCompound section = sections.getCompoundTagAt(i);

            // Blocks16 and Data16 exist only because of NEID.
            byte[] blockArray = section.getByteArray("Blocks16");
            byte[] metadataArray = section.getByteArray("Data16");

            byte sectionY = section.getByte("Y");

            for (int index = 0; index < blockArray.length / 2; index++) {
                int blockId = ((blockArray[index * 2] & 0xFF) << 8) | (blockArray[index * 2 + 1] & 0xFF);
                int metadata = ((metadataArray[index * 2] & 0xFF) << 8) | (metadataArray[index * 2 + 1] & 0xFF);

                Block block = Block.getBlockById(blockId);
                String blockName = GameRegistry.findUniqueIdentifierFor(block)
                    .toString();

                BlockConversionInfo blockConversionInfo = new BlockConversionInfo();
                blockConversionInfo.blockName = blockName;
                blockConversionInfo.blockID = blockId;
                blockConversionInfo.metadata = (byte) metadata; // Updated
                blockConversionInfo.world = world;

                int x = index % 16;
                int y = (index / 256) + (sectionY * 16);
                int z = (index / 16) % 16;

                blockConversionInfo.x = x + chunkXPos + 1;
                blockConversionInfo.y = y;
                blockConversionInfo.z = z + chunkZPos + 1;

                BlockConversionInfo output = BlockReplacementManager.getBlockReplacement(blockConversionInfo, world);

                if (output != null) {
                    int newBlockId = output.blockID;
                    int newMetadata = output.metadata;

                    blockArray[index * 2] = (byte) ((newBlockId >> 8) & 0xFF);
                    blockArray[index * 2 + 1] = (byte) (newBlockId & 0xFF);

                    metadataArray[index * 2] = (byte) ((newMetadata >> 8) & 0xFF);
                    metadataArray[index * 2 + 1] = (byte) (newMetadata & 0xFF);
                }
            }
        }
    }

    private static void transformTileEntities(NBTTagCompound levelCompoundTag, World world) {

        Pair<List<ConversionInfo>, NBTTagList> output = adjustTileEntities(
            levelCompoundTag.getTagList("TileEntities", 10),
            world);
        List<ConversionInfo> conversionInfoList = output.first();
        NBTTagList tileEntities = output.second();

        if (tileEntities.tagCount() > 0) {
            levelCompoundTag.setTag("TileEntities", tileEntities);
        }

        int chunkXPos = levelCompoundTag.getInteger("xPos") * 16; // Assuming each chunk is 16 blocks along x-axis
        int chunkZPos = levelCompoundTag.getInteger("zPos") * 16; // Assuming each chunk is 16 blocks along z-axis

        NBTTagList sections = levelCompoundTag.getTagList("Sections", 10);
        for (int i = 0; i < sections.tagCount(); i++) {
            NBTTagCompound section = sections.getCompoundTagAt(i);
            processSection(section, conversionInfoList, chunkXPos, chunkZPos);
        }
    }

    private static void processSection(NBTTagCompound section, List<ConversionInfo> conversionInfoList, int chunkXPos,
        int chunkZPos) {
        byte[] blockArray = section.getByteArray("Blocks16");
        byte[] metadataArray = section.getByteArray("Data16"); // Updated to use Data16
        byte y = section.getByte("Y");

        List<ConversionInfo> filteredList = filterConversionInfosByY(conversionInfoList, y);

        for (ConversionInfo info : filteredList) {
            int[] localCoords = convertToChunkLocalCoordinates(info, y, chunkXPos, chunkZPos);
            int index = computeBlockIndex(localCoords);
            setBlockInfo(info.blockInfo.block, index, blockArray);
            setMetadata(info.blockInfo.metadata, index, metadataArray); // This uses the updated setMetadata
        }
    }

    private static List<ConversionInfo> filterConversionInfosByY(List<ConversionInfo> conversionInfoList, byte y) {
        return conversionInfoList.stream()
            .filter(info -> info.y >= y * 16 && info.y < (y + 1) * 16)
            .collect(Collectors.toList());
    }

    private static int[] convertToChunkLocalCoordinates(ConversionInfo info, byte y, int chunkXPos, int chunkZPos) {
        int tileX = info.x - chunkXPos;
        int tileY = info.y - y * 16;
        int tileZ = info.z - chunkZPos;
        return new int[] { tileX, tileY, tileZ };
    }

    private static int computeBlockIndex(int[] localCoords) {
        return localCoords[1] * 256 + localCoords[2] * 16 + localCoords[0];
    }

    private static void setBlockInfo(Block block, int index, byte[] blockArray) {
        int blockID = Block.getIdFromBlock(block);
        blockArray[index * 2] = (byte) ((blockID >> 8) & 0xFF);
        blockArray[index * 2 + 1] = (byte) (blockID & 0xFF);
    }

    private static void setMetadata(int metadata, int index, byte[] metadataArray) {
        // Since each metadata is now two bytes, multiply the index by 2 to get the correct position.
        int metadataStartIndex = index * 2;

        // Split the 16-bit metadata into two bytes and store them in the array.
        metadataArray[metadataStartIndex] = (byte) ((metadata >> 8) & 0xFF); // Higher 8 bits
        metadataArray[metadataStartIndex + 1] = (byte) (metadata & 0xFF); // Lower 8 bits
    }

    private static Pair<List<ConversionInfo>, NBTTagList> adjustTileEntities(NBTTagList tileEntities, World world) {
        List<ConversionInfo> conversionInfo = new ArrayList<>();

        NBTTagList tileEntitiesCopy = new NBTTagList();

        for (int i = 0; i < tileEntities.tagCount(); i++) {
            NBTTagCompound tileEntity = tileEntities.getCompoundTagAt(i);
            String tileEntityId = tileEntity.getString("id");

            // Check if we have a transformer registered for this tile entity ID
            BiFunction<NBTTagCompound, World, BlockInfo> transformationFunction = TileEntityReplacementManager
                .getTileEntityToNormalBlockTransformerFunction(tileEntityId);

            if (transformationFunction != null) {
                int x = tileEntity.getInteger("x");
                int y = tileEntity.getInteger("y");
                int z = tileEntity.getInteger("z");

                BlockInfo blockInfo = transformationFunction.apply(tileEntity, world);

                if (blockInfo.tileTransformer != null) {
                    tileEntitiesCopy.appendTag(blockInfo.tileTransformer.apply(tileEntity));
                } // Otherwise they are removed, therefore not appended.

                conversionInfo.add(new ConversionInfo(x, y, z, blockInfo));
            } else {
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
