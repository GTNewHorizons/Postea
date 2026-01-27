package com.gtnewhorizons.postea.api;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.OreDictionary;

import com.gtnewhorizons.postea.utility.BlockConversionInfo;

public abstract class PosteaBlockStackTransformer {

    public String originalId;
    public int originalMeta;

    public PosteaBlockStackTransformer(String originalId) {
        this(originalId, OreDictionary.WILDCARD_VALUE);
    }

    public PosteaBlockStackTransformer(String originalId, int originalMeta) {
        this.originalId = originalId;
        this.originalMeta = originalMeta;
    }

    public void register() {
        if (this instanceof IComplexStackTransformer cst) {
            if (this.originalMeta == OreDictionary.WILDCARD_VALUE) {
                ItemStackReplacementManager.addTransformationHandler(
                    this.originalId,
                    (originalId, tag) -> complexStackTransformerCheckMeta(originalId, tag, cst));
            } else {
                ItemStackReplacementManager.addTransformationHandler(this.originalId, cst::transformStack);
            }
        }
        if (this instanceof IComplexBlockTransformer cbt) {
            if (this.originalMeta == OreDictionary.WILDCARD_VALUE) {
                BlockReplacementManager
                    .addTransformationHandler(this.originalId, (info) -> complexBlockTransformerCheckMeta(info, cbt));
            } else {
                BlockReplacementManager.addTransformationHandler(this.originalId, cbt::transformBlock);
            }
        }
        if (this instanceof ISimpleBlockTransformer sbt) {
            BlockReplacementManager.addSimpleReplacement(
                this.originalId,
                this.originalMeta,
                sbt.getTargetBlock(),
                sbt.getTargetBlockMeta(),
                true);
        }
        if (this instanceof ISimpleItemTransformer sit) {
            ItemStackReplacementManager.addSimpleReplacement(
                this.originalId,
                this.originalMeta,
                sit.getTargetItem(),
                sit.getTargetItemMeta(),
                true);
        }
        if (this instanceof ISimpleStackTransformer sst) {
            ItemStack stack = sst.getTargetStack();
            ItemStackReplacementManager.addSimpleReplacement(
                this.originalId,
                this.originalMeta,
                stack.getItem(),
                Items.feather.getDamage(stack),
                true);
        }
    }

    private boolean complexStackTransformerCheckMeta(String originalId, NBTTagCompound tag,
        IComplexStackTransformer st) {
        if (tag.getShort("Damage") != originalMeta) return false;
        return st.transformStack(originalId, tag);
    }

    private boolean complexBlockTransformerCheckMeta(BlockConversionInfo info, IComplexBlockTransformer bt) {
        if (info.metadata != originalMeta) return false;
        return bt.transformBlock(info);
    }
}
