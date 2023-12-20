package com.colen.postea.API;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import net.minecraft.world.World;

import com.colen.postea.Utility.BlockConversionInfo;

public class BlockReplacementManager {

    private static final Map<String, BiFunction<BlockConversionInfo, World, BlockConversionInfo>> blockReplacementMap = new HashMap<>();

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

}
