package com.colen.postea.API;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ChunkFixerUtility {

    public static void processChunkNBT(NBTTagCompound compound) {
        replaceTileEntitiesWithNormalBlock(compound);
    }

    /**
     * Replaces specified tile entities with a block in the chunk NBT data.
     *
     * @param chunkNBT           The chunk's NBT data.
     */
    public static void replaceTileEntitiesWithNormalBlock(NBTTagCompound chunkNBT) {
        NBTTagCompound level = chunkNBT.getCompoundTag("Level");
        List<ConversionInfo> conversionInfoList = removeTileEntities(level.getTagList("TileEntities", 10));

        int chunkXPos = level.getInteger("xPos") * 16;  // Assuming each chunk is 16 blocks along x-axis
        int chunkZPos = level.getInteger("zPos") * 16;  // Assuming each chunk is 16 blocks along z-axis

        NBTTagList sections = level.getTagList("Sections", 10);
        for (int i = 0; i < sections.tagCount(); i++) {
            NBTTagCompound section = sections.getCompoundTagAt(i);
            processSection(section, conversionInfoList, chunkXPos, chunkZPos);
        }
    }

    private static void processSection(NBTTagCompound section, List<ConversionInfo> conversionInfoList, int chunkXPos, int chunkZPos) {
        byte[] blockArray = section.getByteArray("Blocks16");
        byte[] metadataArray = section.getByteArray("Data");
        byte y = section.getByte("Y");

        List<ConversionInfo> filteredList = filterConversionInfosByY(conversionInfoList, y);

        for (ConversionInfo info : filteredList) {
            int[] localCoords = convertToChunkLocalCoordinates(info, y, chunkXPos, chunkZPos);
            int index = computeBlockIndex(localCoords);
            setBlockInfo(info.blockInfo.block, index, blockArray);
            setMetadata(info.blockInfo.metadata, index, metadataArray);
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
        return new int[]{tileX, tileY, tileZ};
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
        int metadataIndex = index / 2;
        byte currentMetadataByte = metadataArray[metadataIndex];

        if ((index & 1) == 0) { // Even
            currentMetadataByte &= 0xF0;
            currentMetadataByte |= (metadata & 0x0F);
        } else { // Odd
            currentMetadataByte &= 0x0F;
            currentMetadataByte |= ((metadata << 4) & 0xF0);
        }

        metadataArray[metadataIndex] = currentMetadataByte;
    }

    private static List<ConversionInfo> removeTileEntities(NBTTagList tileEntities) {
        List<ConversionInfo> conversionInfo = new ArrayList<>();
        List<Integer> indicesToRemove = new ArrayList<>();  // List to store indices of tileEntities to be removed

        for (int i = 0; i < tileEntities.tagCount(); i++) {
            NBTTagCompound tileEntity = tileEntities.getCompoundTagAt(i);
            String tileEntityId = tileEntity.getString("id");

            // Check if we have a transformer registered for this tile entity ID
            Function<NBTTagCompound, BlockInfo> transformationFunction = TileEntityReplacementManager.getTileEntityToNormalBlockTransformerFunction(tileEntityId);

            if (transformationFunction != null) {
                int x = tileEntity.getInteger("x");
                int y = tileEntity.getInteger("y");
                int z = tileEntity.getInteger("z");

                BlockInfo blockInfo = transformationFunction.apply(tileEntity);

                conversionInfo.add(new ConversionInfo(x, y, z, blockInfo));
                indicesToRemove.add(i);  // Add the index to the list for removal
            }
        }

        // Remove the tile entities in reverse order to avoid index shifts
        Collections.reverse(indicesToRemove);
        for (int index : indicesToRemove) {
            tileEntities.removeTag(index);
        }

        return conversionInfo;
    }


    private static void fixTileEntities(NBTTagCompound compound) {
        NBTTagList tileEntities = compound.getCompoundTag("Level").getTagList("TileEntities", 10);
        for (int i = 0; i < tileEntities.tagCount(); i++) {
            NBTTagCompound tileEntity = tileEntities.getCompoundTagAt(i);
            String tileEntityStringName = tileEntity.getString("id");
            Function<NBTTagCompound, NBTTagCompound> tileFixer = TileEntityReplacementManager.getTileEntityFixer(tileEntityStringName);

            if (tileFixer != null) {
                tileFixer.apply(tileEntity);
            }
        }
    }

    public static class ConversionInfo {

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

    public static class BlockInfo {
        public final Block block;
        public final int metadata;

        public BlockInfo(Block block, int metadata) {
            this.block = block;
            this.metadata = metadata;
        }
    }

}
