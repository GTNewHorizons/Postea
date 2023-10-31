package com.colen.postea.API;

import com.colen.postea.Utility.BlockInfo;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.function.BiFunction;

public class TileEntityReplacementManager {

    private static final HashMap<String, BiFunction<NBTTagCompound, World, BlockInfo>> tileEntityToNormalBlockTransformer = new HashMap<>();

    public static void tileEntityTransformer(String tileEntityId, BiFunction<NBTTagCompound, World, BlockInfo> transformerFunction) {
        tileEntityToNormalBlockTransformer.put(tileEntityId, transformerFunction);
    }

    public static BiFunction<NBTTagCompound, World, BlockInfo> getTileEntityToNormalBlockTransformerFunction(String tileEntityId) {
        return tileEntityToNormalBlockTransformer.getOrDefault(tileEntityId, null);
    }
}
