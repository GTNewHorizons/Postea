package com.gtnewhorizons.postea.utility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.LinkedListMultimap;
import com.gtnewhorizons.postea.Postea;
import com.gtnewhorizons.postea.api.BlockNameAndMeta;
import com.gtnewhorizons.postea.api.IBlockTransformationHandler;
import com.gtnewhorizons.postea.api.IDExtenderCompat;
import com.gtnewhorizons.postea.api.IItemStackTransformationHandler;
import com.gtnewhorizons.postea.api.TriFunction;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.LoaderState;
import cpw.mods.fml.common.registry.GameData;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntList;

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
    /// Maps an item id to the names that hold it and their handlers. Generated during the mapping update event when a
    /// world loads. An id carries several names when content that once held it was removed.
    private static final Int2ObjectOpenHashMap<List<Pair<String, List<IItemStackTransformationHandler>>>> RUNTIME_ITEM_REPLACEMENT_MAP = new Int2ObjectOpenHashMap<>();
    /// Maps a block id to the names that hold it and their handlers. Generated during the mapping update event when a
    /// world loads. An id carries several names when content that once held it was removed.
    private static final Int2ObjectOpenHashMap<List<Pair<String, List<IBlockTransformationHandler>>>> RUNTIME_BLOCK_REPLACEMENT_MAP = new Int2ObjectOpenHashMap<>();

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
        for (String key : BLOCK_REPLACEMENT_MAP.keySet()) {
            IntList ids = IDRegistry.getBlockIds(key);
            if (ids.isEmpty()) {
                Postea.LOG.debug("Block {} has no id in this world; its transformers stay inactive", key);
                continue;
            }
            for (int id : ids) {
                RUNTIME_BLOCK_REPLACEMENT_MAP.computeIfAbsent(id, unused -> new ArrayList<>())
                    .add(Pair.of(key, BLOCK_REPLACEMENT_MAP.get(key)));
            }
        }
        for (String key : ITEM_REPLACEMENT_MAP.keySet()) {
            IntList ids = IDRegistry.getItemIds(key);
            if (ids.isEmpty()) {
                Postea.LOG.debug("Item {} has no id in this world; its transformers stay inactive", key);
                continue;
            }
            for (int id : ids) {
                RUNTIME_ITEM_REPLACEMENT_MAP.computeIfAbsent(id, unused -> new ArrayList<>())
                    .add(Pair.of(key, ITEM_REPLACEMENT_MAP.get(key)));
            }
        }
    }
    // endregion FML Life Cycle handlers

    /// Registers a block transformation handler for the given namespaced id.
    ///
    /// The id may name content that no longer exists. The handler runs on every numeric id the content held in this
    /// world, as resolved through the world's saved id map and retired id table.
    ///
    /// @param originalId the namespaced id of the block to transform
    /// @param transformer the handler that transforms the block
    public static void addBlockTransformer(@Nonnull String originalId,
        @Nonnull IBlockTransformationHandler transformer) {
        BLOCK_REPLACEMENT_MAP.put(originalId, transformer);
    }

    /// Registers an item transformation handler for the given namespaced id.
    ///
    /// The id may name content that no longer exists. The handler runs on every numeric id the content held in this
    /// world, as resolved through the world's saved id map and retired id table.
    ///
    /// @param originalId the namespaced id of the item to transform
    /// @param transformer the handler that transforms the stack
    public static void addStackTransformer(@Nonnull String originalId,
        @Nonnull IItemStackTransformationHandler transformer) {
        ITEM_REPLACEMENT_MAP.put(originalId, transformer);
    }

    /// Registers a tile entity transformation handler for the given saved tile entity id. Handlers run in
    /// registration order until one returns a non-null [BlockInfo].
    ///
    /// @param originalId the saved id of the tile entity to transform
    /// @param transformer the handler that transforms the tile entity
    public static void addTileEntityTransformer(@Nonnull String originalId,
        @Nonnull TriFunction<NBTTagCompound, World, Chunk, BlockInfo> transformer) {
        TILE_ENTITY_REPLACEMENT_MAP.put(originalId, transformer);
    }

    // region transformation handlers
    public static @Nullable BlockConversionInfo getBlockReplacement(int blockId, int metadata, World world, int x,
        int y, int z) {
        List<Pair<String, List<IBlockTransformationHandler>>> candidates = RUNTIME_BLOCK_REPLACEMENT_MAP.get(blockId);
        if (candidates == null) return null;
        for (Pair<String, List<IBlockTransformationHandler>> data : candidates) {
            BlockConversionInfo info = new BlockConversionInfo(data.getKey(), blockId, metadata, x, y, z, world);
            for (IBlockTransformationHandler transformer : data.getValue()) {
                if (transformer.apply(info)) return info;
            }
        }
        return null;
    }

    public static @Nullable BlockNameAndMeta transformBlock(@Nonnull String name, int metadata) {
        List<IBlockTransformationHandler> handlers = BLOCK_REPLACEMENT_MAP.get(name);
        if (handlers.isEmpty()) return null;
        BlockConversionInfo blockConversionInfo = new BlockConversionInfo(
            name,
            IDRegistry.getBlockId(name),
            metadata,
            0,
            0,
            0,
            null);
        for (IBlockTransformationHandler transformer : handlers) {
            if (transformer.apply(blockConversionInfo)) {
                Block block = GameData.getBlockRegistry()
                    .getRaw(blockConversionInfo.blockID);
                if (block == null) return null;
                return new BlockNameAndMeta(
                    GameData.getBlockRegistry()
                        .getNameForObject(block),
                    blockConversionInfo.metadata);
            }
        }
        return null;
    }

    public static void transformItem(NBTTagCompound tag) {
        // abort early if tag is bad.
        if (tag.hasNoTags() || !tag.hasKey("id")) return;
        List<Pair<String, List<IItemStackTransformationHandler>>> candidates = RUNTIME_ITEM_REPLACEMENT_MAP
            .get(IDExtenderCompat.getItemStackID(tag));
        if (candidates == null) return;
        for (Pair<String, List<IItemStackTransformationHandler>> data : candidates) {
            for (IItemStackTransformationHandler transformer : data.getValue()) {
                if (transformer.apply(data.getKey(), tag)) return;
            }
        }
    }

    public static Collection<TriFunction<NBTTagCompound, World, Chunk, BlockInfo>> getTileEntityToNormalBlockTransformerFunction(
        String tileEntityId) {
        return TILE_ENTITY_REPLACEMENT_MAP.get(tileEntityId);
    }
    // endregion transformation handlers
}
