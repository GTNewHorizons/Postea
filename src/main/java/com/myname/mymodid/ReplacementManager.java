package com.myname.mymodid;


import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class ReplacementManager {
    private static final Map<ItemStackKey, ItemStack> replacementMap = new HashMap<>();

    public static void addReplacement(String modId, String itemName, int meta, ItemStack replacement) {
        replacementMap.put(new ItemStackKey(modId, itemName, meta), replacement);
    }

    public static ItemStack getReplacement(ItemStack original) {
        return replacementMap.getOrDefault(new ItemStackKey(original), original);
    }
}

