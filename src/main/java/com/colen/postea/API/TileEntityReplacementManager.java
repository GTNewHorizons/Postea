package com.colen.postea.API;

import net.minecraft.nbt.NBTTagCompound;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class TileEntityReplacementManager {

    private static final Map<String, Function<NBTTagCompound, NBTTagCompound>> tileEntityFixMap = new HashMap<>();
    private static final HashMap<String, Function<NBTTagCompound, ChunkFixerUtility.BlockInfo>> tileEntityToNormalBlockTransformer = new HashMap<>();

    @SuppressWarnings("unused")
    public static void tileEntityFixer(String tileName , Function<NBTTagCompound, NBTTagCompound> fixerFunction) {
        tileEntityFixMap.put(tileName, fixerFunction);
    }

    public static Function<NBTTagCompound, NBTTagCompound> getTileEntityFixer(String tileName) {
        return tileEntityFixMap.getOrDefault(tileName, null);
    }

    public static void tileEntityTransformer(String tileEntityId, Function<NBTTagCompound, ChunkFixerUtility.BlockInfo> transformerFunction) {
        tileEntityToNormalBlockTransformer.put(tileEntityId, transformerFunction);
    }

    public static Function<NBTTagCompound, ChunkFixerUtility.BlockInfo> getTileEntityToNormalBlockTransformerFunction(String tileEntityId) {
        return tileEntityToNormalBlockTransformer.getOrDefault(tileEntityId, null);
    }
}
