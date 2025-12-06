package com.gtnewhorizons.postea.api;

import java.util.HashMap;
import java.util.function.BiFunction;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import com.gtnewhorizons.postea.utility.BlockInfo;

@SuppressWarnings("unused")
public class TileEntityReplacementManager {

    private static final HashMap<String, TriFunction<NBTTagCompound, World, Chunk, BlockInfo>> tileEntityToNormalBlockTransformer = new HashMap<>();

    public static void tileEntityTransformer(String tileEntityId,
        BiFunction<NBTTagCompound, World, BlockInfo> transformerFunction) {
        tileEntityToNormalBlockTransformer
            .put(tileEntityId, (tag, world, chunk) -> transformerFunction.apply(tag, world));
    }

    public static void tileEntityTransformer(String tileEntityId,
        TriFunction<NBTTagCompound, World, Chunk, BlockInfo> transformerFunction) {
        tileEntityToNormalBlockTransformer.put(tileEntityId, transformerFunction);
    }

    public static TriFunction<NBTTagCompound, World, Chunk, BlockInfo> getTileEntityToNormalBlockTransformerFunction(
        String tileEntityId) {
        return tileEntityToNormalBlockTransformer.getOrDefault(tileEntityId, null);
    }
}
