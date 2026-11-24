package com.gtnewhorizons.postea.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import org.junit.jupiter.api.Test;

class HeightMapsTest {

    /// A chunk with empty sections at `sections`, opaque only at positions set through [#setOpaque], with no block
    /// registry behind it.
    private static final class OpacityChunk extends Chunk {

        private final Set<Long> opaque = new HashSet<>();

        OpacityChunk(int... sections) {
            super(null, 0, 0);
            for (int section : sections) getBlockStorageArray()[section] = new ExtendedBlockStorage(section << 4, true);
        }

        void setOpaque(int x, int y, int z) {
            opaque.add(position(x, y, z));
        }

        @Override
        public int func_150808_b(int x, int y, int z) {
            return opaque.contains(position(x, y, z)) ? 255 : 0;
        }

        private static long position(int x, int y, int z) {
            return ((long) y << 8) | (z << 4) | x;
        }
    }

    @Test
    void anEmptiedColumnGetsHeightZero() {
        OpacityChunk chunk = new OpacityChunk(0);
        chunk.setOpaque(0, 10, 0);
        chunk.heightMap[5 << 4 | 3] = 64;

        HeightMaps.regenerate(chunk);

        assertEquals(0, chunk.heightMap[5 << 4 | 3]);
        assertEquals(11, chunk.heightMap[0]);
        assertEquals(0, chunk.heightMapMinimum);
    }

    @Test
    void aBlockAtTheTopOfTheWorldGetsHeight256() {
        OpacityChunk chunk = new OpacityChunk(15);
        chunk.setOpaque(7, 255, 9);

        HeightMaps.regenerate(chunk);

        assertEquals(256, chunk.heightMap[9 << 4 | 7]);
    }
}
