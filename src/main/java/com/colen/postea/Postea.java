package com.colen.postea;

import akka.japi.Pair;
import com.colen.postea.API.BlockKey;
import com.colen.postea.API.BlockReplacementManager;
import com.colen.postea.API.ChunkFixerUtility;
import com.colen.postea.API.TileEntityReplacementManager;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

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
/*        Function<NBTTagCompound, NBTTagCompound> myTransformer = (tag) -> {
            if (tag.getInteger("m") == 86) {
                tag.setInteger("m", 323);
                return tag;
            }
            return tag;
        };*/

        //TileEntityReplacementManager.tileEntityFixer("GT_TileEntity_Ores", myTransformer);
        TileEntityReplacementManager.tileEntityTransformer("GT_TileEntity_Ores", (tag, world) -> {
            if (world.provider.dimensionId == -1) {
                return new ChunkFixerUtility.BlockInfo(Blocks.wool, 1);
            }

            if (world.provider.dimensionId == 1) {
                return new ChunkFixerUtility.BlockInfo(Blocks.wool, 2);
            }

            return new ChunkFixerUtility.BlockInfo(Blocks.wool, 3);
        });




/*        TileEntityReplacementManager.tileEntityTransformer("Furnace", (tag) -> {

            Function<NBTTagCompound, NBTTagCompound> chestTransformer = (oldTag) -> {

                NBTTagCompound newTag = cleanseNBT("Chest", oldTag);

                NBTTagList tagList = new NBTTagList();

                NBTTagCompound stoneAtSlot13 = new NBTTagCompound();
                stoneAtSlot13.setByte("Count", (byte) 1);
                stoneAtSlot13.setByte("Slot", (byte) 13);
                stoneAtSlot13.setShort("Damage", (short) 0);
                stoneAtSlot13.setShort("id", (short) 1);

                tagList.appendTag(stoneAtSlot13);

                newTag.setTag("Items", tagList);

                return newTag;
            };

            return new ChunkFixerUtility.BlockInfo(Blocks.chest, 0, chestTransformer);
        });*/

        TileEntityReplacementManager.tileEntityTransformer("Chest", (tag, world) -> {

            Function<NBTTagCompound, NBTTagCompound> chestTransformer = (oldTag) -> {

                NBTTagCompound newTag = cleanseNBT("Chest", oldTag);

                NBTTagList tagList = new NBTTagList();

                NBTTagCompound stoneAtSlot12 = new NBTTagCompound();
                stoneAtSlot12.setByte("Count", (byte) 2);
                stoneAtSlot12.setByte("Slot", (byte) 12);
                stoneAtSlot12.setShort("Damage", (short) 0);
                stoneAtSlot12.setShort("id", (short) 1);

                NBTTagCompound stoneAtSlot14 = new NBTTagCompound();
                stoneAtSlot14.setByte("Count", (byte) 4);
                stoneAtSlot14.setByte("Slot", (byte) 14);
                stoneAtSlot14.setShort("Damage", (short) 3);
                stoneAtSlot14.setShort("id", (short) 5);

                tagList.appendTag(stoneAtSlot12);
                tagList.appendTag(stoneAtSlot14);

                newTag.setTag("Items", tagList);

                return newTag;
            };

            return new ChunkFixerUtility.BlockInfo(Blocks.chest, 0, chestTransformer);
        });

        Function<BlockKey, Pair<Integer, Integer>> blockTransformer = (blockKey) -> {
            return new Pair<>(7, 0);
        };

        BlockReplacementManager.addBlockReplacement("minecraft:grass", 0, blockTransformer);

    }

    private NBTTagCompound cleanseNBT(String newTileEntityID, NBTTagCompound tag) {
        // Cleans the NBT, so that it replaces the ID and adds the x, y, z coordinates.
        NBTTagCompound tagCompound = new NBTTagCompound();

        tagCompound.setString("id", newTileEntityID);
        tagCompound.setInteger("x", tag.getInteger("x"));
        tagCompound.setInteger("y", tag.getInteger("y"));
        tagCompound.setInteger("z", tag.getInteger("z"));

        return tagCompound;
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        //BlockReplacementManager.addBlockReplacement(GregTech_API.sBlockOres1, 0, Blocks.chest, 0);
    }
}
