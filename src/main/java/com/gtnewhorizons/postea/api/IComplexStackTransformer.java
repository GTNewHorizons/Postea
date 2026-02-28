package com.gtnewhorizons.postea.api;

import net.minecraft.nbt.NBTTagCompound;

@FunctionalInterface
public interface IComplexStackTransformer {

    /**
     * A custom transformer for items.
     *
     * @param originalId The namespaced id of the stack being passed to this transformer.
     * @param tag        The raw nbt data of the stack being transformed.
     * @return A non-null value to prevent any future handlers from executing or if the stack was replaced.
     */
    boolean transformStack(String originalId, NBTTagCompound tag);
}
