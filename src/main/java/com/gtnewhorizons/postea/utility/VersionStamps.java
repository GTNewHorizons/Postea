package com.gtnewhorizons.postea.utility;

import static net.minecraftforge.common.util.Constants.NBT.TAG_COMPOUND;
import static net.minecraftforge.common.util.Constants.NBT.TAG_INT;

import net.minecraft.nbt.NBTTagCompound;

import com.gtnewhorizons.postea.api.IVersionedTransformer;

/// The `POSTEA_VERSIONS` compound Postea keeps on data handled by [IVersionedTransformer]s, with one int per
/// transformer key.
///
/// Keys of transformers not registered in the running session are carried through unchanged.
public final class VersionStamps {

    public static final String KEY = "POSTEA_VERSIONS";

    private VersionStamps() {}

    /// The stamps stored on `container`, or null if it carries none.
    public static NBTTagCompound read(NBTTagCompound container) {
        return container.hasKey(KEY, TAG_COMPOUND) ? container.getCompoundTag(KEY) : null;
    }

    /// The version stored for `key` in `stamps`, or [IVersionedTransformer#UNSTAMPED]. `stamps` may be null.
    public static int stored(NBTTagCompound stamps, String key) {
        return stamps != null && stamps.hasKey(key, TAG_INT) ? stamps.getInteger(key) : IVersionedTransformer.UNSTAMPED;
    }

    /// Writes the stamps onto `container`: `carried`, the stamps the data was read with or null, with every
    /// transformer's key set to its current version.
    public static void write(NBTTagCompound container, NBTTagCompound carried,
        Iterable<IVersionedTransformer> transformers) {
        NBTTagCompound stamps = carried == null ? new NBTTagCompound() : (NBTTagCompound) carried.copy();
        for (IVersionedTransformer transformer : transformers) {
            stamps.setInteger(transformer.key(), transformer.currentVersion());
        }
        container.setTag(KEY, stamps);
    }
}
