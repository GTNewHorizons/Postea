package com.gtnewhorizons.postea.api;

import java.util.function.Consumer;

import net.minecraft.nbt.NBTTagCompound;

import com.gtnewhorizons.postea.utility.NbtItemStacks;
import com.gtnewhorizons.postea.utility.VersionStamps;

/// A mod's custom world storage, as handed to [IVersionedTransformer#transformCustomData]. Stacks here may have
/// string `id`s instead of numeric ones, and [#forEachItemStackTag] visits both.
public final class CustomDataTransformContext {

    public static final int UNSTAMPED = VersionStamps.UNSTAMPED;

    private final String storageId;
    private final NBTTagCompound root;
    private final int storedVersion;
    private final int currentVersion;

    public CustomDataTransformContext(String storageId, NBTTagCompound root, int storedVersion, int currentVersion) {
        this.storageId = storageId;
        this.root = root;
        this.storedVersion = storedVersion;
        this.currentVersion = currentVersion;
    }

    /// The storage's identity as its owner registered it, e.g. `"betterquesting:questDatabase"`.
    public String storageId() {
        return storageId;
    }

    public NBTTagCompound root() {
        return root;
    }

    /// The version stamped under the transformer's key when the storage was written, or [#UNSTAMPED].
    public int storedVersion() {
        return storedVersion;
    }

    public int currentVersion() {
        return currentVersion;
    }

    /// Visits every serialized item stack in the storage, including stacks whose `id` is a string name. See
    /// [NbtItemStacks#forEach].
    public void forEachItemStackTag(Consumer<NBTTagCompound> visitor) {
        NbtItemStacks.forEach(root, visitor, true);
    }
}
