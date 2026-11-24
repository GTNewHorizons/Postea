package com.gtnewhorizons.postea.api;

import java.util.function.Consumer;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import com.github.bsideup.jabel.Desugar;
import com.gtnewhorizons.postea.utility.NbtItemStacks;

/// A player's saved data, as handed to [IVersionedTransformer#transformPlayer].
///
/// The root tag holds the inventory, ender chest, `ForgeData` and every extended-properties compound. The player
/// object is constructed but not yet loaded from the tag.
@Desugar
public record PlayerDataTransformContext(EntityPlayerMP player, NBTTagCompound playerTag, int storedVersion) {

    /// Visits every serialized item stack anywhere in the player's data. See [NbtItemStacks#forEach].
    public void forEachItemStackTag(Consumer<NBTTagCompound> visitor) {
        NbtItemStacks.forEach(playerTag, visitor);
    }
}
