package com.gtnewhorizons.postea.utility;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.LinkedListMultimap;
import com.gtnewhorizons.postea.api.IBlockTransformationHandler;
import com.gtnewhorizons.postea.api.IDExtenderCompat;
import com.gtnewhorizons.postea.api.IItemStackTransformationHandler;
import com.gtnewhorizons.postea.api.TriFunction;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.LoaderState;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

/**
 * A registry for all transformer functions
 */
public class TransformerRegistry {

    private static final LinkedListMultimap<String, IItemStackTransformationHandler> ITEM_REPLACEMENT_MAP = LinkedListMultimap
        .create();
    public static final LinkedListMultimap<String, IBlockTransformationHandler> BLOCK_REPLACEMENT_MAP = LinkedListMultimap
        .create();
    private static final LinkedListMultimap<String, TriFunction<NBTTagCompound, World, Chunk, BlockInfo>> TILE_ENTITY_REPLACEMENT_MAP = LinkedListMultimap
        .create();
    /**
     * Map of itemID -> originalName+transformationHandler.
     * Pivoting on the id is faster than pivoting on the strings so this should help with perf a bit.
     * Generated during the mapping update event whilst entering/loading a world.
     */
    private static final Int2ObjectOpenHashMap<Pair<String, List<IItemStackTransformationHandler>>> RUNTIME_ITEM_REPLACEMENT_MAP = new Int2ObjectOpenHashMap<>();
    /**
     * Map of blockIO -> originalName+transformationHandler.
     * Pivoting on the id is faster than pivoting on the strings so this should help with perf a bit.
     * Generated during the mapping update event whilst entering/loading a world.
     */
    private static final Int2ObjectOpenHashMap<Pair<String, List<IBlockTransformationHandler>>> RUNTIME_BLOCK_REPLACEMENT_MAP = new Int2ObjectOpenHashMap<>();

    // region FML Life Cycle handlers
    /**
     * When FML loads a world, it remaps certain IDs if the IDs it registered during the load period do not match the
     * IDs that were previously registered when the world was last loaded.
     * <br>
     * We use this opportunity to fetch the "world specific" IDs of all the blocks and items that have a
     * transformation registered to them so we can pivot using the numeric IDs when we're doing replacements.
     * <br>
     * This is because pivoting on the numeric ids is significantly faster than pivoting on string or object hashes.
     */
    public static void onIdMappingsChanged() {
        RUNTIME_BLOCK_REPLACEMENT_MAP.clear();
        RUNTIME_ITEM_REPLACEMENT_MAP.clear();
        // this event is fired on server startup and shutdown. On shutdown, we just need to clear the mappings to save
        // some space memory we can just ignore adding stuff safely.
        if (!Loader.instance()
            .isInState(LoaderState.SERVER_ABOUT_TO_START)) return;
        // create a lookup for source block ids.
        for (String key : BLOCK_REPLACEMENT_MAP.keys()) {
            int id = IDRegistry.getBlockId(key);
            if (id >= 0) {
                RUNTIME_BLOCK_REPLACEMENT_MAP.put(id, Pair.of(key, BLOCK_REPLACEMENT_MAP.get(key)));
            }
        }
        // create a lookup for source block ids.
        for (String key : ITEM_REPLACEMENT_MAP.keys()) {
            int id = IDRegistry.getItemId(key);
            if (id >= 0) {
                RUNTIME_ITEM_REPLACEMENT_MAP.put(id, Pair.of(key, ITEM_REPLACEMENT_MAP.get(key)));
            }
        }
    }
    // endregion FML Life Cycle handlers

    /**
     * Registers a block transformation handler function for the given id.
     *
     * @implNote It doesn't matter if the id doesn't belong to an existing block, a dummy block will be created when
     *           Postea detects that the id isn't associated to a block during the FMLLoadCompleted event. Dependent
     *           mods don't need to worry about this as the block name in the BlockConversionInfo is automatically
     *           updated to the original id when attempting to transform a dummy block. When Postea runs a
     *           transformation on a dummy block and no handler return true, Postea will automatically convert it to
     *           air.
     *
     * @param originalId  The namespaced id of the block to transform.
     * @param transformer The transformation handler that will execute a transformation on the block.
     */
    public static void addBlockTransformer(@Nonnull String originalId,
        @Nonnull IBlockTransformationHandler transformer) {
        BLOCK_REPLACEMENT_MAP.put(originalId, transformer);
    }

    /**
     * Registers an item transformation handler function for the given id.
     *
     * @implNote It doesn't matter if the id doesn't belong to an existing item, a dummy item will be created when
     *           Postea detects that the id isn't associated to an item during the FMLLoadCompleted event. Dependent
     *           mods don't need to worry about this as transformation handlers are given the original id associated
     *           with the nbt they are provided when the handler is executed. When Postea runs a transformation on a
     *           dummy item and no handler returns true, Postea will automatically invalidate the stack by removing
     *           its id and idExt fields.
     *
     * @param originalId  The namespaced id of the block to transform.
     * @param transformer The transformation handler that will execute a transformation on the block.
     */
    public static void addStackTransformer(@Nonnull String originalId,
        @Nonnull IItemStackTransformationHandler transformer) {
        ITEM_REPLACEMENT_MAP.put(originalId, transformer);
    }

    /**
     * Registers an item transformation handler function for the given id.
     *
     * @implNote It doesn't matter if the id doesn't belong to an existing item, a dummy item will be created when
     *           Postea detects that the id isn't associated to an item during the FMLLoadCompleted event. Dependent
     *           mods don't need to worry about this as transformation handlers are given the original id associated
     *           with the nbt they are provided when the handler is executed. When Postea runs a transformation on a
     *           dummy item and no handler returns true, Postea will automatically invalidate the stack by removing
     *           its id and idExt fields.
     *
     * @param originalId  The namespaced id of the block to transform.
     * @param transformer The transformation handler that will execute a transformation on the block.
     */
    public static void addTileEntityTransformer(@Nonnull String originalId,
        @Nonnull TriFunction<NBTTagCompound, World, Chunk, BlockInfo> transformer) {
        TILE_ENTITY_REPLACEMENT_MAP.put(originalId, transformer);
    }

    // region transformation handlers
    public static @Nullable BlockConversionInfo getBlockReplacement(int blockId, byte metadata, World world, int x,
        int y, int z) {
        Pair<String, List<IBlockTransformationHandler>> data = RUNTIME_BLOCK_REPLACEMENT_MAP.get(blockId);
        if (data == null) return null;
        BlockConversionInfo blockConversionInfo = new BlockConversionInfo(
            // transparently maps the id to the original id if it's a dummy id
            data.getKey(),
            blockId,
            metadata,
            x,
            y,
            z,
            world);
        for (IBlockTransformationHandler transformer : data.getValue()) {
            if (transformer.apply(blockConversionInfo)) {
                return blockConversionInfo;
            }
        }
        return null;
    }

    public static void transformItem(NBTTagCompound tag) {
        // abort early if tag is bad.
        if (tag.hasNoTags() || !tag.hasKey("id")) return;
        // get handler
        int id = IDExtenderCompat.getItemStackID(tag);
        Pair<String, List<IItemStackTransformationHandler>> data = RUNTIME_ITEM_REPLACEMENT_MAP.get(id);
        // abort early if handler not found
        if (data != null && !data.getValue()
            .isEmpty()) {
            // apply handlers
            for (IItemStackTransformationHandler transformer : data.getValue()) {
                if (transformer.apply(data.getKey(), tag)) {
                    return;
                }
            }
        }
    }

    public static Collection<TriFunction<NBTTagCompound, World, Chunk, BlockInfo>> getTileEntityToNormalBlockTransformerFunction(
        String tileEntityId) {
        return TILE_ENTITY_REPLACEMENT_MAP.get(tileEntityId);
    }
    // endregion transformation handlers
}
