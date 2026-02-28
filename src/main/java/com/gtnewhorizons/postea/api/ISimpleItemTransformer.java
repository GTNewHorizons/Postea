package com.gtnewhorizons.postea.api;

import net.minecraft.item.Item;

public interface ISimpleItemTransformer {

    Item getTargetItem();

    int getTargetItemMeta();
}
