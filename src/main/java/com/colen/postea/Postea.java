package com.colen.postea;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.nbt.NBTTagCompound;

import java.util.function.Function;

@Mod(modid = Tags.MODID, version = Tags.VERSION, name = Tags.MODNAME, acceptedMinecraftVersions = "[1.7.10]")
@SuppressWarnings("unused")
public class Postea {

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        //ItemStackReplacementManager.addItemReplacement(Item.getItemFromBlock(Blocks.grass), 0, Item.getItemFromBlock(Blocks.coal_ore), 10);
//        BlockReplacementManager.addBlockReplacement(GregTech_API.sBlockOres1, 6, Blocks.emerald_ore, 0);
//        BlockReplacementManager.addBlockReplacement(GregTech_API.sBlockOres1, 16022, Blocks.nether_brick, 0);
//        BlockReplacementManager.addBlockReplacement(GregTech_API.sBlockOres1, 1018, Blocks.end_stone, 0);

    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        // Define the transformation function
        Function<NBTTagCompound, NBTTagCompound> myTransformer = (tag) -> {
            return tag;
        };

//        TileEntityReplacementManager.tileEntityReplacement("GT_TileEntity_Ores", new TileTransformer(TileEntityChest.class, myTransformer));
//        BlockReplacementManager.addBlockReplacement(GregTech_API.sBlockOres1, 0, Blocks.chest, 0);
    }
}
