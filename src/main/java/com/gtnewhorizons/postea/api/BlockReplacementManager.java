package com.gtnewhorizons.postea.api;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

import com.gtnewhorizons.postea.utility.BlockConversionInfo;
import com.gtnewhorizons.postea.utility.IDRegistry;
import com.gtnewhorizons.postea.utility.MissingMappingHandler;
import com.gtnewhorizons.postea.utility.SimpleTransformationRegistry;
import com.gtnewhorizons.postea.utility.TransformerRegistry;

/**
 * Public API used to register block transformers via postea's handler system.<br>
 * <br>
 * Block transformers are executed after Tile Entity transformers, when chunks being loading into the world.
 */
public abstract class BlockReplacementManager {

    // region custom transformer registration

    /**
     * Adds a custom transformer for a given id.
     *
     * @deprecated Use {@link #addTransformationHandler(String, Function)}
     *
     * @apiNote When the transformer function passed to this function returns a null, value Postea will assume that
     *          the transformer has failed to identify and run the handler on any other handler function registered
     *          to the given ID.
     *          <br>
     *          If you need to perform a simple transformations consider using the following methods instead:
     *          <ul>
     *          <li>block -> block (with same meta): {@link #addSimpleReplacement(String, Block)}</li>
     *          <li>block -> block with specific meta: {@link #addSimpleReplacement(String, Block, int)}</li>
     *          <li>block with meta -> block (with same meta): {@link #addSimpleReplacement(String, int, Block)}</li>
     *          <li>block with meta -> block with specific meta:
     *          {@link #addSimpleReplacement(String, int, Block, int)}</li>
     *          </ul>
     *
     * @implNote For performance reasons, Postea assumes that the BlockConversionInfo that was passed to the handler
     *           hasn't been modified if you return a null value. This isn't enforced though since returning a null
     *           value after doing some modifications has a couple niche uses. eg: having separate handlers for
     *           different migrations of a single thing that can be sunset at different times down the line.
     *
     * @param originalId  The id of the block to transform.
     * @param transformer The transformer to apply.
     */
    @Deprecated
    @SuppressWarnings("unused")
    public static void addBlockReplacement(String originalId,
        BiFunction<BlockConversionInfo, World, BlockConversionInfo> transformer) {
        if (transformer == null) throw new IllegalArgumentException("transformer is null");
        addTransformationHandler(originalId, info -> transformer.apply(info, info.world) != null);
    }

    /**
     * Adds a custom transformer for a given id.
     *
     * @apiNote When the transformer function passed to this function returns a null, value Postea will assume that
     *          the transformer has failed to identify and run the handler on any other handler function registered
     *          to the given ID.
     *          <br>
     *          If you need to perform a simple transformations consider using the following methods instead:
     *          <ul>
     *          <li>block -> block (with same meta): {@link #addSimpleReplacement(String, Block)}</li>
     *          <li>block -> block with specific meta: {@link #addSimpleReplacement(String, Block, int)}</li>
     *          <li>block with meta -> block (with same meta): {@link #addSimpleReplacement(String, int, Block)}</li>
     *          <li>block with meta -> block with specific meta:
     *          {@link #addSimpleReplacement(String, int, Block, int)}</li>
     *          </ul>
     *
     * @implNote For performance reasons, Postea assumes that the BlockConversionInfo that was passed to the handler
     *           hasn't been modified if your handler return false. This isn't enforced though since returning false
     *           after doing some modifications has a couple niche uses. eg: having separate handlers for different
     *           migrations of a single thing that can be sunset at different times down the line.
     *
     * @param originalId  The id of the block to transform.
     * @param transformer The transformer to apply.
     */
    @SuppressWarnings("unused")
    public static void addTransformationHandler(String originalId, Function<BlockConversionInfo, Boolean> transformer) {
        if (originalId == null) throw new IllegalArgumentException("original id is null");
        if (transformer == null) throw new IllegalArgumentException("transformer is null");
        TransformerRegistry.addBlockTransformer(originalId, transformer);
    }

    // endregion custom transformer registration

    // region id resolving

    // spotless:off
    /**
     * Registers a callback function that will be provided the numeric ID of the given namespaced ID when a world is
     * first loaded. Since forge doesn't remap missing block IDs to 0 when loading a world for performance reasons,
     * instead opting to treat any block ID as Air, you can store that value into a static field, and use it to
     * compare against an id provided to you during the execution of a transformation handler.<br>
     * <br>
     * This function is mostly intended to be used in partnership with tile entity transformers, and is intended to
     * overcome the need to create dummy blocks and items in order to identify blocks inside a custom transformer.<br>
     * <br>
     * For compatibility reasons, it's better not to register dummies to take the place of dummy items. If one mod was
     * to only convert blocks with a specific meta while another handled the rest, the other mod would either need
     * additional work to detect your own dummy blocks or even worst, fail to identify the original blocks as it's not
     * aware of your dummy blocks.
     *
     * @apiNote Here is an example implementation:
     *          <pre>{@code
     *          import net.minecraft.block.Block;
     *          import net.minecraft.block.material.Material;
     *          import net.minecraft.nbt.NBTTagCompound;
     *          import net.minecraft.world.World;
     *          import net.minecraft.world.chunk.Chunk;
     *
     *          import com.gtnewhorizons.postea.api.BlockAccessCompat;
     *          import com.gtnewhorizons.postea.api.BlockReplacementManager;
     *          import com.gtnewhorizons.postea.api.TileEntityReplacementManager;
     *          import com.gtnewhorizons.postea.utility.BlockInfo;
     *          import com.gtnewhorizons.postea.utility.PosteaUtilities;
     *
     *          import cpw.mods.fml.common.Mod;
     *          import cpw.mods.fml.common.event.FMLPostInitializationEvent;
     *          import cpw.mods.fml.common.event.FMLPreInitializationEvent;
     *          import cpw.mods.fml.common.registry.GameRegistry;
     *
     *          @Mod(
     *              modid = "mymod",
     *              version = "1.0.0",
     *              name = "Example Mod",
     *              acceptedMinecraftVersions = "[1.7.10]",
     *              dependencies = " after:otherMod;")
     *          @SuppressWarnings("unused")
     *          public class ExampleMod {
     *
     *              private static int targetBlock1 = -1;
     *              private static int targetBlock2 = -1;
     *              private static int targetBlock3 = -1;
     *
     *              private static class MyBlocks {
     *                  public static Block block1;
     *                  public static Block block2;
     *                  public static Block block3;
     *                  public static Block block4;
     *                  public static Block block5;
     *                  public static Block block6;
     *              }
     *
     *              private static class TestBlock extends Block {
     *                  public TestBlock(String id) {
     *                      super(Material.ground);
     *                      GameRegistry.registerBlock(this, id);
     *                  }
     *              }
     *
     *              @Mod.EventHandler
     *              @SuppressWarnings("unused")
     *              public static void preInit(FMLPreInitializationEvent event) {
     *                  MyBlocks.block1 = new TestBlock("block1");
     *                  MyBlocks.block2 = new TestBlock("block2");
     *                  MyBlocks.block3 = new TestBlock("block3");
     *                  MyBlocks.block4 = new TestBlock("block4");
     *                  MyBlocks.block5 = new TestBlock("block5");
     *                  MyBlocks.block6 = new TestBlock("block6");
     *              }
     *
     *              @Mod.EventHandler
     *              @SuppressWarnings("unused")
     *              public static void postLoad(FMLPostInitializationEvent event) {
     *                  BlockReplacementManager.registerIDResolver("otherMod:block1", i -> targetBlock1 = i);
     *                  BlockReplacementManager.registerIDResolver("otherMod:block2", i -> targetBlock2 = i);
     *                  BlockReplacementManager.registerIDResolver("otherMod:block3", i -> targetBlock3 = i);
     *                  TileEntityReplacementManager.tileEntityTransformer("otherModTE", ExampleMod::transform);
     *                  // If you were to have all of these in one mod,
     *                  // the general recommendation would be to merge the handlers,
     *                  // but let's pretend that these are set up by other mods
     *                  // for the sake of example.
     *                  TileEntityReplacementManager.tileEntityTransformer("otherModTE", ExampleMod::transformOther);
     *                  BlockReplacementManager.addBlockReplacement("otherMod:block1", 0, MyBlocks.block1, 0);
     *              }
     *
     *              public static BlockInfo transform(NBTTagCompound tag, World world, Chunk chunk) {
     *                  int oldBlock = BlockAccessCompat.getBlockIDAtTE(tag, chunk);
     *                  int x = tag.getInteger("x") & 0xF;
     *                  int y = tag.getInteger("y");
     *                  int z = tag.getInteger("z") & 0xF;
     *                  int meta = chunk.getBlockMetadata(x, y, z);
     *                  Block newBlock;
     *                  String targetType;
     *                  if (oldBlock == targetBlock1) {
     *                      // block transformer executes after TE transformers
     *                      // so we sill need to check for meta 0
     *                      if (meta == 0) return null;
     *                      newBlock = MyBlocks.block2;
     *                      targetType = "stone";
     *                  } else if (oldBlock == targetBlock2) {
     *                      // another mod would be handling the transformation for 0
     *                      if (meta != 1 && meta != 2) return null;
     *                      newBlock = meta == 1 ? MyBlocks.block3 : MyBlocks.block4;
     *                      targetType = "iron";
     *                  } else {
     *                      // Return null to tell postea that you can't handle via this transformer.
     *                      // Maybe another transformer can handle it.
     *                      return null;
     *                  }
     *                  return new BlockInfo(newBlock, 0, originalNBT -> {
     *                      NBTTagCompound newNBT = PosteaUtilities.cleanseNBT("myTE", originalNBT);
     *                      newNBT.setString("type", targetType);
     *                      newNBT.setInteger("level", meta);
     *                      newNBT.setInteger("fuelLevel", originalNBT.getInteger("storage") * 20);
     *                      return newNBT;
     *                  });
     *              }
     *
     *              public static BlockInfo transformOther(NBTTagCompound tag, World world, Chunk chunk) {
     *                  int oldBlock = BlockAccessCompat.getBlockIDAtTE(tag, chunk);
     *                  int x = tag.getInteger("x") & 0xF;
     *                  int y = tag.getInteger("y");
     *                  int z = tag.getInteger("z") & 0xF;
     *                  int meta = chunk.getBlockMetadata(x, y, z);
     *                  if (oldBlock == targetBlock2) {
     *                      if (meta != 3) return null;
     *                      // that doesn't need to be a TE anymore.
     *                      return new BlockInfo(MyBlocks.block5, 0, null);
     *                  } else if (oldBlock == targetBlock3) {
     *                      // transfer the TE correctly
     *                      return new BlockInfo(MyBlocks.block6, meta, originalNBT -> {
     *                          NBTTagCompound newNBT = PosteaUtilities.cleanseNBT("myOtherTE", originalNBT);
     *                          newNBT.setInteger("remaining", originalNBT.getInteger("storage"));
     *                          return newNBT;
     *                      });
     *                  }
     *                  // can't handle this
     *                  return null;
     *              }
     *          }
     *          }</pre>
     *
     *
     * @param originalId The namespaced ID of a block you wish to identify in one of your transformers.
     * @param resolver   The callback executed to provide the numeric ID of the given namespaced ID.
     */
    // spotless:on
    @SuppressWarnings("unused")
    public static void registerIDResolver(String originalId, Consumer<Integer> resolver) {
        if (originalId == null) throw new NullPointerException("originalId cannot be null");
        if (resolver == null) throw new NullPointerException("resolver cannot be null");
        IDRegistry.registerBlockIDResolver(originalId, resolver);
    }

    // endregion id resolving

    // region missing mapping replacements

    /**
     * Adds a mapping to remap stacks and block instances of a specific removed block to the given block whilst, whilst
     * maintaining its meta value.<br>
     * <br>
     * <b><u>IMPORTANT: This only works if the world does not have an existing id set for the target block. If you need
     * to transform a missing block into something that was previously given an id (e.g.: Transforming a removed block
     * into dirt), use {@link #addSimpleReplacement(String, Block)} with both meta parameters set to
     * OreDictionary.WILDCARD_VALUE.</u></b>
     *
     * @param originalId The id of the block to remap.
     * @param block      The block to remap to.
     */
    @SuppressWarnings("unused")
    public static void replaceMissingBlockWithNewBlock(String originalId, Block block) {
        if (originalId == null) throw new IllegalArgumentException("original id is null");
        if (block == null) throw new IllegalArgumentException("block is null");
        MissingMappingHandler.addBlockMapping(originalId, block);
    }

    /**
     * Suppresses any missing mapping warning for the given id.
     *
     * @param originalId The id of the block or item to suppress the warning for.
     */
    @SuppressWarnings("unused")
    public static void ignoreMissingMapping(@Nonnull String originalId) {
        MissingMappingHandler.addIgnore(originalId);
    }

    // endregion missing mapping replacements

    // region simple replacements

    // block -> block

    /**
     * Adds a mapping to remap all stacks and block instances of a given block to another block, whilst maintaining its
     * meta value. This also adds a matching item stack replacement.
     *
     * @apiNote Use this if you are replacing a block with something that was already registered to a world's id
     *          mappings,
     *          (e.g.: Transforming a removed block into dirt)
     *
     * @param originalId The id of the item to remap.
     * @param block      The block to remap to.
     */
    @SuppressWarnings("unused")
    public static void addSimpleReplacement(@Nonnull String originalId, @Nonnull Block block) {
        addSimpleReplacement(originalId, OreDictionary.WILDCARD_VALUE, block, OreDictionary.WILDCARD_VALUE, false);
    }

    /**
     * Adds a mapping to remap all stacks and block instances of a given block to another block, whilst maintaining its
     * meta value.
     *
     * @apiNote Use this if you are replacing an item with something that was already registered to a world's id
     *          mappings,
     *          (e.g.: Transforming a removed block into dirt)
     *
     * @param originalId     The id of the item to remap.
     * @param block          The block to remap to.
     * @param skipStackRemap Set to true to skip auto-adding an item stack remapper.
     */
    @SuppressWarnings("unused")
    public static void addSimpleReplacement(@Nonnull String originalId, @Nonnull Block block, boolean skipStackRemap) {
        addSimpleReplacement(
            originalId,
            OreDictionary.WILDCARD_VALUE,
            block,
            OreDictionary.WILDCARD_VALUE,
            skipStackRemap);
    }

    // block+meta -> block

    /**
     * Adds a mapping to remap all stacks and block instances of a given block and damage/meta value to another block,
     * whilst maintaining its meta value. This also adds a matching item stack replacement.
     *
     * @param originalId   The id of the block to remap.
     * @param originalMeta The meta of the block to remap. OreDictionary.WILDCARD_VALUE acts as a wildcard.
     * @param block        The block to remap to.
     */
    @SuppressWarnings("unused")
    public static void addSimpleReplacement(@Nonnull String originalId, int originalMeta, @Nonnull Block block) {
        addSimpleReplacement(originalId, originalMeta, block, OreDictionary.WILDCARD_VALUE, false);
    }

    /**
     * Adds a mapping to remap all stacks and block instances of a given block and damage/meta value to another block,,
     * whilst maintaining its meta value.
     *
     * @param originalId     The id of the block to remap.
     * @param originalMeta   The meta of the block to remap. OreDictionary.WILDCARD_VALUE acts as a wildcard.
     * @param block          The block to remap to.
     * @param skipStackRemap Set to true to skip auto-adding an item stack remapper.
     */
    @SuppressWarnings("unused")
    public static void addSimpleReplacement(@Nonnull String originalId, int originalMeta, @Nonnull Block block,
        boolean skipStackRemap) {
        addSimpleReplacement(originalId, originalMeta, block, OreDictionary.WILDCARD_VALUE, skipStackRemap);
    }

    // block -> block+meta

    /**
     * Adds a mapping to remap all stacks and block instances of a given block to another block with a given damage/meta
     * value. This also adds a matching item stack replacement.
     *
     * @param originalId The id of the block to remap.
     * @param block      The block to remap to.
     * @param newMeta    The meta of the block to remap to. OreDictionary.WILDCARD_VALUE maintains the existing value.
     */
    @SuppressWarnings("unused")
    public static void addSimpleReplacement(@Nonnull String originalId, @Nonnull Block block, int newMeta) {
        addSimpleReplacement(originalId, OreDictionary.WILDCARD_VALUE, block, newMeta, false);
    }

    /**
     * Adds a mapping to remap all stacks and block instances of a given block to another block with a given damage/meta
     * value.
     *
     * @param originalId     The id of the block to remap.
     * @param block          The block to remap to.
     * @param newMeta        The meta of the block to remap to. OreDictionary.WILDCARD_VALUE maintains the existing
     *                       value.
     * @param skipStackRemap Set to true to skip auto-adding an item stack remapper.
     */
    @SuppressWarnings("unused")
    public static void addSimpleReplacement(@Nonnull String originalId, @Nonnull Block block, int newMeta,
        boolean skipStackRemap) {
        addSimpleReplacement(originalId, OreDictionary.WILDCARD_VALUE, block, newMeta, skipStackRemap);
    }

    // block+meta -> block+meta

    /**
     * Adds a mapping to remap all stacks and block instances of a given block and damage/meta value to another block
     * with a given damage/meta value. This also adds a matching item stack replacement.
     *
     * @param originalId   The id of the block to remap.
     * @param originalMeta The meta of the block to remap. OreDictionary.WILDCARD_VALUE acts as a wildcard.
     * @param block        The block to remap to.
     * @param newMeta      The meta of the block to remap to. OreDictionary.WILDCARD_VALUE maintains the existing value.
     */
    @SuppressWarnings("unused")
    public static void addSimpleReplacement(@Nonnull String originalId, int originalMeta, @Nonnull Block block,
        int newMeta) {
        addSimpleReplacement(originalId, originalMeta, block, newMeta, false);
    }

    /**
     * Adds a mapping to remap all stacks and block instances of a given block and damage/meta value to another block
     * with a given damage/meta value.
     *
     * @param originalId     The id of the block to remap.
     * @param originalMeta   The meta of the block to remap. OreDictionary.WILDCARD_VALUE acts as a wildcard.
     * @param block          The block to remap to.
     * @param newMeta        The meta of the block to remap to. OreDictionary.WILDCARD_VALUE maintains the existing
     *                       value.
     * @param skipStackRemap Set to true to skip auto-adding an item stack remapper.
     */
    @SuppressWarnings("unused")
    public static void addSimpleReplacement(String originalId, int originalMeta, Block block, int newMeta,
        boolean skipStackRemap) {
        if (originalId == null) throw new IllegalArgumentException("original id is null");
        if (block == null) throw new IllegalArgumentException("block is null");
        SimpleTransformationRegistry.addSimpleTransformer(originalId, originalMeta, block, newMeta, skipStackRemap);
    }

    // endregion simple replacements

}
