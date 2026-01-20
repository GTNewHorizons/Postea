package com.gtnewhorizons.postea.utility;

import java.util.Collection;
import java.util.function.Function;

import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;

import com.gtnewhorizons.postea.api.IDExtenderCompat;

import cpw.mods.fml.common.registry.GameRegistry;

public abstract class ItemFixerUtility {

    public static void fixItemStack(NBTTagCompound tag) {

        if (tag.hasNoTags()) return;

        if (tag.hasKey("id")) {
            int id = IDExtenderCompat.getItemStackID(tag);
            Item item = Item.getItemById(id);
            if (item == null) return;
            GameRegistry.UniqueIdentifier uuid = GameRegistry.findUniqueIdentifierFor(item);
            String itemNameInternal = uuid.modId + ":" + uuid.name;

            // abort early if no handlers
            Collection<Function<NBTTagCompound, NBTTagCompound>> handlers = TransformerRegistry
                .getItemReplacement(itemNameInternal);
            if (handlers.isEmpty()) return;

            // apply handlers
            for (Function<NBTTagCompound, NBTTagCompound> transformer : handlers) {
                if (transformer.apply(tag) != null) {
                    return;
                }
            }

            // if no handler overwrote anything on this stack, and it's a dummy, delete the stack.
            if (MissingMappingHandler.isDummyItem(item)) {
                tag.removeTag("id");
                tag.removeTag("idExt");
            }
        }
    }
}
