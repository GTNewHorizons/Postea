package com.gtnewhorizons.postea.utility;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import com.gtnewhorizons.postea.api.PlayerDataTransformContext;
import com.gtnewhorizons.postea.api.VersionedReplacementManager;

/// Runs versioned transformers over a server player's NBT as it is read, and stamps it as it is written.
///
/// Runs on every server player NBT read and write, which covers `playerdata` and the host player in `level.dat`.
public final class PlayerDataFixerUtility {

    private PlayerDataFixerUtility() {}

    /// Transforms `tag` in place and returns a copy of the stamps it carried, or null if unstamped. A failing
    /// transformer throws and leaves `tag` as read.
    public static NBTTagCompound onPlayerRead(EntityPlayerMP player, NBTTagCompound tag) {
        return VersionedPass.runAtomically(
            tag,
            VersionedReplacementManager.transformers(),
            "Player",
            player.getCommandSenderName() + " (" + player.getUniqueID() + ")",
            (transformer, data, stored) -> transformer
                .transformPlayer(new PlayerDataTransformContext(player, data, stored)));
    }

    public static void onPlayerWrite(NBTTagCompound tag, NBTTagCompound carried) {
        VersionStamps.write(tag, carried, VersionedReplacementManager.transformers());
    }
}
