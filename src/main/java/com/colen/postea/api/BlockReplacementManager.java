package com.colen.postea.api;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.BiFunction;

import net.minecraft.world.World;

import com.colen.postea.utility.BlockConversionInfo;

public class BlockReplacementManager {

    public static final Map<String, BiFunction<BlockConversionInfo, World, BlockConversionInfo>> blockReplacementMap = new HashMap<>();
    public static final HashSet<Integer> posteaMarkedIDs = new HashSet<>();

    @SuppressWarnings("unused")
    public static void addBlockReplacement(String blockNameIn,
        BiFunction<BlockConversionInfo, World, BlockConversionInfo> transformer) {
        blockReplacementMap.put(blockNameIn, transformer);
    }

    public static BlockConversionInfo getBlockReplacement(BlockConversionInfo blockConversionInfo, World world) {

        BiFunction<BlockConversionInfo, World, BlockConversionInfo> transformer = blockReplacementMap
            .getOrDefault(blockConversionInfo.blockName, null);

        if (transformer == null) {
            return null;
        } else {
            return transformer.apply(blockConversionInfo, world);
        }
    }

    // We need this to save reprocessing blocks.
    public static boolean blockNotConvertible(int blockID) {
        return !posteaMarkedIDs.contains(blockID);
    }

}
