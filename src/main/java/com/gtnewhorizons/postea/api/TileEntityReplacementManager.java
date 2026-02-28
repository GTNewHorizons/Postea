package com.gtnewhorizons.postea.api;

import java.util.function.Consumer;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import com.gtnewhorizons.postea.utility.BlockInfo;
import com.gtnewhorizons.postea.utility.PosteaUtilities;
import com.gtnewhorizons.postea.utility.TransformerRegistry;

/**
 * Public API used to register tile entity transformers via postea's handler system.<br>
 * <br>
 * Tile entity transformers are executed before any block transformers, when chunks being loading into the world.
 */
@SuppressWarnings("unused")
public abstract class TileEntityReplacementManager {

    /**
     * Register a callback function to transform a tile entity before it's loaded.
     * Callback returns a {@link BlockInfo} which the tile entity will be transformed into.
     * If you return a null value instead of a block info, Postea will assume that you don't want to replace the block
     * and/or tile entity and look for any other compatible handlers that may be able to do a replacement.<br>
     * <br>
     * Note that the callback will be called before the world is fully loaded, and {@code world.getBlock} will throw an
     * error. If you need to identify a block, consider using
     * {@link BlockReplacementManager#registerIDResolver(String, Consumer)}
     * and {@link BlockAccessCompat#getBlockIDAtTE(NBTTagCompound, Chunk)} instead. If you need to identify the meta
     * value, consider using {@code Chunk.getBlockMetadata(x & 15, y, z & 15)} or the other functions inside of
     * {@link BlockReplacementManager}.<br>
     * <br>
     * {@code BlockReplacementManager.registerIDResolver} registers a simple callback that will be executed just after
     * the game resolves all numeric ID conflicts when loading a save. The provided value is the numeric ID of the
     * block linked provided string ID or -1 if the block has <em>never</em> been registered while the currently
     * loading world.<br>
     * <br>
     * {@code BlockAccessCompat.getBlockIDAtTE} lets you fetch the ID of the block where the current tile entity is
     * located. Since forge doesn't actually replace the id of missing blocks for performance reasons, opting to treat
     * them like air instead; You can use the value given to the resolver you registered using
     * {@code BlockReplacementManager.registerIDResolver} to identify the block directly.<br>
     * <br>
     * Using these 2 methods skips the need to register dummy blocks or items to identify blocks when migrating things,
     * which will improve the overall performance and compatibility of your migrations.
     *
     * @param tileEntityId        ID of the tile entity to be transformed
     * @param transformerFunction Callback function to transforms the tile entity
     */
    public static void tileEntityTransformer(String tileEntityId,
        TriFunction<NBTTagCompound, World, Chunk, BlockInfo> transformerFunction) {
        if (transformerFunction == null) throw new NullPointerException("transformer is null");
        if (tileEntityId == null) throw new NullPointerException("transformer is null");
        TransformerRegistry.addTileEntityTransformer(tileEntityId, transformerFunction);
    }

    /**
     * Creates a bare-bones tile entity tag for the provided ID that clones all the positional information of the
     * provided tile entity NBT Tag. The passed NBT tag is never modified.
     *
     * @param tag   The tag of the tile entity at the current position.
     * @param newId The ID of the new tile entity.
     * @return A bare-bones tile entity tag for the provided id at ths same position as the passed tile entity tag.
     */
    public static NBTTagCompound createTETagAtSamePosition(String newId, NBTTagCompound tag) {
        if (tag == null) throw new NullPointerException("tag was null");
        if (newId == null) throw new NullPointerException("tag was null");
        return PosteaUtilities.cleanseNBT(newId, tag);
    }
}
