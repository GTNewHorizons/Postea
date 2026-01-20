package com.gtnewhorizons.postea.api;

import java.util.Collection;
import java.util.function.Function;

import net.minecraft.nbt.NBTTagCompound;

import com.google.common.collect.LinkedListMultimap;
import com.gtnewhorizons.postea.PosteaMissingMappingHandler;

public class ItemStackReplacementManager {

    private static final LinkedListMultimap<String, Function<NBTTagCompound, NBTTagCompound>> replacementMap = LinkedListMultimap
        .create();

    // Public API for converting ItemStacks.

    @SuppressWarnings("unused")
    public static void addItemReplacement(String itemName, Function<NBTTagCompound, NBTTagCompound> transformer) {
        replacementMap.put(PosteaMissingMappingHandler.createDummyItemIfNeeded(itemName), transformer);
    }

    @SuppressWarnings("unused")
    public static Collection<Function<NBTTagCompound, NBTTagCompound>> getItemReplacement(String itemNameInternal) {
        return replacementMap.get(itemNameInternal);
    }

}
