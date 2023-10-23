package com.colen.postea.API;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.item.Item;

public class ItemStackReplacementManager {

    private static final Map<ItemStackKey, ItemStackKey> replacementMap = new HashMap<>();

    // Public API for adding replacement ItemStacks.

    @SuppressWarnings("unused")
    public static void addItemReplacement(Item itemIn, int metaIn, Item itemOut, int metaOut) {
        replacementMap.put(new ItemStackKey(itemIn, metaIn), new ItemStackKey(itemOut, metaOut));
    }

    @SuppressWarnings("unused")
    public static ItemStackKey getItemReplacement(Item itemIn, int metaIn) {
        return replacementMap.getOrDefault(new ItemStackKey(itemIn, metaIn), null);
    }
}
