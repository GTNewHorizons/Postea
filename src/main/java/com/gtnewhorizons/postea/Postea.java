package com.gtnewhorizons.postea;

import com.gtnewhorizons.postea.utility.MissingMappingHandler;
import com.gtnewhorizons.postea.utility.TransformerRegistry;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLMissingMappingsEvent;
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
        TransformerRegistry.onIdMappingsChanged();
    }

    @Mod.EventHandler
    public void onMissingMapping(FMLMissingMappingsEvent event) {
        MissingMappingHandler.onMissingMapping(event);
    }

    @Mod.EventHandler
    public void onLoadCompleted(FMLLoadCompleteEvent event) {
        TransformerRegistry.onLoadCompleted();
        MissingMappingHandler.onLoadCompleted();
    }

    @Mod.EventHandler
    public void onServerStopping(FMLServerStoppingEvent event) {
        TransformerRegistry.onServerStopping();
    }

}
