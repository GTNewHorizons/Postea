package com.gtnewhorizons.postea;

import static com.gtnewhorizons.postea.api.BlockReplacementManager.blockReplacementMap;
import static com.gtnewhorizons.postea.api.BlockReplacementManager.posteaMarkedIDs;

import net.minecraft.block.Block;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;

@Mod(modid = Tags.MODID, version = Tags.VERSION, name = Tags.MODNAME, acceptedMinecraftVersions = "[1.7.10]")
@SuppressWarnings("unused")
public class Postea {

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {}

    @Mod.EventHandler
    public void onServerStarting(FMLServerAboutToStartEvent event) {
        for (String name : blockReplacementMap.keySet()) {
            Block block = Block.getBlockFromName(name);
            if (block != null) {
                int id = Block.getIdFromBlock(block);
                posteaMarkedIDs.add(id);
            }
        }
    }

    @Mod.EventHandler
    public void onServerStopping(FMLServerStoppingEvent event) {
        posteaMarkedIDs.clear();
    }

}
