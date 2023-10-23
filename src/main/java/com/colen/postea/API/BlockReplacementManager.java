package com.colen.postea.API;

import net.minecraft.block.Block;

import java.util.HashMap;
import java.util.Map;

public class BlockReplacementManager {

    private static final Map<BlockKey, BlockKey> replacementMap = new HashMap<>();

    @SuppressWarnings("unused")
    public static void addBlockReplacement(Block blockIn, int metadataIn, Block blockOut, int metadataOut) {
        replacementMap.put(new BlockKey(blockIn, metadataIn), new BlockKey(blockOut, metadataOut));
    }

    @SuppressWarnings("unused")
    public static BlockKey getBlockReplacement(Block blockIn, int metadataIn) {
        return replacementMap.getOrDefault(new BlockKey(blockIn, metadataIn), null);
    }
}
