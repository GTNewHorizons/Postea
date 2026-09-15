package com.gtnewhorizons.postea.api;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ReportedException;

import com.gtnewhorizons.postea.Postea;
import com.gtnewhorizons.postea.utility.ChunkFixerUtility;
import com.gtnewhorizons.postea.utility.IDRegistry;
import com.gtnewhorizons.postea.utility.VersionStamps;
import com.gtnewhorizons.postea.utility.VersionedTransformerLog;

import cpw.mods.fml.common.FMLCommonHandler;

/**
 * Versioned transformation of a mod's own world storage -- files outside chunks and player data, such as a quest
 * database or an ender chest table. Postea never reads or writes such files itself; the owning mod offers their
 * content at the moments it controls:
 * <ul>
 * <li>Call {@link #transform} once on the root read from disk, before consuming it, and persist that root (stamps
 * included) when writing the storage back.</li>
 * <li>A save path that rebuilds its NBT from scratch calls {@link #stamp} on the fresh root instead; once
 * {@link #transform} ran at load, the in-memory data is at the current version, so stamping current is correct.</li>
 * <li>A consumer that never writes the storage back (a read-only restore) calls only {@link #transform}: an
 * unstamped file re-transforms from the same baseline on every read, which is correct while its bytes never
 * change.</li>
 * </ul>
 */
public abstract class CustomDataReplacementManager {

    /**
     * Transforms {@code root} in place through every registered transformer whose stamp differs from its current
     * version, then writes the updated stamps onto {@code root}. Does nothing without a running server (a
     * multiplayer client). Throws
     * {@link IllegalStateException} when called before the world's saved id mappings were applied.
     */
    public static void transform(String storageId, NBTTagCompound root) {
        // Not the effective side: mods save their storage from worker threads, which 1.7.10 reports as client.
        if (FMLCommonHandler.instance()
            .getMinecraftServerInstance() == null) return;
        if (!IDRegistry.isMappingApplied()) {
            throw new IllegalStateException(
                "Custom data " + storageId
                    + " was offered for transformation before the world's id mappings were applied");
        }
        NBTTagCompound stamps = VersionStamps.read(root);
        for (IVersionedTransformer transformer : VersionedReplacementManager.transformers()) {
            int current = transformer.currentVersion();
            int stored = VersionStamps.stored(stamps, transformer.key());
            if (stored == current) continue;
            VersionedTransformerLog.announce(transformer, stored, current);
            Postea.LOG.debug("{}: custom data {} from version {} to {}", transformer.key(), storageId, stored, current);
            try {
                transformer.transformCustomData(new CustomDataTransformContext(storageId, root, stored, current));
            } catch (Throwable t) {
                throw new ReportedException(
                    ChunkFixerUtility.describe(t, transformer, stored, current, "Custom data", storageId));
            }
        }
        VersionStamps.write(root, stamps, VersionedReplacementManager.transformers());
    }

    /**
     * Stamps a freshly built root about to be written to disk with every registered transformer's current version.
     * Does nothing without a running server.
     */
    public static void stamp(NBTTagCompound root) {
        if (FMLCommonHandler.instance()
            .getMinecraftServerInstance() == null) return;
        VersionStamps.write(root, VersionStamps.read(root), VersionedReplacementManager.transformers());
    }
}
