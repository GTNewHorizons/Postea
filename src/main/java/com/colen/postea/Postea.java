package com.colen.postea;

import com.colen.postea.API.BlockReplacementManager;
import com.colen.postea.API.ItemStackReplacementManager;
import com.colen.postea.API.TileEntityReplacementManager;
import com.colen.postea.API.TileTransformer;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import gregtech.api.GregTech_API;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityFurnace;

import java.util.function.Function;

@Mod(modid = Tags.MODID, version = Tags.VERSION, name = Tags.MODNAME, acceptedMinecraftVersions = "[1.7.10]")
@SuppressWarnings("unused")
public class Postea {

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        //ItemStackReplacementManager.addItemReplacement(Item.getItemFromBlock(Blocks.grass), 0, Item.getItemFromBlock(Blocks.coal_ore), 10);
        BlockReplacementManager.addBlockReplacement(GregTech_API.sBlockOres1, 6, Blocks.emerald_ore, 0);
        BlockReplacementManager.addBlockReplacement(GregTech_API.sBlockOres1, 16022, Blocks.nether_brick, 0);
        BlockReplacementManager.addBlockReplacement(GregTech_API.sBlockOres1, 1018, Blocks.end_stone, 0);

        // Define the transformation function
        Function<NBTTagCompound, NBTTagCompound> myTransformer = (tag) -> {
            return tag;
        };

        TileEntityReplacementManager.addItemReplacement("BlockController", new TileTransformer(TileEntityChest.class, myTransformer));

    }
}
