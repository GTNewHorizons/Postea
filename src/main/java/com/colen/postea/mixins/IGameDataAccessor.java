package com.colen.postea.mixins;

import cpw.mods.fml.common.registry.FMLControlledNamespacedRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.Item;

public interface IGameDataAccessor {
    FMLControlledNamespacedRegistry<Block> getIBlockRegistry();
    FMLControlledNamespacedRegistry<Item> getIItemRegistry();
}
