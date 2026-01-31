# TileEntityReplacementManager API

This library provides a suite of tools for transforming any existing tile entities, blocks and items the world at
runtime. Allowing developers to replace, migrate or modify game elements based on specific conditions.

## Features

1. **Tile Entity Transformers**: Replace or modify tile entities.
2. **Block Transformers**: Replace normal blocks with another normal block.
3. **ItemStack Transformers**: Replace or Modify items.
4. **Simple Replacement API**: Efficiently replace or remap large amounts of blocks and items at scale without incurring any noticeable performance impact.
5. **Missing Mapping Replacement API**: A set of API endpoints to register actions to be taken when FML detects a missing mapping for a given ID.
6. **Numeric ID Identification for Removed Content**: A way to identify the ID of any content that has ceased to exist.

## Examples

### 1. Tile Entity to Regular Block Transformation

The `TileEntityReplacementManager` class can be used to register tile entity transformers for any tile entities with
the given ID. These transformers usually perform one of 3 types of interactions: Block replacements, NBT transformations
and full-on replacements.

When doing a block replacement, you simply just return a `BlockInfo` without specifying an NBT transformer (the third
argument of the constructor). This will replace the existing block and erase the presence of the tile entity being
transformed.

> [!NOTE]
>
> Tile entity transformers are always applied before any block transformers.

> [!TIP]
>
> Tile entity and block transformers are only ran once per chunk until a player's mod list changes. This behaviour is
> disabled if you are in a dev environment; Allowing you to test your transformers simply by leaving and rejoining your
> test world.

> [!CAUTION]
>
> Tile entity and block transformers are executed as the chunk data is being read and so the world being passed isn't
> fully loaded. **If you try to perform any kind of block access using the world parameter, forge will throw an error.**
>
> **If you need to perform any kind of block access on the block located at the same place as the tile entity being
> transformed use the `BlockAccessCompat`**. These endpoints will fetch the ID and meta value of that block without
> causing any errors. You can then compare it to a value obtained from a callback registered using
> `BlockReplacementManager.registerIDResolver` to match the block in question.
>
> If you try to use the chunk, to get a value, you may get an incorrect value if the block that was previously located
> at the location of the tile entity no longer exists. The `BlockAccessCompat` endpoints are able to overcome this
> issue and will allow you to identify block ids linked to content that no longer exists without the use of a
> FMLMissingMapping remap.

Code example:
```java
public abstract class TEToBlockExample {
    public static void postLoad() {
        // In this example, we look for any tile entity with the ID of `GT_TileEntity_Ores` and
        // replace it with are regular block with no tile entities. This is specified by returning
        // a `BlockInfo` that has no nbt transformation function.
        TileEntityReplacementManager.tileEntityTransformer("GT_TileEntity_Ores", (tag, world, chunk) -> {
            // yes, it's in great shame that we admit that Gregtech ores used to be tile entities.
            if (world.provider.dimensionId == -1) {
                //  nether gets orange wool
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
```
### 2. Tile Entity NBT Transformations

If you need to edit an existing tile entity without replacing it, simply perform the modification as usual inside the
main transformer function and return a `null` value instead of a `BlockInfo`. This will tell Postea that you haven't
done any replacements and that any other handler targeting this tile entity type should also be able to perform their
tasks.

> [!NOTE]
>
> Technically you can also return a `BlockInfo` with the same block and meta value, and a pass though function for the
> NBT transformer, _**but this is considered bad practice for pure NBT transformations**_.
>
> You should also note that when multiple tile entity transformers are registered to a single tile entity ID, they will
> always be executed in order of their registrations.

> [!TIP]
>
> It's technically more efficient to use Postea to do data migrations since your TE's own code won't have to do extra
> checks every time it reads its own NBT data.

> [!IMPORTANT]
>
> Remember that unlike items, tile entities manually store every tag of their NBT data individually, meaning that any
> additional tags will be discarded if the tile entity doesn't read it when it reads it's reloads it's state by
> reading it's stored NBT data.

Code example:
```java
public abstract class TEModifyExample {

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
                }
                else {
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
                toEdit.setByte("Slot", (byte) targetSLot);
                slots.appendTag(toEdit);
            }
            toEdit.setByte("Count", (byte) 1);
            toEdit.setShort("Damage", (short) 0);
            IDExtenderCompat.setItemStackID(toEdit, stoneId);
            return null;
        });
    }
}
```

### 3. Full Tile Entity Replacements

If you need to fully replace a tile entity, your tile entity transformer should return a `BlockInfo` that includes an
NBT transformation callback.

> [!CAUTION]
>
> When you're doing a full replacement, changing the NBT parameter outside the NBT transformation callback is considered
> to be bad practice. It's only acceptable, (and even reccomended to do so) if you're performing a pure NBT
> transformation like in the last example.

> [!TIP]
>
> If your NBT transformer returns `null`, the previous tile entity data will simply be removed. Then what ever block you
> specified can now simply create its own tile entity from scratch when it's loaded.

Code example:
```java
public abstract class TEFullReplacementExample {

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
        if (blockId != litFurnaceId && blockId != unlitFurnaceId) return null;
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
```

### 4. Simple Block and Item Replacements

If you need to transform an existing block or item into something else, Postea now features a set of API endpoints that
let you register "Simple Transformations".

When applicable, we recommend using these endpoints as they will generally result in a faster execution time than if you
were to implement your own handler. Another advantage is that multiple, completely distinct, mods can now target the
same item ID for a simple transformation with zero impact to compatibility.

Simple Transformers can be used to perform one of 4 types of transformations:
1. Some block or item with any meta or damage value -> some block or item, preserving the meta or damage value.
   - Behaves similarly to an FMLMissingMapping remap, but is more effective in the long term as it doesn't cause an ID
   aliasing.
2. Some block or item with any meta or damage value -> some block or item with a specific meta or damage value.
3. Some block or item with a specific meda or damage value -> some block or item, preserving the meta or damage value.
4. Some block or item with a specific meta or damage value -> some block or item, with a specific meta or damage value.

> [!TIP]
>
> These simple transformers can be used to target blocks and items that no longer exist in the world due to a content
> or mod removal. Postea can identify the numeric ID of any content that was once registered to a world's ID map. Even
> if postea wasn't installed when those mods were installed or removed.
>
> This also applies to the `BlockReplacementManager.registerIDResolver` and `ItemStackReplacementManager.registerIDResolver`
> functions. The callbacks registered by these function will return with a value of -1 which is impossible to get from
> the `BlockAccessCompat` functions that return block IDs, (they return 0 (air) when encountering something which isn't
> in the known ID map as a failsafe.)
>
> This means that technically it's better to use simple transformation to migrate removed content as it removes the
> need for forge to create any kind of ID aliases which usually need to be resolved at runtime.

> [!TIP]
>
> `OreDictionary.WILDCARD_VALUE` has a meaning when registering simple transformations:
>
> When used as the **source** meta or damage value; Postea will use the transformation being specified as a **fallback**
> if nothing has specified a meta or damage transformation for the block or item's current meta or damage value.
>
> When used as the **target** meta or damage value; Postea will **preserve the existing value** instead of over-writing it.

> [!TIP]
>
> When registering block or item conversions, Postea will automatically detect if the given item or block has a block or
> item counterpart (respectively) and register an appropriate simple transformation. You can disable this behaviour by
> adding a `false` argument to end of all simple transformation endpoints.

> [!NOTE]
>
> Simple transformations are usually executed after all other complex transformers.

Code example
```java
public abstract class SimpleTransformationExample {
    public static void postLoad() {
        // item/block -> item/block, preserving the Meta/Damage value.
        //
        // remap all block and items of colored stone blocks with wool of the same color
        BlockReplacementManager.addSimpleReplacement("ExtraUtilities:colorStoneBrick", Blocks.wool);

        // Item/Block with meta -> Item/Block, meta preserved
        //
        // angel ring:3 to disk 11
        ItemStackReplacementManager.addSimpleReplacement("ExtraUtilities:angelRing", 3, new ItemStack(Items.record_11));
        ItemStackReplacementManager.addSimpleReplacement("ExtraUtilities:angelRing", 4, new ItemStack(Items.record_13));

        // Item/Block with meta -> Item/Block with specific meta
        //
        // remap all compressed cobble to oak planks
        //
        // When using a stack as a target the passed stack size is ignored,
        // only the contained item and meta value are kept.
        ItemStackReplacementManager.addSimpleReplacement("ExtraUtilities:cobblestone_compressed", 0, new ItemStack(Blocks.planks, 0, 0));

        // block only with meta -> block with specific meta value.
        //
        // remap blocks of double compressed cobble to cobble.
        BlockReplacementManager.addSimpleReplacement("ExtraUtilities:cobblestone_compressed", 1, Blocks.cobblestone, 0, true);
        // item only with damage -> item only with specific damage value.
        //
        // remap stacks of double compressed cobble to oak saplings.
        ItemStackReplacementManager.addSimpleReplacement("ExtraUtilities:cobblestone_compressed", 1,new ItemStack(Blocks.sapling, 1, 0), true);

        // Block/Item with any Meta/Damage -> Block/Item with specific Meta/Damage value.
        //
        // Using OreDictionary.WILDCARD_VALUE will tell postea to use this mapping as a fallback when
        // there is no transformer targeting the specific meta/damage value of the block/item.
        //
        // remap all other instances of compressed blocks to endstone
        BlockReplacementManager.addSimpleReplacement("ExtraUtilities:cobblestone_compressed", OreDictionary.WILDCARD_VALUE, Blocks.end_stone, 0);

        // Block/Item with any Meta/Damage -> Block/Item, preserving the Meta/Damage value.
        //
        // When passing OreDictionary.WILDCARD_VALUE as the target Meta/Damage value, Postea will
        // preserve any existing Meta/Damage value. This is also the default value when you don't
        // specify a target meta value.
        //
        // remap all other instances of compressed blocks to endstone
        BlockReplacementManager.addSimpleReplacement(
          "ExtraUtilities:cobblestone_compressed",
          OreDictionary.WILDCARD_VALUE,
          Blocks.end_stone,
          OreDictionary.WILDCARD_VALUE);

    }
}

```

### 5. Complex Block Transformers

If you need some custom logic in order to perform a block conversion, you can register a custom transformer using
`BlockReplacementManager.addTransformationHandler`.

> [!TIP]
>
> Returning `true` tells Postea to stop looking for any other handlers for the current block, and to replace the block
> being transformed using the `blockID` and `metadata` currently stored in the `BlockConversionInfo` parameter.

> [!CAUTION]
>
> Just like for Tile Entity transformers, trying to perform block access using the world provided by the
> BlockTransformationInfo parameter will result in an error as the transformers are executed before the world's content
> are properly initialized.

```java
public abstract class ComplexBlockTransformer {

    private static int woolId = -1;

    public static void postLoad() {
        // This transformer replaces all grass blocks with even x-coordinates with orange wool.
        BlockReplacementManager.registerIDResolver("minecraft:wool", i -> woolId = i);
        BlockReplacementManager.addTransformationHandler("minecraft:grass", ComplexBlockTransformer::transformGrass);
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
```


### 6. Stack Transformation

If you need some custom logic in order to perform an item conversion, you can register a custom transformer using
`ItemStackReplacementManager.addTransformationHandler`.

Code example:
```java
public abstract class ComplexItemTransformer {

    private static int writableBookId = -1;
    private static int woolId = -1;

    private static void postLoad() {
        ItemStackReplacementManager.registerIDResolver("minecraft:writable_book", i -> writableBookId = i);
        ItemStackReplacementManager.registerIDResolver("minecraft:wool", i -> woolId = i);
        ItemStackReplacementManager.addTransformationHandler("minecraft:written_book", ComplexItemTransformer::unsignBook);
    }

  /**
   * This transformer transforms a written book into either an unsigned book or orange wool depending on it's title.
   *
   * @param name The name of the item being transformed, even if that item doesn't exist anymore.
   * @param nbt  The NBT data of the stack item being transformed.
   * @return True if we've actually transformed the item.
   */
    private static boolean unsignBook(String name, NBTTagCompound nbt) {
        // check if the book has a title.
        if (!nbt.hasKey("tag",10)) return false;
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
        }
        else if (title.equalsIgnoreCase("orange wool")) {
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
```

### 7. Remapping Missing Mappings using FML

Postea provides a few API endpoints to quickly register re-mappings for any given Item or Block that has been removed.

> [!NOTE]
>
> It's generally more advisable to use the simple transformers instead as they will create less overhead in the
> long term.

> [!CAUTION]
>
> If you try to remap something to a block or item that already has ever existed at a prior date inside a world,
> (eg: remapping to a block of dirt or stick, or something from your own mod that has existed for a while) FML will
> throw an error when it loads the world and tell you that your world is corrupted.
>
> Simple Transformers are generally safer in that regard and do not care if a thing has ever existed in a world before.
> Therefore we generally recommend using them instead of FML re-mappings.

Code example:
```java
public abstract class FMLReMappingExample {

    // You have to make your own block class since the block constructor is protected.
    public static class MyBlock extends Block {
        protected MyBlock() {
            super(Material.ground);
        }
    }

    public static void postLoad() {
        // this only works when the thing you're replacing with has never been registered to a world's
        // ID map. If you try to run this with a block that already exists (eg good old dirt), FML
        // error out and you that your world is corrupted beyond repair, and advise you to undo what
        // ever you just did.
        //
        // Postea Simple Transformers don't have that issue and are more lag friendly in the long term.
        // Consider using them when ever possible, instead of FML mappings.
        Item item = new Item();
        GameRegistry.registerItem(item, "my_item");
        ItemStackReplacementManager.replaceMissingMapping("IC2:itemFertilizer", item);

        // This also remaps the stacks of IC2:blockCrop
        Block block = new MyBlock();
        GameRegistry.registerBlock(block, "my_block");
        BlockReplacementManager.replaceMissingMapping("IC2:blockCrop", block);
    }
}
```

### 8. Ignoring Missing Mappings from FML

Postea provides a few API endpoints that let you tell FML to hide any missing mapping warnings regarding that ID when
loading a world.

> [!NOTE]
>
> You should only register an ignore mapping when you know that your transformers fully replace what ever used to be
> associated with the given ID.

Code example:
```java
public abstract class FMLIgnoreMissingMappingExample {
    public static void postLoad() {
        // It doesn't really matter which one you use, they both do the same thing under the hood.
        ItemStackReplacementManager.ignoreMissingMapping("IC2:itemFertilizer");
        BlockReplacementManager.ignoreMissingMapping("IC2:blockCrop");
    }
}
```
