package com.colen.postea;

import com.colen.postea.API.BlockReplacementManager;
import com.colen.postea.Utility.BlockConversionInfo;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

import java.util.function.BiFunction;
import java.util.function.Function;

import static com.colen.postea.API.BlockReplacementManager.blockReplacementMap;
import static com.colen.postea.API.BlockReplacementManager.posteaMarkedIDs;

@Mod(modid = Tags.MODID, version = Tags.VERSION, name = Tags.MODNAME, acceptedMinecraftVersions = "[1.7.10]")
@SuppressWarnings("unused")
public class Postea {

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {

    }

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
