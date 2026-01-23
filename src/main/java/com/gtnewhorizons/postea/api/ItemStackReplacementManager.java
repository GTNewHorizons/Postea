package com.gtnewhorizons.postea.api;

import java.util.function.BiFunction;
import java.util.function.Function;

import javax.annotation.Nonnull;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.OreDictionary;

import com.gtnewhorizons.postea.utility.MissingMappingHandler;
import com.gtnewhorizons.postea.utility.SimpleTransformationRegistry;
import com.gtnewhorizons.postea.utility.TransformerRegistry;

public class ItemStackReplacementManager {

    // Public API for converting ItemStacks.

    /**
     * Adds a custom transformer for a given id.
     *
     * @deprecated Use {@link #addTransformationHandler(String, BiFunction)} instead.
     *
     * @apiNote When the transformer function passed to this function returns a null, value Postea will assume that
     *          the transformer has failed to identify and run the handler on any other handler function registered
     *          to the given ID.
     *          <p>
     *          If you need to perform a simple transformations consider using the following methods instead:
     *          <ul>
     *          <li>item -> item (with same meta): {@link #addItemReplacement(String, Item)}</li>
     *          <li>item -> item with specific meta: {@link #addItemReplacement(String, Item, int)}</li>
     *          <li>item with meta -> item (with same meta): {@link #addItemReplacement(String, int, Item)}</li>
     *          <li>item with meta -> item with specific meta: {@link #addItemReplacement(String, int, Item, int)}</li>
     *          </ul>
     *
     * @implNote For performance reasons, Postea assumes that the tag that was passed to the handler hasn't been
     *           modified
     *           if you return a null value. This isn't enforced though since returning a null value after doing some
     *           modifications has a couple niche uses. eg: having separate handlers for different migrations of a
     *           single thing that can be sunset at different times down the line.
     *
     * @param originalId  The id of the item to transform.
     * @param transformer The transformer to apply.
     */
    @Deprecated
    @SuppressWarnings("unused")
    public static void addItemReplacement(String originalId, Function<NBTTagCompound, NBTTagCompound> transformer) {
        if (transformer == null) throw new IllegalArgumentException("transformer is null");
        addTransformationHandler(originalId, (_originalId, tag) -> transformer.apply(tag) != null);
    }

    /**
     * Adds a custom transformer for a given id.
     *
     * @apiNote When the transformer function passed to this function returns a null, value Postea will assume that
     *          the transformer has failed to identify and run the handler on any other handler function registered
     *          to the given ID.
     *          <p>
     *          If you need to perform a simple transformations consider using the following methods instead:
     *          <ul>
     *          <li>item -> item (with same meta): {@link #addItemReplacement(String, Item)}</li>
     *          <li>item -> item with specific meta: {@link #addItemReplacement(String, Item, int)}</li>
     *          <li>item with meta -> item (with same meta): {@link #addItemReplacement(String, int, Item)}</li>
     *          <li>item with meta -> item with specific meta: {@link #addItemReplacement(String, int, Item, int)}</li>
     *          </ul>
     *
     * @implNote For performance reasons, Postea assumes that the tag that was passed to the handler hasn't been
     *           modified if you return false. This isn't enforced though since returning false after doing some
     *           modifications has a couple niche uses. eg: having separate handlers for different migrations of a
     *           single thing that can be sunset at different times down the line.
     *
     * @param originalId  The id of the item to transform.
     * @param transformer The transformer to apply.
     */
    @SuppressWarnings("unused")
    public static void addTransformationHandler(String originalId,
        BiFunction<String, NBTTagCompound, Boolean> transformer) {
        if (originalId == null) throw new IllegalArgumentException("original id is null");
        if (transformer == null) throw new IllegalArgumentException("transformer is null");
        TransformerRegistry.addItemReplacement(originalId, transformer);
    }

    /**
     * Adds a mapping to remap stacks of a specific removed item to the given item.
     * <p>
     * <b><u>IMPORTANT: This only works if the world does not have an existing id set for the target item. If you need
     * to transform a missing item into something that was previously given an id (e.g.: Transforming a removed item
     * into a stick), use {@link #addItemReplacement(String, int, Item, int)} with both meta values set to
     * OreDictionary.WILDCARD_VALUE.</u></b>
     *
     * @param originalId The id of the item to remap.
     * @param item       The item to remap to.
     */
    @SuppressWarnings("unused")
    public static void replaceMissingItemWithNewItem(String originalId, Item item) {
        if (originalId == null) throw new IllegalArgumentException("original id is null");
        if (item == null) throw new IllegalArgumentException("item is null");
        MissingMappingHandler.addSimpleReplacement(originalId, item);
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

    // item -> item

    /**
     * Adds a mapping to remap stacks of a given item to another item, whilst maintaining its damage/meta value. This
     * also adds a matching block replacement if this item happens to be an ItemBlock.
     *
     * @apiNote Use this if you are replacing an item with something that was already registered to a world's id
     *          mappings,
     *          (e.g.: Transforming a removed item into a stick)
     *
     * @implNote All transformations registered using this handler are executed in constant time regardless of how many
     *           transformations are registered to a single id.
     *
     * @param originalId The id of the item to remap.
     * @param item       The item to remap to.
     */
    @SuppressWarnings("unused")
    public static void addItemReplacement(@Nonnull String originalId, @Nonnull Item item) {
        addItemReplacement(originalId, OreDictionary.WILDCARD_VALUE, item, OreDictionary.WILDCARD_VALUE, false);
    }

    /**
     * Adds a mapping to remap stacks of a given item to another item, whilst maintaining its damage/meta value.
     *
     * @apiNote Use this if you are replacing an item with something that was already registered to a world's id
     *          mappings,
     *          (e.g.: Transforming a removed item into a stick)
     *
     * @implNote All transformations registered using this handler are executed in constant time regardless of how many
     *           transformations are registered to a single id.
     *
     * @param originalId     The id of the item to remap.
     * @param item           The item to remap to.
     * @param skipBlockRemap Set to true to skip auto-adding a block remapper if the item happens to be an ItemBlock.
     */
    @SuppressWarnings("unused")
    public static void addItemReplacement(@Nonnull String originalId, @Nonnull Item item, boolean skipBlockRemap) {
        addItemReplacement(
            originalId,
            OreDictionary.WILDCARD_VALUE,
            item,
            OreDictionary.WILDCARD_VALUE,
            skipBlockRemap);
    }

    // item -> item+meta

    /**
     * Adds a mapping to remap stacks of a given item to another item with a given damage/meta value. This also adds a
     * matching block replacement if this item happens to be an ItemBlock.
     *
     * @implNote All transformations registered using this handler are executed in constant time regardless of how many
     *           transformations are registered to a single id.
     *
     * @param originalId The id of the item to remap.
     * @param item       The item to remap to.
     * @param newMeta    The meta of the item to remap to. OreDictionary.WILDCARD_VALUE maintains the existing value.
     */
    @SuppressWarnings("unused")
    public static void addItemReplacement(@Nonnull String originalId, @Nonnull Item item, int newMeta) {
        addItemReplacement(originalId, OreDictionary.WILDCARD_VALUE, item, newMeta, false);
    }

    /**
     * Adds a mapping to remap stacks of a given item to another item with a given damage/meta value.
     *
     * @implNote All transformations registered using this handler are executed in constant time regardless of how many
     *           transformations are registered to a single id.
     *
     * @param originalId     The id of the item to remap.
     * @param item           The item to remap to.
     * @param newMeta        The meta of the item to remap to. OreDictionary.WILDCARD_VALUE maintains the existing
     *                       value.
     * @param skipBlockRemap Set to true to skip auto-adding a block remapper if the item happens to be an ItemBlock.
     */
    @SuppressWarnings("unused")
    public static void addItemReplacement(@Nonnull String originalId, @Nonnull Item item, int newMeta,
        boolean skipBlockRemap) {
        addItemReplacement(originalId, OreDictionary.WILDCARD_VALUE, item, newMeta, skipBlockRemap);
    }

    // item -> stack

    /**
     * Adds a mapping to remap stacks of a given item to another item with a given damage/meta value. This also adds a
     * matching block replacement if this item happens to be an ItemBlock.
     * <br>
     * DOES NOT COPY NBT FROM STACK.
     *
     * @implNote All transformations registered using this handler are executed in constant time regardless of how many
     *           transformations are registered to a single id.
     *
     * @param originalId The id of the item to remap.
     * @param stack      The item to remap to.
     */
    @SuppressWarnings("unused")
    public static void addItemReplacement(@Nonnull String originalId, @Nonnull ItemStack stack) {
        addItemReplacement(
            originalId,
            OreDictionary.WILDCARD_VALUE,
            stack.getItem(),
            Items.feather.getDamage(stack),
            false);
    }

    /**
     * Adds a mapping to remap stacks of a given item to another item with a given damage/meta value.
     * <br>
     * DOES NOT COPY NBT FROM STACK.
     *
     * @implNote All transformations registered using this handler are executed in constant time regardless of how many
     *           transformations are registered to a single id.
     *
     * @param originalId     The id of the item to remap.
     * @param stack          A stack of the item to replace to.
     * @param skipBlockRemap Set to true to skip auto-adding a block remapper if the item happens to be an ItemBlock.
     */
    @SuppressWarnings("unused")
    public static void addItemReplacement(@Nonnull String originalId, @Nonnull ItemStack stack,
        boolean skipBlockRemap) {
        addItemReplacement(
            originalId,
            OreDictionary.WILDCARD_VALUE,
            stack.getItem(),
            Items.feather.getDamage(stack),
            skipBlockRemap);
    }

    // item+meta -> item

    /**
     * Adds a mapping to remap all stacks of a given item and damage/meta value to another item, whilst maintaining its
     * damage/meta value. This also adds a matching block replacement if this item happens to be an ItemBlock.
     *
     * @implNote All transformations registered using this handler are executed in constant time regardless of how many
     *           transformations are registered to a single id.
     *
     * @param originalId   The id of the item to remap.
     * @param originalMeta The meta of the item to remap. OreDictionary.WILDCARD_VALUE acts as a wildcard.
     * @param item         The item to remap to.
     */
    @SuppressWarnings("unused")
    public static void addItemReplacement(@Nonnull String originalId, int originalMeta, @Nonnull Item item) {
        addItemReplacement(originalId, originalMeta, item, OreDictionary.WILDCARD_VALUE, false);
    }

    /**
     * Adds a mapping to remap all stacks of a given item and damage/meta value to another item, whilst maintaining its
     * damage/meta value.
     *
     * @implNote All transformations registered using this handler are executed in constant time regardless of how many
     *           transformations are registered to a single id.
     *
     * @param originalId     The id of the item to remap.
     * @param originalMeta   The meta of the item to remap. OreDictionary.WILDCARD_VALUE acts as a wildcard.
     * @param item           The item to remap to.
     * @param skipBlockRemap Set to true to skip auto-adding a item remapper if the .
     */
    @SuppressWarnings("unused")
    public static void addItemReplacement(@Nonnull String originalId, int originalMeta, @Nonnull Item item,
        boolean skipBlockRemap) {
        addItemReplacement(originalId, originalMeta, item, OreDictionary.WILDCARD_VALUE, skipBlockRemap);
    }

    // item+meta -> stack

    /**
     * Adds a mapping to remap all stacks of a given item and damage/meta value to another item with a
     * given damage/meta value. This also adds a matching block replacement if this item happens to be an ItemBlock.
     * <br>
     * DOES NOT COPY NBT FROM STACK.
     *
     * @implNote All transformations registered using this handler are executed in constant time regardless of how many
     *           transformations are registered to a single id.
     *
     * @param originalId   The id of the item to remap.
     * @param originalMeta The meta of the item to remap. OreDictionary.WILDCARD_VALUE acts as a wildcard.
     * @param stack        A stack of the item to replace to.
     */
    @SuppressWarnings("unused")
    public static void addItemReplacement(@Nonnull String originalId, int originalMeta, @Nonnull ItemStack stack) {
        addItemReplacement(originalId, originalMeta, stack, false);
    }

    /**
     * Adds a mapping to remap all stacks of a given item and damage/meta value to another item with a
     * given damage/meta value.
     * <br>
     * DOES NOT COPY NBT FROM STACK.
     *
     * @implNote All transformations registered using this handler are executed in constant time regardless of how many
     *           transformations are registered to a single id.
     *
     * @param originalId     The id of the item to remap.
     * @param originalMeta   The meta of the item to remap. OreDictionary.WILDCARD_VALUE acts as a wildcard.
     * @param stack          A stack of the item to replace to.
     * @param skipBlockRemap Set to true to skip auto-adding a block remapper if the item happens to be an ItemBlock.
     */
    @SuppressWarnings("unused")
    public static void addItemReplacement(@Nonnull String originalId, int originalMeta, @Nonnull ItemStack stack,
        boolean skipBlockRemap) {
        addItemReplacement(originalId, originalMeta, stack.getItem(), Items.feather.getDamage(stack), skipBlockRemap);
    }

    // item+meta -> item+meta

    /**
     * Adds a mapping to remap all stacks of a given item and damage/meta value to another item with a
     * given damage/meta value. This also adds a matching block replacement if this item happens to be an ItemBlock.
     *
     * @implNote All transformations registered using this handler are executed in constant time regardless of how many
     *           transformations are registered to a single id.
     *
     * @param originalId   The id of the item to remap.
     * @param originalMeta The meta of the item to remap. OreDictionary.WILDCARD_VALUE acts as a wildcard.
     * @param item         The item to remap to.
     * @param newMeta      The meta of the item to remap to. OreDictionary.WILDCARD_VALUE maintains the existing value.
     */
    @SuppressWarnings("unused")
    public static void addItemReplacement(@Nonnull String originalId, int originalMeta, @Nonnull Item item,
        int newMeta) {
        addItemReplacement(originalId, originalMeta, item, newMeta, false);
    }

    /**
     * Adds a mapping to remap all stacks of a given item and damage/meta value to another item with a
     * given damage/meta value.
     *
     * @implNote All transformations registered using this handler are executed in constant time regardless of how many
     *           transformations are registered to a single id.
     *
     * @param originalId     The id of the item to remap.
     * @param originalMeta   The meta of the item to remap. OreDictionary.WILDCARD_VALUE acts as a wildcard.
     * @param item           The item to remap to.
     * @param newMeta        The meta of the item to remap to. OreDictionary.WILDCARD_VALUE maintains the existing
     *                       value.
     * @param skipBlockRemap Set to true to skip auto-adding a item remapper if the .
     */
    @SuppressWarnings("unused")
    public static void addItemReplacement(String originalId, int originalMeta, Item item, int newMeta,
        boolean skipBlockRemap) {
        if (originalId == null) throw new IllegalArgumentException("original id is null");
        if (item == null) throw new IllegalArgumentException("item is null");
        SimpleTransformationRegistry.addSimpleTransformer(originalId, originalMeta, item, newMeta, skipBlockRemap);
    }
}
