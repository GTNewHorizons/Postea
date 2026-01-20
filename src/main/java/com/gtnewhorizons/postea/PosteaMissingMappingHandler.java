package com.gtnewhorizons.postea;

import java.util.HashMap;
import java.util.HashSet;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;

import cpw.mods.fml.common.event.FMLMissingMappingsEvent;
import cpw.mods.fml.common.registry.GameRegistry;

public abstract class PosteaMissingMappingHandler {

    private static final HashMap<String, Item> missingItemMappings = new HashMap<>();
    private static final HashMap<String, Block> missingBlockMappings = new HashMap<>();
    private static final HashMap<String, String> remappedBlocks = new HashMap<>();
    private static final HashSet<String> remappedItems = new HashSet<>();

    private static final String DUMMY_ID_PREFIX_START = Postea.MODID + ":";
    private static final String DUMMY_ID_PREFIX_END = "d.";
    private static final String DUMMY_ID_PREFIX = DUMMY_ID_PREFIX_START + DUMMY_ID_PREFIX_END;

    public static String createDummyBlockIfNeeded(String originalId) {
        if (remappedItems.contains(originalId))
            throw new IllegalArgumentException("block mappings must be declared before the item remapping");
        String newId = onMappingAdded(
            originalId,
            GameRegistry::findBlock,
            missingBlockMappings,
            DummyBlock::new,
            GameRegistry::registerBlock);
        // don't let it register an item jic
        if (!newId.equals(originalId)) {
            remappedBlocks.put(originalId, newId);
        }
        return newId;
    }

    public static String createDummyItemIfNeeded(String originalId) {
        if (remappedBlocks.containsKey(originalId)) return remappedBlocks.get(originalId);
        String newId = onMappingAdded(
            originalId,
            GameRegistry::findItem,
            missingItemMappings,
            DummyItem::new,
            GameRegistry::registerItem);
        if (!newId.equals(originalId)) remappedItems.add(newId);
        return newId;
    }

    private static <T> String onMappingAdded(String originalId, BiFunction<String, String, T> registryCheck,
        HashMap<String, T> dummyLookup, Supplier<T> dummySupplier, BiConsumer<T, String> registryConsumer) {
        // split name into parts
        int pos = originalId.indexOf(':');
        if (pos < 1) return originalId;
        final String modId = originalId.substring(0, pos);
        final String name = originalId.substring(pos + 1);
        // check if it exists
        if (registryCheck.apply(modId, name) != null) return originalId;
        // else register a fake item
        final String registerAs = DUMMY_ID_PREFIX_END + modId + "." + name;
        final String dummyFullId = DUMMY_ID_PREFIX_START + registerAs;
        // abort early if we've already registered a dummy handler for this item.
        if (dummyLookup.containsKey(dummyFullId)) return dummyFullId;
        T dummy = dummySupplier.get();
        dummyLookup.put(dummyFullId, dummy);
        dummyLookup.put(originalId, dummy);
        registryConsumer.accept(dummy, registerAs);
        return dummyFullId;
    }

    public static boolean isDummyItem(Item item) {
        return item instanceof DummyItem || item instanceof ItemBlock ib && ib.field_150939_a instanceof DummyBlock;
    }

    public static boolean isDummyBlock(Block block) {
        return block instanceof DummyBlock;
    }

    public static void onLoadCompleted() {
        for (
    }

    // we're using reflection to identify dummies quickly.
    public static class DummyBlock extends Block {

        public DummyBlock() {
            super(Material.ground);
        }
    }

    // because we're using reflection to quickly identify our items
    public static class DummyItem extends Item {
    }

    static void onMissingMapping(FMLMissingMappingsEvent event) {
        for (FMLMissingMappingsEvent.MissingMapping mapping : event.getAll()) {
            if (mapping.name.startsWith(DUMMY_ID_PREFIX)) {
                // for when a migrator ceases being registered, likely due to a removal.
                mapping.ignore();
                continue;
            }

            if (mapping.type == GameRegistry.Type.BLOCK) {
                Block remappedBlock = missingBlockMappings.get(mapping.name);
                if (remappedBlock != null) {
                    mapping.remap(remappedBlock);
                    continue;
                }
            } else if (mapping.type == GameRegistry.Type.ITEM) {
                Block remappedBlock = missingBlockMappings.get(mapping.name);
                if (remappedBlock != null) {
                    mapping.remap(Item.getItemFromBlock(remappedBlock));
                    continue;
                }
                Item remappedItem = missingItemMappings.get(mapping.name);
                if (remappedItem != null) {
                    mapping.remap(missingItemMappings.get(mapping.name));
                    continue;
                }
            }
        }
    }
}
