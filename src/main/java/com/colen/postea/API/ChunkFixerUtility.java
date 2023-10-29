package com.colen.postea.API;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ChunkFixerUtility {
    public static Chunk fixChunk(Chunk chunk) {
        // Iterate over each ExtendedBlockStorage. Kinda ugly but does the job...
        for (ExtendedBlockStorage storage : chunk.getBlockStorageArray()) {
            if (storage != null) { // Always check if storage isn't null before using it
                for (int x = 0; x < 16; x++) {
                    for (int y = 0; y < 16; y++) { // This is not a mistake, we do this on purpose because of how minecraft stores stuff.
                        for (int z = 0; z < 16; z++) {

                            // Get the current block and metadata at the position
                            Block currentBlock = storage.getBlockByExtId(x, y, z);
                            int currentMeta = storage.getExtBlockMetadata(x, y, z);

                            // Check if there's a replacement for this block+metadata combination
                            BlockKey replacement = BlockReplacementManager.getBlockReplacement(currentBlock, currentMeta);

                            // If there's a replacement, set the new block and metadata
                            if (replacement != null) {
                                storage.func_150818_a(x, y, z, replacement.getBlock());
                                storage.setExtBlockMetadata(x, y, z, replacement.getDamage());
                            }
                        }
                    }
                }
            }
        }
        return chunk;
    }

    public static void processCode(NBTTagCompound compound) {
        System.out.println(compound.toString());

        //fixTileEntities(compound);
        replaceTileEntitiesWithNormalBlock(compound);
        //setAllBlocksInNBTToID7(compound);
    }

    public static void setAllBlocksInNBTToID7(NBTTagCompound compound) {

        int blockID = 469;  // The block ID to replace with

        // Ensure that the "Level" tag compound exists
        if (compound.hasKey("Level", 10)) {
            NBTTagCompound levelTag = compound.getCompoundTag("Level");

            // Ensure that the "Sections" tag list exists
            if (levelTag.hasKey("Sections", 9)) {
                NBTTagList sectionsList = levelTag.getTagList("Sections", 10);  // 10 is the NBT compound ID

                for (int i = 0; i < sectionsList.tagCount(); i++) {
                    NBTTagCompound section = sectionsList.getCompoundTagAt(i);

                    if (section.hasKey("Blocks16", 7)) {
                        byte[] blockArray = section.getByteArray("Blocks16");

                        // Extract high and low bytes from the blockID
                        byte highByte = (byte) ((blockID >> 8) & 0xFF);  // Get the most significant 8 bits
                        byte lowByte = (byte) (blockID & 0xFF);          // Get the least significant 8 bits

                        // Set all entries to the specified block ID
                        for (int j = 0; j < blockArray.length; j += 2) {
                            blockArray[j] = highByte;
                            blockArray[j + 1] = lowByte;
                        }

                        // Replace the old array with the new one
                        section.setTag("Blocks16", new NBTTagByteArray(blockArray));
                    }

                }
            }
        }
    }

    /**
     * Replaces specified tile entities with a block in the chunk NBT data.
     *
     * @param chunkNBT           The chunk's NBT data.
     */
    public static void replaceTileEntitiesWithNormalBlock(NBTTagCompound chunkNBT) {

        final NBTTagCompound level = chunkNBT.getCompoundTag("Level");

        final List<ConversionInfo> conversionInfoList = removeTileEntities(level.getTagList("TileEntities", 10));
        NBTTagList sections = level.getTagList("Sections", 10);

        List<ChunkBlockData> blockDataList = new ArrayList<>();

        int chunkXPos = level.getInteger("xPos") * 16;  // Assuming each chunk is 16 blocks along x-axis
        int chunkZPos = level.getInteger("zPos") * 16;  // Assuming each chunk is 16 blocks along z-axis

        for (int i = 0; i < sections.tagCount(); i++) {
            NBTTagCompound section = sections.getCompoundTagAt(i);
            byte[] blockArray = section.getByteArray("Blocks16");
            byte[] metadataArray = section.getByteArray("Data");
            byte y = section.getByte("Y");

            List<ConversionInfo> filteredList = conversionInfoList.stream()
                .filter(info -> info.y >= y * 16 && info.y < (y + 1) * 16)
                .collect(Collectors.toList());

            for (ConversionInfo info : filteredList) {
                int tileX = info.x - chunkXPos;  // Convert global x-coordinate to local x-coordinate within the chunk
                int tileY = info.y - y * 16;     // Convert global y-coordinate to local y-coordinate within the section
                int tileZ = info.z - chunkZPos;  // Convert global z-coordinate to local z-coordinate within the chunk

                int index = tileY * 256 + tileZ * 16 + tileX;

                // Set block ID from ConversionInfo
                int blockID = Block.getIdFromBlock(info.blockInfo.block);
                blockArray[index * 2] = (byte) ((blockID >> 8) & 0xFF);
                blockArray[index * 2 + 1] = (byte) (blockID & 0xFF);

                // Set metadata from ConversionInfo
                int metadataIndex = index / 2;
                byte currentMetadataByte = metadataArray[metadataIndex];

                if ((index & 1) == 0) { // Even
                    currentMetadataByte &= 0xF0;
                    currentMetadataByte |= (info.blockInfo.metadata & 0x0F);
                } else { // Odd
                    currentMetadataByte &= 0x0F;
                    currentMetadataByte |= ((info.blockInfo.metadata << 4) & 0xF0);
                }

                metadataArray[metadataIndex] = currentMetadataByte;
            }

        }

    }



    private static List<ConversionInfo> removeTileEntities(NBTTagList tileEntities) {

        List<ConversionInfo> conversionInfo = new ArrayList<>();
        String targetTileEntityId = "GT_TileEntity_Ores";

        // Loop through each tile entity to check for matches
        for (int i = 0; i < tileEntities.tagCount(); i++) {
            NBTTagCompound tileEntity = tileEntities.getCompoundTagAt(i);
            String tileEntityId = tileEntity.getString("id");

            // If the tile entity matches the target
            if (tileEntityId.equals(targetTileEntityId)) {
                int x = tileEntity.getInteger("x");
                int y = tileEntity.getInteger("y");
                int z = tileEntity.getInteger("z");

                Function<NBTTagCompound, BlockInfo> transformationFunction = TileEntityReplacementManager.getTileEntityToNormalBlockTransformerFunction(tileEntityId);

                BlockInfo blockInfo = transformationFunction.apply(tileEntity);

                conversionInfo.add(new ConversionInfo(x, y, z, blockInfo));

                tileEntities.removeTag(i);
            }
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
        final BlockInfo blockInfo;

        public ConversionInfo(int x, int y, int z, BlockInfo blockInfo) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.blockInfo = blockInfo;
        }
    }

    public static class BlockInfo {
        final Block block;
        final int metadata;

        public BlockInfo(Block block, int metadata) {
            this.block = block;
            this.metadata = metadata;
        }
    }

    public static class ChunkBlockData {
        private int blockID;      // Block ID (2 bytes or 16 bits)
        private byte metadata;    // Metadata (4 bits)

        public ChunkBlockData(int blockID, byte metadata) {
            this.blockID = blockID;
            this.metadata = metadata;
        }

        public int getBlockID() {
            return blockID;
        }

        public void setBlockID(int blockID) {
            this.blockID = blockID;
        }

        public byte getMetadata() {
            return metadata;
        }

        public void setMetadata(byte metadata) {
            this.metadata = metadata;
        }
    }
}
