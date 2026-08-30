package com.gtnewhorizons.postea.utility;

import java.io.File;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.google.common.collect.LinkedHashMultimap;
import com.gtnewhorizons.postea.Postea;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.LoaderState;
import cpw.mods.fml.common.registry.FMLControlledNamespacedRegistry;
import cpw.mods.fml.common.registry.GameData;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

/**
 * Resolves a namespaced id to the numeric ids it holds or held in the active world: the live registry id, the id
 * the world's own saved map assigns it, and the ids recorded in the world's {@link WorldIdTable} for content that
 * has since been removed. A retired id counts only while FML still blocks it and no live content occupies it.
 */
public abstract class IDRegistry {

    // Part of a bi-map for lookups between block namespaced ids and numeric ids
    private static final Object2IntOpenHashMap<String> BLOCK_NAME_TO_ID = new Object2IntOpenHashMap<>();

    // Part of a bi-map for lookups between item namespaced ids and numeric ids
    private static final Object2IntOpenHashMap<String> ITEM_NAME_TO_ID = new Object2IntOpenHashMap<>();

    private static final LinkedHashMultimap<String, Consumer<Integer>> BLOCK_ID_RESOLVERS = LinkedHashMultimap.create();
    private static final LinkedHashMultimap<String, Consumer<Integer>> ITEM_ID_RESOLVERS = LinkedHashMultimap.create();

    private static File tableFile;
    private static WorldIdTable table = new WorldIdTable();
    private static IntSet blockedIds;

    /**
     * Opens the retired id table of the world about to load. Until {@link #endWorld()} the saved id map is captured
     * and its missing names merged into it.
     */
    public static void beginWorld(File worldDirectory) {
        tableFile = new File(new File(worldDirectory, Postea.MODID), WorldIdTable.FILE_NAME);
        table = WorldIdTable.load(tableFile);
    }

    public static void endWorld() {
        tableFile = null;
        table = new WorldIdTable();
        blockedIds = null;
        BLOCK_NAME_TO_ID.clear();
        ITEM_NAME_TO_ID.clear();
    }

    /**
     * Triggers all the callbacks registered by other mods that wish to identify a block that may no longer exist.
     * These callbacks effectively remove the need for dummies, and should help improve performance at runtime.
     */
    public static void onMappingUpdated() {
        // this event is fired on server startup and shutdown. On shutdown, we just need to clear the mappings to save
        // some space memory we can just ignore adding stuff safely.
        if (!Loader.instance()
            .isInState(LoaderState.SERVER_ABOUT_TO_START)) return;
        for (Map.Entry<String, Collection<Consumer<Integer>>> kv : BLOCK_ID_RESOLVERS.asMap()
            .entrySet()) {
            int id = getBlockId(kv.getKey());
            for (Consumer<Integer> resolver : kv.getValue()) {
                resolver.accept(id);
            }
        }
        for (Map.Entry<String, Collection<Consumer<Integer>>> kv : ITEM_ID_RESOLVERS.asMap()
            .entrySet()) {
            int id = getItemId(kv.getKey());
            for (Consumer<Integer> resolver : kv.getValue()) {
                resolver.accept(id);
            }
        }
    }

    /**
     * Called by {@link com.gtnewhorizons.postea.mixins.preinit.MixinGameData}, this function captures the world's
     * saved name-to-id map before FML remaps the live registries onto it, and records the names no registered
     * content answers to in the world's retired id table. Ignored outside a server world and off the server thread,
     * since the client side of the FML handshake also injects a map.
     */
    public static void registerWorldSpecificIDs(Map<String, Integer> map) {
        if (tableFile == null || !FMLCommonHandler.instance()
            .getEffectiveSide()
            .isServer()) return;
        BLOCK_NAME_TO_ID.clear();
        ITEM_NAME_TO_ID.clear();
        blockedIds = null;
        Map<String, Integer> missing = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> kv : map.entrySet()) {
            String name = kv.getKey()
                .substring(1);
            boolean block = kv.getKey()
                .charAt(0) == WorldIdTable.BLOCK_PREFIX;
            if (block) {
                BLOCK_NAME_TO_ID.put(name, (int) kv.getValue());
            } else {
                ITEM_NAME_TO_ID.put(name, (int) kv.getValue());
            }
            FMLControlledNamespacedRegistry<?> registry = block ? GameData.getBlockRegistry()
                : GameData.getItemRegistry();
            if (registry.getId(name) < 0) missing.put(kv.getKey(), kv.getValue());
        }
        if (table.merge(missing)) table.save(tableFile);
    }

    /**
     * The block id {@code name} currently holds in this world: the live registry id, or the id it held before its
     * content was removed, or -1 when the world never mapped it.
     */
    public static int getBlockId(String name) {
        int id = liveBlockId(name);
        if (id >= 0) return id;
        IntList retired = retiredBlockIds(name, id);
        return retired.isEmpty() ? -1 : retired.getInt(retired.size() - 1);
    }

    /**
     * The item id {@code name} currently holds in this world: the live registry id, or the id it held before its
     * content was removed, or -1 when the world never mapped it.
     */
    public static int getItemId(String name) {
        int id = liveItemId(name);
        if (id >= 0) return id;
        IntList retired = retiredItemIds(name, id);
        return retired.isEmpty() ? -1 : retired.getInt(retired.size() - 1);
    }

    /**
     * Every block id {@code name} holds or held in this world, live id first. Empty when the world never mapped
     * the name.
     */
    public static IntList getBlockIds(String name) {
        int live = liveBlockId(name);
        IntList ids = retiredBlockIds(name, live);
        if (live >= 0) {
            ids.add(0, live);
        } else if (!ids.isEmpty()) {
            Postea.LOG.info("Block {} is no longer registered; its transformers target retired id(s) {}", name, ids);
        }
        return ids;
    }

    /**
     * Every item id {@code name} holds or held in this world, live id first. Empty when the world never mapped
     * the name.
     */
    public static IntList getItemIds(String name) {
        int live = liveItemId(name);
        IntList ids = retiredItemIds(name, live);
        if (live >= 0) {
            ids.add(0, live);
        } else if (!ids.isEmpty()) {
            Postea.LOG.info("Item {} is no longer registered; its transformers target retired id(s) {}", name, ids);
        }
        return ids;
    }

    private static IntList retiredBlockIds(String name, int live) {
        IntList ids = new IntArrayList();
        for (int id : table.blockIds(name)) {
            if (id != live && blockedIds().contains(id)
                && GameData.getBlockRegistry()
                    .getRaw(id) == null) {
                ids.add(id);
            }
        }
        return ids;
    }

    private static IntList retiredItemIds(String name, int live) {
        IntList ids = new IntArrayList();
        for (int id : table.itemIds(name)) {
            if (id != live && blockedIds().contains(id)
                && GameData.getItemRegistry()
                    .getRaw(id) == null) {
                ids.add(id);
            }
        }
        return ids;
    }

    private static IntSet blockedIds() {
        IntSet ids = blockedIds;
        if (ids == null) {
            ids = new IntOpenHashSet(GameData.getBlockedIds());
            blockedIds = ids;
        }
        return ids;
    }

    private static int liveBlockId(String name) {
        int id = BLOCK_NAME_TO_ID.getOrDefault(name, -1);
        if (id < 0) {
            id = GameData.getBlockRegistry()
                .getId(name);
            if (id >= 0) BLOCK_NAME_TO_ID.put(name, id);
        }
        return id;
    }

    private static int liveItemId(String name) {
        int id = ITEM_NAME_TO_ID.getOrDefault(name, -1);
        if (id < 0) {
            id = GameData.getItemRegistry()
                .getId(name);
            if (id >= 0) ITEM_NAME_TO_ID.put(name, id);
        }
        return id;
    }

    /**
     * Registers a callback used by mods to identify the numeric ID of a namespaced ID when a world is loaded.
     *
     * @param name     The namespaced ID to resolve.
     * @param resolver The callback to call just after forge adjusts the ID mappings to prevent collisions.
     */
    public static void registerBlockIDResolver(String name, Consumer<Integer> resolver) {
        BLOCK_ID_RESOLVERS.put(name, resolver);
    }

    /**
     * Registers a callback used by mods to identify the numeric ID of a namespaced ID when a world is loaded.
     *
     * @param name     The namespaced ID to resolve.
     * @param resolver The callback to call just after forge adjusts the ID mappings to prevent collisions.
     */
    public static void registerItemIDResolver(String name, Consumer<Integer> resolver) {
        ITEM_ID_RESOLVERS.put(name, resolver);
    }
}
