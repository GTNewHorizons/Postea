package com.gtnewhorizons.postea.utility;

import java.util.Map;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

import cpw.mods.fml.common.event.FMLMissingMappingsEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

/**
 * A simple utility used to allow mods to register simple missing mapping migrations without having to set up a whole
 * system themselves.
 */
public abstract class MissingMappingHandler {

    /**
     * A set of String IDs that can safely be ignored.
     */
    private static final Set<String> MISSING_IDS_TO_IGNORE = new ObjectOpenHashSet<>();
    /**
     * A set of string IDs and items they should map to.
     */
    private static final Map<String, Item> MISSING_ITEM_MAPPINGS = new Object2ObjectOpenHashMap<>();
    /**
     * A set of string IDs and items they should map to.
     */
    private static final Map<String, Block> MISSING_BLOCK_MAPPINGS = new Object2ObjectOpenHashMap<>();

    // region FML Life Cycle handlers
    public static void onMissingMapping(FMLMissingMappingsEvent event) {
        for (FMLMissingMappingsEvent.MissingMapping mapping : event.getAll()) {
            // ignore things that can be ignored safely, and our own dummy items for when a handler is removed.
            if (MISSING_IDS_TO_IGNORE.contains(mapping.name)) {
                mapping.ignore();
                continue;
            }

            if (mapping.type == GameRegistry.Type.BLOCK) {
                Block remappedBlock = MISSING_BLOCK_MAPPINGS.get(mapping.name);
                if (remappedBlock != null) {
                    mapping.remap(remappedBlock);
                    continue;
                }
            } else if (mapping.type == GameRegistry.Type.ITEM) {
                Block remappedBlock = MISSING_BLOCK_MAPPINGS.get(mapping.name);
                if (remappedBlock != null) {
                    mapping.remap(Item.getItemFromBlock(remappedBlock));
                    continue;
                }
                Item remappedItem = MISSING_ITEM_MAPPINGS.get(mapping.name);
                if (remappedItem != null) {
                    mapping.remap(MISSING_ITEM_MAPPINGS.get(mapping.name));
                    continue;
                }
            }
        }
    }
    // endregion FML Life Cycle handlers

    /**
     * Adds a direct mapping for a missing item mapping.
     * 
     * @param originalId The namespaced id of the missing item.
     * @param item       The item it should be replaced with.
     */
    public static void addItemMapping(String originalId, Item item) {
        MISSING_ITEM_MAPPINGS.put(originalId, item);
    }

    /**
     * Adds a direct mapping for a missing block mapping.
     * 
     * @param originalId The namespaced id of the missing block.
     * @param block      The block it should be replaced with.
     */
    public static void addBlockMapping(String originalId, Block block) {
        MISSING_BLOCK_MAPPINGS.put(originalId, block);
    }

    /**
     * Adds a notice that FML should silence any warning when loading a world telling the player that an ID
     * is now missing.
     *
     * @param originalId The namespace ID to ignore.
     */
    public static void addIgnore(String originalId) {
        MISSING_IDS_TO_IGNORE.add(originalId);
    }
}
