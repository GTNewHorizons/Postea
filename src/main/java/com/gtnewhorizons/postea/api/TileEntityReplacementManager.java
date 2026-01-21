package com.gtnewhorizons.postea.api;

import static com.gtnewhorizons.postea.Postea.LOG;

import java.util.HashMap;
import java.util.Objects;
import java.util.function.BiFunction;

import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import com.gtnewhorizons.postea.utility.BlockInfo;

@SuppressWarnings("unused")
public class TileEntityReplacementManager {

    private static final HashMap<String, TriFunction<NBTTagCompound, World, Chunk, BlockInfo>> tileEntityToNormalBlockTransformer = new HashMap<>();

    /**
     * @deprecated Superseded by {@link #tileEntityTransformer(String, TriFunction)}.
     */
    @Deprecated
    public static void tileEntityTransformer(String tileEntityId,
        BiFunction<NBTTagCompound, World, BlockInfo> transformerFunction) {
        tileEntityToNormalBlockTransformer
            .put(tileEntityId, (tag, world, chunk) -> transformerFunction.apply(tag, world));
    }

    /**
     * Register a callback function to transform a tile entity before it's loaded.
     * Callback returns a {@link BlockInfo} which the tile entity will be transformed into.</br>
     * Note that the callback will be called before the world is fully loaded, and world.getBlock will throw an error.
     * Use chunk.getBlock(x & 15, y, z & 15) instead.
     *
     * @param tileEntityId        ID of the tile entity to be transformed
     * @param transformerFunction Callback function to transforms the tile entity
     */
    @SuppressWarnings("unused")
    public static void tileEntityTransformer(String tileEntityId,
        TriFunction<NBTTagCompound, World, Chunk, BlockInfo> transformerFunction) {
        Objects.requireNonNull(tileEntityId, "tileEntityId must not be null");
        Objects.requireNonNull(transformerFunction, "transformerFunction must not be null");

        TriFunction<NBTTagCompound, World, Chunk, BlockInfo> existing = tileEntityToNormalBlockTransformer
            .putIfAbsent(tileEntityId, transformerFunction);

        if (existing != null) {
            throw new IllegalStateException(
                "Postea TileEntity transformer already registered for '" + tileEntityId + "'.");
        }

        LOG.info("TileEntity replacement registered successfully for {}", tileEntityId);
    }

    public static @Nullable TriFunction<NBTTagCompound, World, Chunk, BlockInfo> getTileEntityToNormalBlockTransformerFunction(
        String tileEntityId) {
        return tileEntityToNormalBlockTransformer.get(tileEntityId);
    }
}
