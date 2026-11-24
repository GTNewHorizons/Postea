package com.gtnewhorizons.postea.api;

import java.util.function.Consumer;

import net.minecraft.nbt.NBTTagCompound;

/// A versioned transformer that rewrites every saved item stack, in chunks, player data and custom world storage alike.
/// A subclass that also rewrites blocks overrides [#transformChunk] and calls `super`.
///
/// Each pass calls [#stackTransform] once, before visiting any stack.
public abstract class VersionedStackTransformer implements IVersionedTransformer {

    /// The rewrite applied to each stack tag in data stamped at `storedVersion`, or null when that data needs none.
    protected abstract Consumer<NBTTagCompound> stackTransform(int storedVersion);

    @Override
    public void transformChunk(ChunkTransformContext ctx) {
        Consumer<NBTTagCompound> transform = stackTransform(ctx.storedVersion());
        if (transform != null) ctx.forEachItemStackTag(transform);
    }

    @Override
    public void transformPlayer(PlayerDataTransformContext ctx) {
        Consumer<NBTTagCompound> transform = stackTransform(ctx.storedVersion());
        if (transform != null) ctx.forEachItemStackTag(transform);
    }

    @Override
    public void transformCustomData(CustomDataTransformContext ctx) {
        Consumer<NBTTagCompound> transform = stackTransform(ctx.storedVersion());
        if (transform != null) ctx.forEachItemStackTag(transform);
    }
}
