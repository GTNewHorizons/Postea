package com.gtnewhorizons.postea.utility;

import net.minecraft.world.chunk.Chunk;

/// Rebuilds a chunk's height map from the chunk's own storage, which is safe on the chunk I/O thread.
/// `Chunk.generateHeightMap` is client-only and `generateSkylightMap` reaches into the world.
final class HeightMaps {

    private HeightMaps() {}

    static void regenerate(Chunk chunk) {
        int top = chunk.getTopFilledSegment() + 16;
        chunk.heightMapMinimum = Integer.MAX_VALUE;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                chunk.precipitationHeightMap[x + (z << 4)] = -999;
                int y = top;
                while (y > 0 && chunk.func_150808_b(x, y - 1, z) == 0) {
                    y--;
                }
                chunk.heightMap[z << 4 | x] = y;
                chunk.heightMapMinimum = Math.min(chunk.heightMapMinimum, y);
            }
        }
    }
}
