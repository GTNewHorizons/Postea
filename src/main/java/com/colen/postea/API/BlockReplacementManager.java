package com.colen.postea.API;

import akka.japi.Pair;
import net.minecraft.block.Block;
import net.minecraft.item.Item;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static com.colen.postea.API.ItemStackReplacementManager.addItemReplacement;

public class BlockReplacementManager {

    private static final Map<BlockKey, Function<BlockKey, Pair<Integer, Integer>>> blockReplacementMap = new HashMap<>();

    @SuppressWarnings("unused")
    public static void addBlockReplacement(String blockNameIn, int metadataIn, Function<BlockKey, Pair<Integer, Integer>> transformer) {
        blockReplacementMap.put(new BlockKey(blockNameIn, metadataIn), transformer);
    }

    @SuppressWarnings("unused")
    public static Pair<Integer, Integer> getBlockReplacement(int blockID, int metadataIn) {
        Block block = Block.getBlockById(blockID);
        String blockName = block.getUnlocalizedName();

        Function<BlockKey, Pair<Integer, Integer>> transformer = blockReplacementMap.getOrDefault(new BlockKey(blockName, metadataIn), null);

        if (transformer == null) {
            return null;
        } else {
            return transformer.apply(new BlockKey(blockName, metadataIn));
        }
    }

}
