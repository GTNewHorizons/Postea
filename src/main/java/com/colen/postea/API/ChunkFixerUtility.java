package com.colen.postea.API;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

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

        fixTileEntities(compound);
        replaceTileEntitiesWithDumbBlock(compound, "GT_TileEntity_Ores", (byte) 7, (byte) 0);
    }

    /**
     * Replaces specified tile entities with a block in the chunk NBT data.
     *
     * @param chunkNBT           The chunk's NBT data.
     * @param targetTileEntityId The ID of the tile entity to be replaced.
     * @param newBlockId         The ID of the block to replace the tile entity with.
     * @param newBlockMetadata   The metadata of the new block.
     */
    public static void replaceTileEntitiesWithDumbBlock(NBTTagCompound chunkNBT, String targetTileEntityId, byte newBlockId, byte newBlockMetadata) {

        List<ConversionInfo> conversionInfoList = new ArrayList<>();

        NBTTagCompound level = chunkNBT.getCompoundTag("Level");
        NBTTagList tileEntities = level.getTagList("TileEntities", 10);
        NBTTagList sections = level.getTagList("Sections", 10);

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

                conversionInfoList.add(new ConversionInfo(x, y, z, blockInfo));

                tileEntities.removeTag(i);
            }
        }
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
}
