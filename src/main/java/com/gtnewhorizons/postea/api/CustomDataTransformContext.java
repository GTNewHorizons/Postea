package com.gtnewhorizons.postea.api;

import java.util.function.Consumer;

import net.minecraft.nbt.NBTTagCompound;

import com.gtnewhorizons.postea.utility.NbtItemStacks;
import com.gtnewhorizons.postea.utility.VersionStamps;

/**
 * A mod's own world storage as handed to {@link IVersionedTransformer#transformCustomData}: the NBT root the
 * owning mod read from disk, before it consumes the data. Unlike chunk and player data, stacks here may carry a
 * string {@code id} (a registry name, as quest databases store) instead of a numeric one;
 * {@link #forEachItemStackTag} visits both forms.
 */
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

    /**
     * The storage's identity as its owner registered it, e.g. {@code "betterquesting:questDatabase"}.
     */
    public String storageId() {
        return storageId;
    }

    public NBTTagCompound root() {
        return root;
    }

    /**
     * The version this transformer's key was stamped with when the storage was written, or {@link #UNSTAMPED}.
     */
    public int storedVersion() {
        return storedVersion;
    }

    public int currentVersion() {
        return currentVersion;
    }

    /**
     * Visits every serialized item stack in the storage, including stacks whose {@code id} is a string name; see
     * {@link NbtItemStacks#forEach}.
     */
    public void forEachItemStackTag(Consumer<NBTTagCompound> visitor) {
        NbtItemStacks.forEach(root, visitor, true);
    }
}
