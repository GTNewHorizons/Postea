package com.gtnewhorizons.postea.api;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import net.minecraft.nbt.NBTTagCompound;

public class ItemStackReplacementManager {

    private static final Map<String, Function<NBTTagCompound, NBTTagCompound>> replacementMap = new HashMap<>();

    // Public API for converting ItemStacks.

    @SuppressWarnings("unused")
    public static void addItemReplacement(String itemName, Function<NBTTagCompound, NBTTagCompound> transformer) {
        replacementMap.put(itemName, transformer);
    }

    @SuppressWarnings("unused")
    public static Function<NBTTagCompound, NBTTagCompound> getItemReplacement(String itemNameInternal) {
        return replacementMap.get(itemNameInternal);
    }
}
