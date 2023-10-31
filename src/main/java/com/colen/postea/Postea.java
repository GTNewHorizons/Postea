package com.colen.postea;

import static com.colen.postea.Utility.PosteaUtilities.cleanseNBT;

import java.util.function.Function;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import com.colen.postea.API.BlockReplacementManager;
import com.colen.postea.API.ItemStackReplacementManager;
import com.colen.postea.API.TileEntityReplacementManager;
import com.colen.postea.Utility.BlockConversionInfo;
import com.colen.postea.Utility.BlockInfo;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = Tags.MODID, version = Tags.VERSION, name = Tags.MODNAME, acceptedMinecraftVersions = "[1.7.10]")
@SuppressWarnings("unused")
public class Postea {

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {

        // This is an example of replacing GT ore blocks depending on the dimension. Because BlockInfo has no TileEntity
        // transformer set, it will delete the underlying tile entity fully. This will essentially convert a TE block to
        // a non-TE block.
        // You can use the data in the tile to determine this conversion from the "tag" variable.
        TileEntityReplacementManager.tileEntityTransformer("GT_TileEntity_Ores", (tag, world) -> {
            if (world.provider.dimensionId == -1) {
                return new BlockInfo(Blocks.wool, 1);
            }

            if (world.provider.dimensionId == 1) {
                return new BlockInfo(Blocks.wool, 2);
            }

            return new BlockInfo(Blocks.wool, 3);
        });

        // This is an example of how to replace another tile entity and it's block. Here we are replacing furnaces with
        // chests.
        // It will then also replace the furnace block with a chest. Otherwise the Tile would not match the block.
        TileEntityReplacementManager.tileEntityTransformer("Furnace", (tag, world) -> {

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

            return new BlockInfo(Blocks.chest, 0, chestTransformer);
        });

        // This is an example of how to change the data of a specific tile entity. You provide the Tiles ID e.g. Chest
        // and then provide a conversion function. In this instance we are changing the contents of a chest.
        // This will replace the contents fully with stone and jungle planks at specific slots.
        TileEntityReplacementManager.tileEntityTransformer("Chest", (tag, world) -> {

            Function<NBTTagCompound, NBTTagCompound> chestTransformer = (oldTag) -> {

                NBTTagCompound newTag = cleanseNBT("Chest", oldTag);

                NBTTagList tagList = new NBTTagList();

                NBTTagCompound stoneAtSlot12 = new NBTTagCompound();
                stoneAtSlot12.setByte("Count", (byte) 2);
                stoneAtSlot12.setByte("Slot", (byte) 12);
                stoneAtSlot12.setShort("Damage", (short) 0);
                stoneAtSlot12.setShort("id", (short) Block.getIdFromBlock(Blocks.stone));

                NBTTagCompound stoneAtSlot14 = new NBTTagCompound();
                stoneAtSlot14.setByte("Count", (byte) 4);
                stoneAtSlot14.setByte("Slot", (byte) 14);
                stoneAtSlot14.setShort("Damage", (short) 3);
                stoneAtSlot14.setShort("id", (short) Block.getIdFromBlock(Blocks.planks));

                tagList.appendTag(stoneAtSlot12);
                tagList.appendTag(stoneAtSlot14);

                newTag.setTag("Items", tagList);

                return newTag;
            };

            return new BlockInfo(Blocks.chest, 0, chestTransformer);
        });

        // Example of how to replace a non-TE block with another non-TE block, this replaces grass with wool.
        // It converts every grass block with an even x coordinate to orange wool.
        Function<BlockConversionInfo, BlockConversionInfo> blockTransformer = (blockConversionInfoOld) -> {

            BlockConversionInfo blockConversionInfoNew = new BlockConversionInfo();

            if ((blockConversionInfoOld.x % 2 == 0)) {
                blockConversionInfoNew.blockID = Block.getIdFromBlock(Blocks.wool);
                blockConversionInfoNew.metadata = 1;
            } else {
                blockConversionInfoNew.blockID = blockConversionInfoOld.blockID;
                blockConversionInfoNew.metadata = blockConversionInfoOld.metadata;
            }

            return blockConversionInfoNew;
        };

        BlockReplacementManager.addBlockReplacement("minecraft:grass", blockTransformer);

        // Example of how to replace an item, this replaces iron ingots with gold ingots.
        // You can manipulate stack size but it isn't recommended.
        // You can also use the tag associated with this ItemStack to determine the replacement.
        Function<NBTTagCompound, NBTTagCompound> itemTransformer = (tag) -> {
            tag.setShort("id", (short) Item.getIdFromItem(Items.gold_ingot));
            return tag;
        };

        ItemStackReplacementManager.addItemReplacement("minecraft:iron_ingot", itemTransformer);

    }
}
