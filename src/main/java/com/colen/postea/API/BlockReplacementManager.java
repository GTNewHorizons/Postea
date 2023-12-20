package com.colen.postea.API;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.colen.postea.Utility.BlockConversionInfo;

public class BlockReplacementManager {

    private static final Map<String, Function<BlockConversionInfo, BlockConversionInfo>> blockReplacementMap = new HashMap<>();

    @SuppressWarnings("unused")
    public static void addBlockReplacement(String blockNameIn,
        Function<BlockConversionInfo, BlockConversionInfo> transformer) {
        blockReplacementMap.put(blockNameIn, transformer);
    }

    @SuppressWarnings("unused")
    public static BlockConversionInfo getBlockReplacement(BlockConversionInfo blockConversionInfo) {

        Function<BlockConversionInfo, BlockConversionInfo> transformer = blockReplacementMap
            .getOrDefault(blockConversionInfo.blockName, null);

        if (transformer == null) {
            return null;
        } else {
            return transformer.apply(blockConversionInfo);
        }
    }

}
