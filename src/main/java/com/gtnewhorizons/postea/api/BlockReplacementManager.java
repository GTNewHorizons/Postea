package com.gtnewhorizons.postea.api;

import java.util.HashMap;
import java.util.function.BiFunction;

import net.minecraft.world.World;

import com.google.common.collect.LinkedListMultimap;
import com.gtnewhorizons.postea.PosteaMissingMappingHandler;
import com.gtnewhorizons.postea.utility.BlockConversionInfo;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

public class BlockReplacementManager {

    private static final HashMap<String, String> dummyMap = new HashMap<>();
    public static final LinkedListMultimap<String, BiFunction<BlockConversionInfo, World, BlockConversionInfo>> blockReplacementMap = LinkedListMultimap
        .create();
    public static final IntOpenHashSet posteaMarkedIDs = new IntOpenHashSet();

    @SuppressWarnings("unused")
    public static void addBlockReplacement(String blockNameIn,
        BiFunction<BlockConversionInfo, World, BlockConversionInfo> transformer) {
        String newName = PosteaMissingMappingHandler.createDummyBlockIfNeeded(blockNameIn);
        if (!newName.equals(blockNameIn)) {
            dummyMap.put(newName, blockNameIn);
        }
        blockReplacementMap.put(newName, transformer);
    }

    public static BlockConversionInfo getBlockReplacement(BlockConversionInfo blockConversionInfo, World world) {
        // auto change the value in case a handler check it, we can't do anything about the block id
        // but hopefully that's never going to be an issue
        String originalName = blockConversionInfo.blockName;
        blockConversionInfo.blockName = dummyMap
            .getOrDefault(blockConversionInfo.blockName, blockConversionInfo.blockName);
        for (BiFunction<BlockConversionInfo, World, BlockConversionInfo> transformer : blockReplacementMap
            .get(originalName)) {
            BlockConversionInfo result = transformer.apply(blockConversionInfo, world);
            if (result != null) return result;
        }
        return null;
    }

    // We need this to save reprocessing blocks.
    public static boolean blockNotConvertible(int blockID) {
        return !posteaMarkedIDs.contains(blockID);
    }
}
