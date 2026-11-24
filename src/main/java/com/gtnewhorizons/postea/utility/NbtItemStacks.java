package com.gtnewhorizons.postea.utility;

import static net.minecraftforge.common.util.Constants.NBT.TAG_ANY_NUMERIC;
import static net.minecraftforge.common.util.Constants.NBT.TAG_COMPOUND;
import static net.minecraftforge.common.util.Constants.NBT.TAG_INT;
import static net.minecraftforge.common.util.Constants.NBT.TAG_SHORT;
import static net.minecraftforge.common.util.Constants.NBT.TAG_STRING;

import java.util.function.Consumer;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

/// Visits every serialized item stack in an NBT tree.
///
/// A compound is a stack when it has a `Count` and a numeric `id` or an EndlessIDs `idExt`. In string-id mode, a
/// string `id` such as a registry name also qualifies. String-id mode must not be used on chunk or player trees,
/// where tile entity compounds carry a string `id` of their own.
///
/// The walk is pre-order and descends into every compound and every list of compounds, including a stack's own
/// `tag`. Lists of lists are not descended. A visitor may change or remove keys of the stack it is given, and must
/// not touch other parts of the tree.
public final class NbtItemStacks {

    private NbtItemStacks() {}

    public static void forEach(NBTBase root, Consumer<NBTTagCompound> visitor) {
        forEach(root, visitor, false);
    }

    public static void forEach(NBTBase root, Consumer<NBTTagCompound> visitor, boolean stringIds) {
        if (root instanceof NBTTagCompound compound) {
            if (isStack(compound, stringIds)) visitor.accept(compound);
            for (Object key : compound.func_150296_c()) {
                forEach(compound.getTag((String) key), visitor, stringIds);
            }
        } else if (root instanceof NBTTagList list && list.func_150303_d() == TAG_COMPOUND) {
            for (int i = 0; i < list.tagCount(); i++) {
                forEach(list.getCompoundTagAt(i), visitor, stringIds);
            }
        }
    }

    public static boolean isStack(NBTTagCompound compound) {
        return isStack(compound, false);
    }

    public static boolean isStack(NBTTagCompound compound, boolean stringIds) {
        return compound.hasKey("Count", TAG_ANY_NUMERIC)
            && (compound.hasKey("id", TAG_SHORT) || compound.hasKey("id", TAG_INT)
                || compound.hasKey("idExt", TAG_INT)
                || (stringIds && compound.hasKey("id", TAG_STRING)));
    }
}
