package com.gtnewhorizons.postea.utility;

import java.util.Map;
import java.util.function.Consumer;

import com.google.common.collect.LinkedHashMultimap;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.LoaderState;
import cpw.mods.fml.common.registry.GameData;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

/**
 * A simple utility to resolve the IDs of any namepsaced ID that has ever been registered to an active world's id list.
 */
public abstract class IDRegistry {

    // Part of a bi-map for lookups between block namespaced ids and numeric ids
    private static final Object2IntOpenHashMap<String> BLOCK_NAME_TO_ID = new Object2IntOpenHashMap<>();

    // Part of a bi-map for lookups between item namespaced ids and numeric ids
    private static final Object2IntOpenHashMap<String> ITEM_NAME_TO_ID = new Object2IntOpenHashMap<>();

    private static final LinkedHashMultimap<String, Consumer<Integer>> BLOCK_ID_RESOLVERS = LinkedHashMultimap.create();
    private static final LinkedHashMultimap<String, Consumer<Integer>> ITEM_ID_RESOLVERS = LinkedHashMultimap.create();

    /**
     * Triggers all the callbacks registered by other mods that wish to identify a block that may no longer exist.
     * These callbacks effectively remove the need for dummies, and should help improve performance at runtime.
     */
    public static void onMappingUpdated() {
        // this event is fired on server startup and shutdown. On shutdown, we just need to clear the mappings to save
        // some space memory we can just ignore adding stuff safely.
        if (!Loader.instance()
            .isInState(LoaderState.SERVER_ABOUT_TO_START)) return;
        // we should be fine to execute those in parallel I think?
        BLOCK_ID_RESOLVERS.asMap()
            .entrySet()
            .parallelStream()
            .forEach(kv -> {
                int id = getItemId(kv.getKey());
                for (Consumer<Integer> resolver : kv.getValue()) {
                    resolver.accept(id);
                }
            });
        ITEM_ID_RESOLVERS.asMap()
            .entrySet()
            .parallelStream()
            .forEach(kv -> {
                int id = getItemId(kv.getKey());
                for (Consumer<Integer> resolver : kv.getValue()) {
                    resolver.accept(id);
                }
            });
    }

    /**
     * Called by {@link com.gtnewhorizons.postea.mixins.preinit.MixinGameData}, this function is used to register all
     * the previously registered namespaced to numeric id mappings that have ever been seen into a world.<br>
     * <br>
     * This function executes before the game starts remapping it's own internal IDs, so we can be sure that any
     * existing will remain while the world is loaded.
     */
    public static void registerWorldSpecificIDs(Map<String, Integer> map) {
        BLOCK_NAME_TO_ID.clear();
        ITEM_NAME_TO_ID.clear();
        for (Map.Entry<String, Integer> kv : map.entrySet()) {
            String name = kv.getKey()
                .substring(1);
            int id = kv.getValue();
            if (kv.getKey()
                .charAt(0) == '\u0001') {
                BLOCK_NAME_TO_ID.put(name, id);
            } else {
                ITEM_NAME_TO_ID.put(name, id);
            }
        }
    }

    /**
     * Fetches the block numeric ID associated with the namespaced ID.
     *
     * @param name the namespaced id of the block.
     * @return The ID of the block, or -1 if the namespaced ID has never been registered while the current world was
     *         loaded.
     */
    public static int getBlockId(String name) {
        int id = BLOCK_NAME_TO_ID.getOrDefault(name, -1);
        if (id < 0) {
            id = GameData.getBlockRegistry()
                .getId(name);
            if (id >= 0) BLOCK_NAME_TO_ID.put(name, id);
        }
        return id;
    }

    /**
     * Fetches the item numeric ID associated with the namespaced ID.
     *
     * @param name the namespaced id of the block.
     * @return The ID of the block, or -1 if the namespaced ID has never been registered while the current world was
     *         loaded.
     */
    public static int getItemId(String name) {
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
