package com.myname.mymodid;


import net.minecraft.item.Item;

import java.util.HashMap;
import java.util.Map;

public class ReplacementManager {
    private static final Map<ItemStackKey, ItemStackKey> replacementMap = new HashMap<>();

    public static void addReplacement(Item itemIn, int metaIn, Item itemOut, int metaOut) {
        replacementMap.put(new ItemStackKey(itemIn, metaIn), new ItemStackKey(itemOut, metaOut));
    }

    public static ItemStackKey getReplacement(Item itemIn, int metaIn) {
        ItemStackKey stackKey = new ItemStackKey(itemIn, metaIn);
        return replacementMap.getOrDefault(stackKey, stackKey);
    }
}

