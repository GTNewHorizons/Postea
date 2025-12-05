package com.gtnewhorizons.postea;

import static com.gtnewhorizons.postea.api.BlockReplacementManager.blockReplacementMap;
import static com.gtnewhorizons.postea.api.BlockReplacementManager.posteaMarkedIDs;

import net.minecraft.block.Block;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLModIdMappingEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;

@Mod(
    modid = Postea.MODID,
    version = Tags.VERSION,
    name = Postea.MODNAME,
    acceptedMinecraftVersions = "[1.7.10]",
    dependencies = " required-after:gtnhlib@[0.6.21,);")
@SuppressWarnings("unused")
public class Postea {

    public static final String MODID = "postea";
    public static final String MODNAME = "Postea";

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {}

    @Mod.EventHandler
    public void onIdMappingsChanged(FMLModIdMappingEvent event) {
        posteaMarkedIDs.clear();
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
