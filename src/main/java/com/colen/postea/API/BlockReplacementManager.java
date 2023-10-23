package com.colen.postea.API;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

import java.util.HashMap;
import java.util.Map;

import static com.colen.postea.API.ItemStackReplacementManager.addItemReplacement;

public class BlockReplacementManager {

    private static final Map<BlockKey, BlockKey> blockReplacementMap = new HashMap<>();

    @SuppressWarnings("unused")
    public static void addBlockReplacement(Block blockIn, int metadataIn, Block blockOut, int metadataOut) {
        blockReplacementMap.put(new BlockKey(blockIn, metadataIn), new BlockKey(blockOut, metadataOut));
        addItemReplacement(Item.getItemFromBlock(blockIn), metadataIn, Item.getItemFromBlock(blockOut), metadataOut);
    }

    @SuppressWarnings("unused")
    public static BlockKey getBlockReplacement(Block blockIn, int metadataIn) {
        return blockReplacementMap.getOrDefault(new BlockKey(blockIn, metadataIn), null);
    }
}
