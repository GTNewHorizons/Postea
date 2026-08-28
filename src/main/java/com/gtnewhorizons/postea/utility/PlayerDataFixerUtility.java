package com.gtnewhorizons.postea.utility;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ReportedException;

import com.gtnewhorizons.postea.api.IVersionedTransformer;
import com.gtnewhorizons.postea.api.PlayerDataTransformContext;
import com.gtnewhorizons.postea.api.VersionedReplacementManager;

/**
 * Runs versioned transformers over a server player's NBT as it is read, and stamps it as it is written. Hooked on
 * {@code Entity.readFromNBT}/{@code writeToNBT} so both the {@code playerdata/<uuid>.dat} files and the
 * single-player host's copy inside {@code level.dat} are covered.
 */
public final class PlayerDataFixerUtility {

    private PlayerDataFixerUtility() {}

    /**
     * Transforms {@code tag} in place and returns a copy of the stamps it carried (null when unstamped).
     */
    public static NBTTagCompound onPlayerRead(EntityPlayerMP player, NBTTagCompound tag) {
        NBTTagCompound stamps = VersionStamps.read(tag);
        for (IVersionedTransformer transformer : VersionedReplacementManager.transformers()) {
            int current = transformer.currentVersion();
            int stored = VersionStamps.stored(stamps, transformer.key());
            if (stored == current) continue;
            VersionedTransformerLog.announce(transformer, stored, current);
            try {
                transformer.transformPlayer(new PlayerDataTransformContext(player, tag, stored, current));
            } catch (Throwable t) {
                throw new ReportedException(
                    ChunkFixerUtility.describe(
                        t,
                        transformer,
                        stored,
                        current,
                        "Player",
                        player.getCommandSenderName() + " (" + player.getUniqueID() + ")"));
            }
        }
        return stamps == null ? null : (NBTTagCompound) stamps.copy();
    }

    public static void onPlayerWrite(NBTTagCompound tag, NBTTagCompound carried) {
        VersionStamps.write(tag, carried, VersionedReplacementManager.transformers());
    }
}
