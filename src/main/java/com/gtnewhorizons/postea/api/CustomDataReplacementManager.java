package com.gtnewhorizons.postea.api;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ReportedException;

import com.gtnewhorizons.postea.Postea;
import com.gtnewhorizons.postea.utility.ChunkFixerUtility;
import com.gtnewhorizons.postea.utility.IDRegistry;
import com.gtnewhorizons.postea.utility.VersionStamps;
import com.gtnewhorizons.postea.utility.VersionedTransformerLog;

import cpw.mods.fml.common.FMLCommonHandler;

/// Applies [IVersionedTransformer]s to a mod's custom storage files.
///
/// Call [#transform] once on the root read from disk, before consuming it, and persist that root, stamps included,
/// when writing the storage back. A save path that rebuilds its NBT from scratch keeps [#savedStamps] from the read and
/// passes them to [#stamp(NBTTagCompound, NBTTagCompound)] on the fresh root. The stamps of transformers missing at
/// save time then survive.
///
/// Custom data belongs to a world, so [#transform] and [#stamp] do nothing unless a server runs in this JVM.
public abstract class CustomDataReplacementManager {

    /// Transforms `root` in place through every registered transformer whose stamp differs from its current
    /// version, then writes the updated stamps onto `root`. Throws [IllegalStateException] when called before the
    /// world's info and saved id map have loaded.
    public static void transform(String storageId, NBTTagCompound root) {
        if (!serverRunning()) return;
        if (!IDRegistry.isMappingApplied()) {
            throw new IllegalStateException(
                "Custom data " + storageId
                    + " was offered for transformation before the world's info and id mappings loaded");
        }
        transform(storageId, root, VersionedReplacementManager.transformers());
    }

    /// [#transform(String, NBTTagCompound)] through `transformers`, without the server and id mapping checks.
    static void transform(String storageId, NBTTagCompound root, Iterable<IVersionedTransformer> transformers) {
        NBTTagCompound stamps = VersionStamps.read(root);
        for (IVersionedTransformer transformer : transformers) {
            int current = transformer.currentVersion();
            int stored = VersionStamps.stored(stamps, transformer.key());
            if (stored == current) continue;
            VersionedTransformerLog.announce(transformer, stored, current);
            Postea.LOG.debug("{}: custom data {} from version {} to {}", transformer.key(), storageId, stored, current);
            try {
                transformer.transformCustomData(new CustomDataTransformContext(storageId, root, stored));
            } catch (Throwable t) {
                throw new ReportedException(
                    ChunkFixerUtility.describe(t, transformer, stored, current, "Custom data", storageId));
            }
        }
        VersionStamps.write(root, stamps, transformers);
    }

    /// Same as [#stamp(NBTTagCompound, NBTTagCompound)] with a null `carried`.
    public static void stamp(NBTTagCompound root) {
        stamp(root, null);
    }

    /// Stamps `root` with every registered transformer's current version, keeping the stamps in `carried` for
    /// transformers not registered now. A non-null `carried` replaces any stamps already on `root`. A null `carried`
    /// keeps them.
    public static void stamp(NBTTagCompound root, NBTTagCompound carried) {
        if (!serverRunning()) return;
        stamp(root, carried, VersionedReplacementManager.transformers());
    }

    static void stamp(NBTTagCompound root, NBTTagCompound carried, Iterable<IVersionedTransformer> transformers) {
        VersionStamps.write(root, carried == null ? VersionStamps.read(root) : carried, transformers);
    }

    /// A copy of the stamps on a root read from disk, or null when it carries none. Call after [#transform].
    public static NBTTagCompound savedStamps(NBTTagCompound root) {
        NBTTagCompound stamps = VersionStamps.read(root);
        return stamps == null ? null : (NBTTagCompound) stamps.copy();
    }

    private static boolean serverRunning() {
        // Not getEffectiveSide(): mods save from worker threads, which 1.7.10 reports as client.
        return FMLCommonHandler.instance()
            .getMinecraftServerInstance() != null;
    }
}
