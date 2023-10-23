package com.colen.postea.API;

import net.minecraft.block.Block;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

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
}
