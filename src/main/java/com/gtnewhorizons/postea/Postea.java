package com.gtnewhorizons.postea;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.gtnewhorizons.postea.utility.ChunkFixerUtility;
import com.gtnewhorizons.postea.utility.IDRegistry;
import com.gtnewhorizons.postea.utility.MissingMappingHandler;
import com.gtnewhorizons.postea.utility.SimpleTransformationRegistry;
import com.gtnewhorizons.postea.utility.TransformerRegistry;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLMissingMappingsEvent;
import cpw.mods.fml.common.event.FMLModIdMappingEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

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

    public static final Logger LOG = LogManager.getLogger(MODNAME);

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Mod.EventHandler
    public void postsLoad(FMLPostInitializationEvent event) {
        // TestUIEMigrator.postLoad();
    }

    @SubscribeEvent
    public void chunkLoaded(ChunkEvent.Load event) {
        ChunkFixerUtility.onChunkLoaded(event.getChunk());
    }

    @Mod.EventHandler
    public void onIdMappingsChanged(FMLModIdMappingEvent event) {
        SimpleTransformationRegistry.onIdMappingsChanged();
        TransformerRegistry.onIdMappingsChanged();
        IDRegistry.onMappingUpdated();
    }

    @Mod.EventHandler
    public void onMissingMapping(FMLMissingMappingsEvent event) {
        MissingMappingHandler.onMissingMapping(event);
    }

    @Mod.EventHandler
    public void onLoadCompleted(FMLLoadCompleteEvent event) {
        SimpleTransformationRegistry.onLoadCompleted();
    }
}
