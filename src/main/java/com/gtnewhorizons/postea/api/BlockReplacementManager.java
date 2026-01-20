package com.gtnewhorizons.postea.api;

import java.util.function.BiFunction;

import net.minecraft.world.World;

import com.google.common.collect.LinkedListMultimap;
import com.gtnewhorizons.postea.PosteaMissingMappingHandler;
import com.gtnewhorizons.postea.utility.BlockConversionInfo;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

public class BlockReplacementManager {

    public static final LinkedListMultimap<String, BiFunction<BlockConversionInfo, World, BlockConversionInfo>> blockReplacementMap = LinkedListMultimap
        .create();
    public static final IntOpenHashSet posteaMarkedIDs = new IntOpenHashSet();

    @SuppressWarnings("unused")
    public static void addBlockReplacement(String blockNameIn,
        BiFunction<BlockConversionInfo, World, BlockConversionInfo> transformer) {
        PosteaMissingMappingHandler.createDummyBlockIfNeeded(blockNameIn);
        blockReplacementMap.put(blockNameIn, transformer);
    }

    public static BlockConversionInfo getBlockReplacement(BlockConversionInfo blockConversionInfo, World world) {
        // auto change the value in case a handler check it, we can't do anything about the block id
        // but hopefully that's never going to be an issue
        String originalName = PosteaMissingMappingHandler.getOriginalIDIfIDIsDummyID(blockConversionInfo.blockName);
        blockConversionInfo.blockName = originalName;
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
