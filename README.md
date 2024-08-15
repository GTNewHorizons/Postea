# TileEntityReplacementManager API

This API provides a suite of tools for transforming blocks and items in the game, allowing developers to replace or modify game elements based on specific conditions.

## Features

1. **Tile Entity Transformation**: Replace or modify tile entities.
2. **Block Replacement**: Replace normal blocks with another normal block.
3. **ItemStack Replacement**: Modify or replace items.

## Examples

### 1. Tile Entity Transformation

#### a. We provide the registered TE ID "GT_TileEntity_Ores" and a lambda function that takes in the NBTTagCompound for any matching tile entity in world and a World and returns a BlockInfo. This will replace all TE's with the ID "GT_TileEntity_Ores" with the block specified in the BlockInfo. The secondary parameter determines the damage value of the block. Returning null instead of a BlockInfo will leave the existing tile and block totally unchanged.

```java
TileEntityReplacementManager.tileEntityTransformer("GT_TileEntity_Ores", (tag, world) -> {
  if (world.provider.dimensionId == -1) {
      return new BlockInfo(Blocks.wool, 1);
  }
  if (world.provider.dimensionId == 1) {
      return new BlockInfo(Blocks.wool, 2);
  }
  return new BlockInfo(Blocks.wool, 3);
});
```

#### b. We do the same here, but now we convert a Furnace TE to a chest TE. We also need to convert the underlying block to a chest. In addtion to this we can provide a transformer that can edit the NBTTagCompound of the TE. This is useful for converting NBT data from one format to another. Here we simply edit the NBT to place stone in the middle slot.

```java
TileEntityReplacementManager.tileEntityTransformer("Chest", (tag, world) -> {
  return new BlockInfo(Blocks.chest, 0, Postea::chestTransformer);
});

private static NBTTagCompound chestTransformer(NBTTagCompound oldTag) {

  NBTTagCompound newTag = cleanseNBT("Chest", oldTag);

  NBTTagList tagList = new NBTTagList();

  NBTTagCompound stoneAtSlot13 = new NBTTagCompound();
  stoneAtSlot13.setByte("Count", (byte) 1);
  stoneAtSlot13.setByte("Slot", (byte) 13);
  stoneAtSlot13.setShort("Damage", (short) 0);
  stoneAtSlot13.setShort("id", (short) Block.getIdFromBlock(Blocks.stone));

  tagList.appendTag(stoneAtSlot13);

  newTag.setTag("Items", tagList);

  return newTag;
}
```

### 2. Normal Block Transformation

#### a. The following example will replace all grass blocks with even x-coordinates with orange wool blocks.

```java
BiFunction<BlockConversionInfo, World, BlockConversionInfo> blockTransformer = (blockConversionInfoOld, world) -> {

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
```


### 3. ItemStack Transformation

#### a. The following example will replace all iron ingots with gold ingots.

```java
Function<NBTTagCompound, NBTTagCompound> itemTransformer = (tag) -> {
  tag.setShort("id", (short) Item.getIdFromItem(Items.gold_ingot));
  return tag;
};

ItemStackReplacementManager.addItemReplacement("minecraft:iron_ingot", itemTransformer);
```

