package com.gtnewhorizons.postea.utility;

import static net.minecraftforge.common.util.Constants.NBT.TAG_COMPOUND;
import static net.minecraftforge.common.util.Constants.NBT.TAG_INT;

import net.minecraft.nbt.NBTTagCompound;

import com.gtnewhorizons.postea.api.IVersionedTransformer;

/**
 * The {@code POSTEA_VERSIONS} compound Postea keeps on a chunk's {@code Level} tag and on a player's root tag: one
 * int per registered transformer key. Keys of transformers that are not registered in the running session are
 * carried through unchanged so a mod that is temporarily absent does not lose its stamps.
 */
public final class VersionStamps {

    public static final String KEY = "POSTEA_VERSIONS";
    public static final int UNSTAMPED = -1;

    private VersionStamps() {}

    /**
     * The stamps stored on {@code container}, or null when it carries none.
     */
    public static NBTTagCompound read(NBTTagCompound container) {
        return container.hasKey(KEY, TAG_COMPOUND) ? container.getCompoundTag(KEY) : null;
    }

    /**
     * The version stored for {@code key} in {@code stamps} (which may be null), or {@link #UNSTAMPED}.
     */
    public static int stored(NBTTagCompound stamps, String key) {
        return stamps != null && stamps.hasKey(key, TAG_INT) ? stamps.getInteger(key) : UNSTAMPED;
    }

    /**
     * Writes the stamps onto {@code container}: {@code carried} (the stamps the data was read with, or null) with
     * every registered transformer's key set to its current version.
     */
    public static void write(NBTTagCompound container, NBTTagCompound carried,
        Iterable<IVersionedTransformer> transformers) {
        NBTTagCompound stamps = carried == null ? new NBTTagCompound() : (NBTTagCompound) carried.copy();
        for (IVersionedTransformer transformer : transformers) {
            stamps.setInteger(transformer.key(), transformer.currentVersion());
        }
        container.setTag(KEY, stamps);
    }
}
