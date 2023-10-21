package com.myname.mymodid;


import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.ItemStack;

import java.util.Objects;

public class ItemStackKey {
    private final String modId;
    private final String itemName;
    private final int meta;

    public ItemStackKey(String modId, String itemName, int meta) {
        this.modId = modId;
        this.itemName = itemName;
        this.meta = meta;
    }

    public ItemStackKey(ItemStack stack) {
        this(stack.getItem().getUnlocalizedName().substring(4),
            GameRegistry.findUniqueIdentifierFor(stack.getItem()).modId,
            stack.getItemDamage());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemStackKey that = (ItemStackKey) o;
        return meta == that.meta &&
            Objects.equals(modId, that.modId) &&
            Objects.equals(itemName, that.itemName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modId, itemName, meta);
    }
}

