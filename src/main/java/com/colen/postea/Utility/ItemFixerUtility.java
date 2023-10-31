package com.colen.postea.Utility;

import java.util.function.Function;

import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;

import com.colen.postea.API.ItemStackReplacementManager;

import cpw.mods.fml.common.registry.GameRegistry;

public abstract class ItemFixerUtility {

    public static void fixItemStack(NBTTagCompound tag) {

        if (tag.hasNoTags()) return;

        if (tag.hasKey("id")) {
            short id = tag.getShort("id");
            Item item = Item.getItemById(id);
            String itemNameInternal = GameRegistry.findUniqueIdentifierFor(item).modId + ":"
                + GameRegistry.findUniqueIdentifierFor(item).name;

            Function<NBTTagCompound, NBTTagCompound> transformer = ItemStackReplacementManager
                .getItemReplacement(itemNameInternal);

            if (transformer != null) {
                transformer.apply(tag);
            }
        }
    }
}
