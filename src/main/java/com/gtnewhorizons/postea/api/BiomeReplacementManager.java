package com.gtnewhorizons.postea.api;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import net.minecraft.world.World;

import com.gtnewhorizons.postea.utility.BiomeConversionInfo;

public class BiomeReplacementManager {

    public static final Map<Integer, BiFunction<BiomeConversionInfo, World, BiomeConversionInfo>> biomeReplacementMap = new HashMap<>();

    @SuppressWarnings("unused")
    public static void addBiomeReplacement(int biomeID,
        BiFunction<BiomeConversionInfo, World, BiomeConversionInfo> transformer) {
        biomeReplacementMap.put(biomeID, transformer);
    }

    public static BiomeConversionInfo getBiomeReplacement(BiomeConversionInfo biomeConversionInfo, World world) {

        BiFunction<BiomeConversionInfo, World, BiomeConversionInfo> transformer = biomeReplacementMap
            .getOrDefault(biomeConversionInfo.biomeID, null);

        if (transformer == null) {
            return null;
        } else {
            return transformer.apply(biomeConversionInfo, world);
        }
    }

}
