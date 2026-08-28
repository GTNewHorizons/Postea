package com.gtnewhorizons.postea.utility;

import static net.minecraftforge.common.util.Constants.NBT.TAG_ANY_NUMERIC;
import static net.minecraftforge.common.util.Constants.NBT.TAG_COMPOUND;
import static net.minecraftforge.common.util.Constants.NBT.TAG_INT;
import static net.minecraftforge.common.util.Constants.NBT.TAG_SHORT;

import java.util.function.Consumer;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

/**
 * Visits every serialized item stack in an NBT tree. A compound is a stack when it has a numeric {@code id} or an
 * EndlessIDs {@code idExt} together with a {@code Count}. The walk is pre-order and descends into every compound
 * and every list of compounds, so stacks nested inside another stack's {@code tag} (backpacks, storage cells) are
 * visited too. Lists of lists are not descended. A visitor may change or remove keys of the stack it is given; it
 * must not touch other parts of the tree.
 */
public final class NbtItemStacks {

    private NbtItemStacks() {}

    public static void forEach(NBTBase root, Consumer<NBTTagCompound> visitor) {
        if (root instanceof NBTTagCompound compound) {
            if (isStack(compound)) visitor.accept(compound);
            for (Object key : compound.func_150296_c()) {
                forEach(compound.getTag((String) key), visitor);
            }
        } else if (root instanceof NBTTagList list && list.func_150303_d() == TAG_COMPOUND) {
            for (int i = 0; i < list.tagCount(); i++) {
                forEach(list.getCompoundTagAt(i), visitor);
            }
        }
    }

    public static boolean isStack(NBTTagCompound compound) {
        return compound.hasKey("Count", TAG_ANY_NUMERIC)
            && (compound.hasKey("id", TAG_SHORT) || compound.hasKey("id", TAG_INT)
                || compound.hasKey("idExt", TAG_INT));
    }
}
