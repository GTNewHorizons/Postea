package com.gtnewhorizons.postea.api;

import java.util.function.Consumer;

import net.minecraft.nbt.NBTTagCompound;

import com.github.bsideup.jabel.Desugar;
import com.gtnewhorizons.postea.utility.NbtItemStacks;

/// A mod's custom world storage, as handed to [IVersionedTransformer#transformCustomData]. Stacks here may have
/// string `id`s instead of numeric ones, and [#forEachItemStackTag] visits both.
///
/// @param storageId the storage's identity as its owner registered it, e.g. `"betterquesting:questDatabase"`
/// @param storedVersion the version stamped under the transformer's key when the storage was written, or
/// [IVersionedTransformer#UNSTAMPED]
@Desugar
public record CustomDataTransformContext(String storageId, NBTTagCompound root, int storedVersion) {

    /// Visits every serialized item stack in the storage, including stacks whose `id` is a string name. See
    /// [NbtItemStacks#forEach].
    public void forEachItemStackTag(Consumer<NBTTagCompound> visitor) {
        NbtItemStacks.forEach(root, visitor, true);
    }
}
