package com.gtnewhorizons.postea.api;

import java.util.function.BiFunction;

import net.minecraft.world.World;

import com.google.common.collect.LinkedListMultimap;
import com.gtnewhorizons.postea.utility.BlockConversionInfo;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

public class BlockReplacementManager {

    public static final LinkedListMultimap<String, BiFunction<BlockConversionInfo, World, BlockConversionInfo>> blockReplacementMap = LinkedListMultimap
        .create();
    public static final IntOpenHashSet posteaMarkedIDs = new IntOpenHashSet();

    @SuppressWarnings("unused")
    public static void addBlockReplacement(String blockNameIn,
        BiFunction<BlockConversionInfo, World, BlockConversionInfo> transformer) {
        blockReplacementMap.put(blockNameIn, transformer);
    }

    public static BlockConversionInfo getBlockReplacement(BlockConversionInfo blockConversionInfo, World world) {
        for (BiFunction<BlockConversionInfo, World, BlockConversionInfo> transformer : blockReplacementMap
            .get(blockConversionInfo.blockName)) {
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
