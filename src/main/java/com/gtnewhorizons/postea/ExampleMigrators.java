package com.gtnewhorizons.postea;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.oredict.OreDictionary;

import com.gtnewhorizons.postea.api.BlockAccessCompat;
import com.gtnewhorizons.postea.api.BlockReplacementManager;
import com.gtnewhorizons.postea.api.ChunkTransformContext;
import com.gtnewhorizons.postea.api.IDExtenderCompat;
import com.gtnewhorizons.postea.api.IVersionedTransformer;
import com.gtnewhorizons.postea.api.ItemStackReplacementManager;
import com.gtnewhorizons.postea.api.PlayerDataTransformContext;
import com.gtnewhorizons.postea.api.TileEntityReplacementManager;
import com.gtnewhorizons.postea.api.VersionedReplacementManager;
import com.gtnewhorizons.postea.utility.BlockConversionInfo;
import com.gtnewhorizons.postea.utility.BlockInfo;

import cpw.mods.fml.common.registry.GameRegistry;

abstract class ExampleMigrators {

    static void postLoad() {
        TEToBlockExample.postLoad();
        TEModifyExample.postLoad();
        TEFullReplacementExample.postLoad();
        SimpleTransformationExample.postLoad();
        ComplexBlockTransformer.postLoad();
        ComplexItemTransformer.postLoad();
        FMLReMappingExample.postLoad();
        VersionedExample.postLoad();
    }

    public static abstract class TEToBlockExample {

        public static void postLoad() {
            // it's with great shame that we do admit that gregtech ores used to be tile entities.
            TileEntityReplacementManager.tileEntityTransformer("GT_TileEntity_Ores", (tag, world, chunk) -> {
                if (world.provider.dimensionId == -1) {
                    // nether gets orange wool
                    return new BlockInfo(Blocks.wool, 1);
                }
                if (world.provider.dimensionId == 1) {
                    // the end gets magenta wool
                    return new BlockInfo(Blocks.wool, 2);
                }
                // all other dimensions get light blue wool
                return new BlockInfo(Blocks.wool, 3);
            });
        }
    }

    public static abstract class TEModifyExample {

        private static int reedsId = -1;
        private static int diamondId = -1;
        private static int stoneId = -1;
        private static int record11Id = -1;

        public static void postLoad() {
            // pre caching ids like this will lead to faster transformers
            ItemStackReplacementManager.registerIDResolver("minecraft:reeds", i -> reedsId = i);
            ItemStackReplacementManager.registerIDResolver("minecraft:diamond", i -> diamondId = i);
            ItemStackReplacementManager.registerIDResolver("minecraft:stone", i -> stoneId = i);
            TileEntityReplacementManager.tileEntityTransformer("Chest", (tag, world, chunk) -> {
                // This transformer replaces all sugar cane stacks inside a chest with
                // stone, unless it's in the 13th slot at which point it's replaced with
                // diamonds. All transformations are done without affecting the stack size.
                NBTTagList slots = tag.getTagList("Items", 10);
                for (int i = 0; i < slots.tagCount(); i++) {
                    NBTTagCompound slot = slots.getCompoundTagAt(i);
                    // only target the removed item
                    if (IDExtenderCompat.getItemStackID(slot) != reedsId) continue;
                    if (slot.getByte("Slot") == 13) {
                        // replace it with diamonds if it's in slot 13
                        IDExtenderCompat.setItemStackID(slot, diamondId);
                    } else {
                        // else replace it with stone.
                        IDExtenderCompat.setItemStackID(slot, stoneId);
                    }
                }
                return null;
            });

            // this tile entity transformer also executes because the other transformer returned null
            ItemStackReplacementManager.registerIDResolver("minecraft:record_11", i -> record11Id = i);
            byte targetSLot = (byte) 12;
            TileEntityReplacementManager.tileEntityTransformer("Chest", (tag, world, chunk) -> {
                // This transformer looks for the 12th slot of the chest and replaces
                // or sets it's content to a new stack of record 11 with a stack size of 1.
                NBTTagList slots = tag.getTagList("Items", 10);
                NBTTagCompound toEdit = null;
                for (int i = 0; i < slots.tagCount(); i++) {
                    NBTTagCompound slot = slots.getCompoundTagAt(i);
                    if (slot.getByte("Slot") != targetSLot) continue;
                    toEdit = slot;
                    break;
                }
                if (toEdit == null) {
                    toEdit = new NBTTagCompound();
                    toEdit.setByte("Slot", targetSLot);
                    slots.appendTag(toEdit);
                }
                toEdit.setByte("Count", (byte) 1);
                toEdit.setShort("Damage", (short) 0);
                IDExtenderCompat.setItemStackID(toEdit, record11Id);
                return null;
            });
        }
    }

    public static abstract class TEFullReplacementExample {

        public static int litFurnaceId = -1;
        public static int unlitFurnaceId = -1;
        public static int fireChargeId = -1;

        public static void postLoad() {
            BlockReplacementManager.registerIDResolver("minecraft:lit_furnace", i -> litFurnaceId = i);
            BlockReplacementManager.registerIDResolver("minecraft:furnace", i -> unlitFurnaceId = i);
            ItemStackReplacementManager.registerIDResolver("minecraft:fire_charge", i -> fireChargeId = i);
            TileEntityReplacementManager.tileEntityTransformer("Furnace", TEFullReplacementExample::transformFurnace);
        }

        /**
         * This transformer transforms a furnace into a chest, and adds a fire charge in the 4th slot of the chest if
         * the block where the tile entity once stood was a lit furnace. If it's not either a lit or unlit furnace,
         * the block is simply transformed into air.
         *
         * @param tag   The NBT data of the tile entity being transformed.
         * @param world The world in which the tile entity is located.
         * @param chunk The chunk in which the tile entity is located.
         * @return Null if we didn't update the block, else a BlockInfo with the transformation data you wish to
         *         execute.
         */
        private static BlockInfo transformFurnace(NBTTagCompound tag, World world, Chunk chunk) {
            int[] blockIdAndMeta = BlockAccessCompat.getBlockIDAndMetaAtTE(tag, chunk);
            int blockId = blockIdAndMeta[0];
            int meta = blockIdAndMeta[1];
            // we return a value this time because we are actually replacing the block and tile entity.
            // we copy the meta value since that preserves the orientation for these blocks.
            return new BlockInfo(Blocks.chest, meta, oldTag -> transformFurnaceNBT(oldTag, blockId));
        }

        /**
         * Moves the content of the furnace to the chest and adds a fire charge in the 4th slot if the furnace was lit.
         *
         * @param oldTag The current NBT data ofthe tile entity being transformed.
         * @return null if we want the tile entity to be erased.
         */
        private static NBTTagCompound transformFurnaceNBT(NBTTagCompound oldTag, int blockId) {
            // if the block wasn't a furnace, clear the current tile entity and let the chest spawn a new one.
            if (blockId != litFurnaceId && blockId != unlitFurnaceId) {
                return null;
            }
            // use this function to create a new tile entity tag with the same positional data as the old one.
            NBTTagCompound newTag = TileEntityReplacementManager.createTETagAtSamePosition("Chest", oldTag);
            NBTTagList newSlots = (NBTTagList) oldTag.getTagList("Items", 10)
                .copy();
            // if the block used to be a lit furnace add a fire charge in the 4th slot.
            if (blockId == litFurnaceId) {
                NBTTagCompound fireChargeStack = new NBTTagCompound();
                IDExtenderCompat.setItemStackID(fireChargeStack, fireChargeId);
                fireChargeStack.setByte("Slot", (byte) 4);
                fireChargeStack.setByte("Count", (byte) 1);
                fireChargeStack.setShort("Damage", (short) 0);
                newSlots.appendTag(fireChargeStack);
            }
            newTag.setTag("Items", newSlots);
            return newTag;
        }
    }

    public static abstract class SimpleTransformationExample {

        public static void postLoad() {
            // item/block -> item/block, preserving the Meta/Damage value.
            //
            // remap all block and items of colored stone blocks with wool of the same color
            BlockReplacementManager.addSimpleReplacement("ExtraUtilities:colorStoneBrick", Blocks.wool);

            // Item/Block with meta -> Item/Block, meta preserved
            //
            // angel ring:3 to disk 11
            ItemStackReplacementManager
                .addSimpleReplacement("ExtraUtilities:angelRing", 3, new ItemStack(Items.record_11));
            ItemStackReplacementManager
                .addSimpleReplacement("ExtraUtilities:angelRing", 4, new ItemStack(Items.record_13));

            // Item/Block with meta -> Item/Block with specific meta
            //
            // remap all compressed cobble to oak planks
            //
            // When using a stack as a target the passed stack size is ignored, only the contained item and meta value
            // are kept.
            ItemStackReplacementManager
                .addSimpleReplacement("ExtraUtilities:cobblestone_compressed", 0, new ItemStack(Blocks.planks, 0, 0));

            // block only with meta -> block with specific meta value.
            //
            // remap blocks of double compressed cobble to cobble.
            BlockReplacementManager
                .addSimpleReplacement("ExtraUtilities:cobblestone_compressed", 1, Blocks.cobblestone, 0, true);
            // item only with damage -> item only with specific damage value.
            //
            // remap stacks of double compressed cobble to oak saplings.
            ItemStackReplacementManager.addSimpleReplacement(
                "ExtraUtilities:cobblestone_compressed",
                1,
                new ItemStack(Blocks.sapling, 1, 0),
                true);

            // Block/Item with any Meta/Damage -> Block/Item with specific Meta/Damage value.
            //
            // Using OreDictionary.WILDCARD_VALUE will tell postea to use this mapping as a fallback when there is no
            // transformer targeting the specific meta/damage value of the block/item.
            //
            // remap all other instances of compressed blocks to endstone
            BlockReplacementManager.addSimpleReplacement(
                "ExtraUtilities:cobblestone_compressed",
                OreDictionary.WILDCARD_VALUE,
                Blocks.end_stone,
                0);

            // Block/Item with any Meta/Damage -> Block/Item, preserving the Meta/Damage value.
            //
            // When passing OreDictionary.WILDCARD_VALUE as the target Meta/Damage value, Postea will preserve any
            // existing
            // Meta/Damage value. This is also the default value when you don't specify a target meta value.
            //
            // remap all other instances of compressed blocks to endstone
            BlockReplacementManager.addSimpleReplacement(
                "ExtraUtilities:cobblestone_compressed",
                OreDictionary.WILDCARD_VALUE,
                Blocks.end_stone,
                OreDictionary.WILDCARD_VALUE);

        }
    }

    public static abstract class ComplexBlockTransformer {

        private static int woolId = -1;

        public static void postLoad() {
            // This transformer replaces all grass blocks with even x-coordinates with orange wool.
            BlockReplacementManager.registerIDResolver("minecraft:wool", i -> woolId = i);
            BlockReplacementManager
                .addTransformationHandler("minecraft:grass", ComplexBlockTransformer::transformGrass);
        }

        private static boolean transformGrass(BlockConversionInfo info) {
            // only affect blocks that are on even x coordinates
            if (info.x % 2 != 0) return false;
            // transform all these blocks into orange wool.
            info.blockID = woolId;
            info.metadata = 1;
            return true;
        }

    }

    public static abstract class ComplexItemTransformer {

        private static int writableBookId = -1;
        private static int woolId = -1;

        private static void postLoad() {
            ItemStackReplacementManager.registerIDResolver("minecraft:writable_book", i -> writableBookId = i);
            ItemStackReplacementManager.registerIDResolver("minecraft:wool", i -> woolId = i);
            ItemStackReplacementManager
                .addTransformationHandler("minecraft:written_book", ComplexItemTransformer::unsignBook);
        }

        /**
         * This transformer transforms a written book into either an unsigned book or orange wool depending on it's
         * title.
         *
         * @param name The name of the item being transformed, even if that item doesn't exist anymore.
         * @param nbt  The NBT data of the stack item being transformed.
         * @return True if we've actually transformed the item.
         */
        private static boolean unsignBook(String name, NBTTagCompound nbt) {
            // check if the book has a title.
            if (!nbt.hasKey("tag", 10)) return false;
            NBTTagCompound tag = nbt.getCompoundTag("tag");
            if (!tag.hasKey("title", 8)) return false;
            String title = tag.getString("title");
            if (title.equalsIgnoreCase("unsign me")) {
                // unsign the book
                IDExtenderCompat.setItemStackID(nbt, writableBookId);
                nbt.setShort("Damage", (short) 0);
                nbt.setByte("Count", (byte) 1);
                // clear the signed book nbt
                tag.removeTag("title");
                tag.removeTag("author");
                return true;
            } else if (title.equalsIgnoreCase("orange wool")) {
                // Transform it into orange wool.
                IDExtenderCompat.setItemStackID(nbt, woolId);
                nbt.setShort("Damage", (short) 1);
                nbt.setByte("Count", (byte) 1);
                // removes the NBT tag from the stack.
                nbt.removeTag("tag");
                return true;
            }
            return false;
        }

    }

    public static abstract class FMLReMappingExample {

        // You have to make your own block class since the block constructor is protected.
        public static class MyBlock extends Block {

            protected MyBlock() {
                super(Material.ground);
            }
        }

        public static void postLoad() {
            // this only works when the thing you're replacing with has never been registered to a world's
            // id map. If you try to run this with a block that already exists (eg good old dirt), FML error out and
            // you that your world is corrupted beyond repair, and advise you to undo what ever you just did.
            //
            // Postea Simple Transformers don't have that issue and are more lag friendly in the long term. Consider
            // using them when ever possible, instead of FML mappings.
            Item item = new Item();
            GameRegistry.registerItem(item, "my_item");
            ItemStackReplacementManager.replaceMissingMapping("IC2:itemFertilizer", item);

            Block block = new MyBlock();
            GameRegistry.registerBlock(block, "my_block");
            BlockReplacementManager.replaceMissingMapping("IC2:blockCrop", block);
        }
    }

    public static abstract class FMLIgnoreMissingMappingExample {

        public static void postLoad() {
            // It doesn't really matter which one you use, they both do the same thing under the hood.
            ItemStackReplacementManager.ignoreMissingMapping("IC2:itemFertilizer");
            BlockReplacementManager.ignoreMissingMapping("IC2:blockCrop");
        }
    }

    public static final class VersionedExample implements IVersionedTransformer {

        public static void postLoad() {
            VersionedReplacementManager.register(new VersionedExample());
        }

        @Override
        public String key() {
            return "examplemod:gemIndex";
        }

        // The version the running mod writes gems under; bump it whenever the index assignment changes.
        @Override
        public int currentVersion() {
            return 2;
        }

        @Override
        public void transformChunk(ChunkTransformContext ctx) {
            // Data saved before this transformer existed carries no stamp.
            if (ctx.storedVersion() == ChunkTransformContext.UNSTAMPED || ctx.storedVersion() == 1) {
                int gemBlockId = Block.getIdFromBlock(Blocks.wool);
                ctx.forEachBlock((x, y, z, id, meta) -> { if (id == gemBlockId) ctx.setBlock(x, y, z, id, meta + 1); });
                ctx.forEachItemStackTag(stack -> {
                    if (IDExtenderCompat.getItemStackID(stack) == Item.getIdFromItem(Items.dye)) {
                        stack.setShort("Damage", (short) (stack.getShort("Damage") + 1));
                    }
                });
            }
        }

        @Override
        public void transformPlayer(PlayerDataTransformContext ctx) {
            if (ctx.storedVersion() == PlayerDataTransformContext.UNSTAMPED || ctx.storedVersion() == 1) {
                ctx.forEachItemStackTag(stack -> {
                    if (IDExtenderCompat.getItemStackID(stack) == Item.getIdFromItem(Items.dye)) {
                        stack.setShort("Damage", (short) (stack.getShort("Damage") + 1));
                    }
                });
            }
        }
    }
}
