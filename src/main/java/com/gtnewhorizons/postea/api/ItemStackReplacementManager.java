package com.gtnewhorizons.postea.api;

import java.util.function.Function;

import net.minecraft.nbt.NBTTagCompound;

import com.gtnewhorizons.postea.utility.MissingMappingHandler;
import com.gtnewhorizons.postea.utility.TransformerRegistry;

public class ItemStackReplacementManager {

    // Public API for converting ItemStacks.

    /**
     * Adds a custom transformer for a given id.
     *
     * @param itemName    The id of the block to transform.
     * @param transformer The transformer to apply.
     */
    @SuppressWarnings("unused")
    public static void addItemReplacement(String itemName, Function<NBTTagCompound, NBTTagCompound> transformer) {
        MissingMappingHandler.createDummyItemIfNeeded(itemName);
        TransformerRegistry.addItemReplacement(itemName, transformer);
    }
}
