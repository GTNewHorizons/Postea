package com.gtnewhorizons.postea.api;

import static com.gtnewhorizons.postea.Postea.LOG;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;

public class ItemStackReplacementManager {

    private static final Map<String, Function<NBTTagCompound, NBTTagCompound>> replacementMap = new HashMap<>();

    // Public API for converting ItemStacks.

    @SuppressWarnings("unused")
    public static void addItemReplacement(String itemName, Function<NBTTagCompound, NBTTagCompound> transformer) {
        Objects.requireNonNull(itemName, "itemName must not be null");
        Objects.requireNonNull(transformer, "transformer must not be null");

        Function<NBTTagCompound, NBTTagCompound> existing = replacementMap.putIfAbsent(itemName, transformer);

        if (existing != null) {
            throw new IllegalStateException("Postea ItemStack replacement already registered for '" + itemName + "'.");
        }

        LOG.info("ItemStack replacement registered successfully for {}", itemName);
    }

    public static @Nullable Function<NBTTagCompound, NBTTagCompound> getItemReplacement(String itemName) {
        Objects.requireNonNull(itemName, "itemName must not be null");
        return replacementMap.get(itemName);
    }
}
