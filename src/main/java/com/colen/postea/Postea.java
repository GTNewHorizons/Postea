package com.colen.postea;

import java.util.function.Function;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

import com.colen.postea.API.BlockReplacementManager;
import com.colen.postea.Utility.BlockConversionInfo;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = Tags.MODID, version = Tags.VERSION, name = Tags.MODNAME, acceptedMinecraftVersions = "[1.7.10]")
@SuppressWarnings("unused")
public class Postea {

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {

        ModContainer activeMod = Loader.instance()
            .activeModContainer();
        String modID = activeMod.getModId();
        String v = activeMod.getVersion();

        Function<BlockConversionInfo, BlockConversionInfo> blockTransformer = (blockConversionInfoOld) -> {

            BlockConversionInfo blockConversionInfoNew = new BlockConversionInfo();

            if ((blockConversionInfoOld.x == -1) && (blockConversionInfoOld.y == 3)
                && (blockConversionInfoOld.z == -1)) {
                blockConversionInfoNew.blockID = Block.getIdFromBlock(Blocks.wool);
                blockConversionInfoNew.metadata = 1;
                return blockConversionInfoNew;
            }

            if ((blockConversionInfoOld.x == -1) && (blockConversionInfoOld.y == 3)
                && (blockConversionInfoOld.z == 1)) {
                blockConversionInfoNew.blockID = Block.getIdFromBlock(Blocks.wool);
                blockConversionInfoNew.metadata = 1;
                return blockConversionInfoNew;
            }

            if ((blockConversionInfoOld.x == 1) && (blockConversionInfoOld.y == 3) && (blockConversionInfoOld.z == 1)) {
                blockConversionInfoNew.blockID = Block.getIdFromBlock(Blocks.wool);
                blockConversionInfoNew.metadata = 1;
                return blockConversionInfoNew;
            }

            if ((blockConversionInfoOld.x == 1) && (blockConversionInfoOld.y == 3)
                && (blockConversionInfoOld.z == -1)) {
                blockConversionInfoNew.blockID = Block.getIdFromBlock(Blocks.wool);
                blockConversionInfoNew.metadata = 1;
                return blockConversionInfoNew;
            }

            blockConversionInfoNew.blockID = blockConversionInfoOld.blockID;
            blockConversionInfoNew.metadata = blockConversionInfoOld.metadata;

            return blockConversionInfoNew;
        };

        BlockReplacementManager.addBlockReplacement("minecraft:grass", blockTransformer);
    }

}
