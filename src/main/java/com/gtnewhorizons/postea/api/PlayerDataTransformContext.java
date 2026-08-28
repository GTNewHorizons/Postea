package com.gtnewhorizons.postea.api;

import java.util.function.Consumer;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import com.gtnewhorizons.postea.utility.NbtItemStacks;
import com.gtnewhorizons.postea.utility.VersionStamps;

/**
 * A player's saved data as handed to {@link IVersionedTransformer#transformPlayer}: the root tag about to be
 * loaded into the player, which holds the inventory, ender chest, {@code ForgeData}, and every extended-properties
 * compound. The player object is constructed but not yet loaded from the tag.
 */
public final class PlayerDataTransformContext {

    public static final int UNSTAMPED = VersionStamps.UNSTAMPED;

    private final EntityPlayerMP player;
    private final NBTTagCompound playerTag;
    private final int storedVersion;
    private final int currentVersion;

    public PlayerDataTransformContext(EntityPlayerMP player, NBTTagCompound playerTag, int storedVersion,
        int currentVersion) {
        this.player = player;
        this.playerTag = playerTag;
        this.storedVersion = storedVersion;
        this.currentVersion = currentVersion;
    }

    public EntityPlayerMP player() {
        return player;
    }

    public NBTTagCompound playerTag() {
        return playerTag;
    }

    public int storedVersion() {
        return storedVersion;
    }

    public int currentVersion() {
        return currentVersion;
    }

    /**
     * Visits every serialized item stack anywhere in the player's data; see {@link NbtItemStacks#forEach}.
     */
    public void forEachItemStackTag(Consumer<NBTTagCompound> visitor) {
        NbtItemStacks.forEach(playerTag, visitor);
    }
}
