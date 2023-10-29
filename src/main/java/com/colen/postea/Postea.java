package com.colen.postea;

import com.colen.postea.API.BlockReplacementManager;
import com.colen.postea.API.ChunkFixerUtility;
import com.colen.postea.API.TileEntityReplacementManager;
import com.colen.postea.API.TileTransformer;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.init.Blocks;
import gregtech.api.GregTech_API;
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

        // Define the transformation function
        Function<NBTTagCompound, NBTTagCompound> myTransformer = (tag) -> {
            if (tag.getInteger("m") == 86) {
                tag.setInteger("m", 323);
                return tag;
            }
            return tag;
        };

        //TileEntityReplacementManager.tileEntityFixer("GT_TileEntity_Ores", myTransformer);
        TileEntityReplacementManager.tileEntityToNormalBlockTransformer("GT_TileEntity_Ores", (tag) -> {
            if (tag.getInteger("m") == 86) {
                return new ChunkFixerUtility.BlockInfo(Blocks.bedrock, 0);
            }

            return new ChunkFixerUtility.BlockInfo(Blocks.brick_block, 0);
        });
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        BlockReplacementManager.addBlockReplacement(GregTech_API.sBlockOres1, 0, Blocks.chest, 0);
    }
}
