package com.gtnewhorizons.postea.utility;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;

import com.gtnewhorizons.postea.Postea;
import com.gtnewhorizons.postea.compat.Compat;

import cpw.mods.fml.common.event.FMLMissingMappingsEvent;
import cpw.mods.fml.common.registry.GameRegistry;

public abstract class MissingMappingHandler {

    enum MappingType {
        /** Tells the mapping logic to register an Item. */
        Item,
        /** Tells the mapping logic to register a Block (and ItemBlock). */
        Block
    }

    static class DummyMapping {

        /** The type of thing that should be registered for this dummy */
        public MappingType type;
        /** The id under which the dummy should be registered */
        public String registerAs;
        /** The full id of the dummy, including mod prefix */
        public String id;

        public DummyMapping(MappingType type, String registerAs, String id) {
            this.type = type;
            this.registerAs = registerAs;
            this.id = id;
        }
    }

    // because we're using reflection to quickly identify our stuff at runtime.
    public static class DummyBlock extends Block {

        public DummyBlock() {
            super(Material.ground);
        }
    }

    public static class DummyItem extends Item {
    }

    public static class DummyItemBlock extends ItemBlock {

        public DummyItemBlock(Block p_i45328_1_) {
            super(p_i45328_1_);
        }
    }

    private static final HashMap<String, Item> MISSING_ITEM_MAPPINGS = new HashMap<>();
    private static final HashMap<String, Block> MISSING_BLOCK_MAPPINGS = new HashMap<>();
    private static final HashMap<String, DummyMapping> DUMMY_TARGET_TYPES = new HashMap<>();
    private static final HashMap<String, String> REVERSE_DUMMY_ID_LOOKUP = new HashMap<>();

    private static final String DUMMY_ID_PREFIX_START = Postea.MODID + ":";
    private static final String DUMMY_ID_PREFIX_END = "d.";
    private static final String DUMMY_ID_PREFIX = DUMMY_ID_PREFIX_START + DUMMY_ID_PREFIX_END;

    public static void onLoadCompleted() {
        for (Map.Entry<String, DummyMapping> kv : DUMMY_TARGET_TYPES.entrySet()) {
            DummyMapping mapping = kv.getValue();
            String originalId = kv.getKey();
            // split the original id into parts to query for the original's existance.
            String[] splitId = splitItemId(originalId);
            if (splitId == null) continue;
            // ensure nothing else is registered under that id.
            if (GameRegistry.findBlock(splitId[0], splitId[1]) != null) continue;
            if (GameRegistry.findItem(splitId[0], splitId[1]) != null) continue;
            // remap if not found
            if (mapping.type == MappingType.Block) {
                // check if the block was since registered.
                Block dummy = new DummyBlock();
                GameRegistry.registerBlock(dummy, DummyItemBlock.class, mapping.registerAs);
                // register both for when we're remapping from ourselves to ourselves because something wasn't loaded
                // and transformed.
                MISSING_BLOCK_MAPPINGS.put(originalId, dummy);
                Compat.hideItemFromNEI(Item.getItemFromBlock(dummy));
            } else {
                Item dummy = new DummyItem();
                GameRegistry.registerItem(dummy, mapping.registerAs);
                MISSING_ITEM_MAPPINGS.put(originalId, dummy);
                Compat.hideItemFromNEI(dummy);
            }
            REVERSE_DUMMY_ID_LOOKUP.put(mapping.id, originalId);
        }
        // no longer needed free what ever used to be in there
        DUMMY_TARGET_TYPES.clear();
    }

    public static void onMissingMapping(FMLMissingMappingsEvent event) {
        for (FMLMissingMappingsEvent.MissingMapping mapping : event.getAll()) {
            if (mapping.name.startsWith(DUMMY_ID_PREFIX)) {
                // for when a migrator ceases being registered, likely due to a removal.
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
        // no longer needed free what ever used to be in there
        MISSING_BLOCK_MAPPINGS.clear();
        MISSING_ITEM_MAPPINGS.clear();
    }

    private static void onMappingAdded(String originalId, MappingType mappingType) {
        // split name into parts
        String[] splitId = splitItemId(originalId);
        if (splitId == null) return;
        // else register a fake item
        final String registerAs = DUMMY_ID_PREFIX_END + splitId[0] + "." + splitId[1];
        final String dummyFullId = DUMMY_ID_PREFIX_START + registerAs;
        if (mappingType == MappingType.Item) {
            DUMMY_TARGET_TYPES.putIfAbsent(originalId, new DummyMapping(mappingType, registerAs, dummyFullId));
        } else {
            DummyMapping existing = DUMMY_TARGET_TYPES.getOrDefault(originalId, null);
            if (existing != null) {
                existing.type = mappingType;
            } else {
                DUMMY_TARGET_TYPES.put(originalId, new DummyMapping(mappingType, registerAs, dummyFullId));
            }
        }
    }

    public static void createDummyBlockIfNeeded(String originalId) {
        DummyMapping existing = DUMMY_TARGET_TYPES.get(originalId);
        // item mappings should be over-written
        if (existing != null && existing.type == MappingType.Block) return;
        onMappingAdded(originalId, MappingType.Block);
    }

    public static void createDummyItemIfNeeded(String originalId) {
        DummyMapping existing = DUMMY_TARGET_TYPES.get(originalId);
        if (existing != null) return;
        onMappingAdded(originalId, MappingType.Item);
    }

    public static String getOriginalIDIfIDIsDummyID(String originalId) {
        return REVERSE_DUMMY_ID_LOOKUP.getOrDefault(originalId, originalId);
    }

    private static @Nullable String[] splitItemId(String itemId) {
        int pos = itemId.indexOf(':');
        if (pos < 1) return null;
        return new String[] { itemId.substring(0, pos), itemId.substring(pos + 1) };
    }

    public static boolean isDummyItem(Item item) {
        return item instanceof DummyItem || item instanceof DummyItemBlock;
    }

    public static boolean isDummyBlock(Block block) {
        return block instanceof DummyBlock;
    }
}
