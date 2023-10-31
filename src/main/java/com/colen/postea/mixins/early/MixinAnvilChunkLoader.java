package com.colen.postea.mixins.early;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.colen.postea.Utility.ChunkFixerUtility.processChunkNBT;

@Mixin(AnvilChunkLoader.class)
@SuppressWarnings("unused")
public abstract class MixinAnvilChunkLoader {

    @Inject(method = "checkedReadChunkFromNBT__Async", at = @At("HEAD"), remap = false)
    private void onCheckedReadChunkFromNBT__Async(World world, int x, int z, NBTTagCompound compound, CallbackInfoReturnable<Object[]> cir) {
        // Your injection code here
        processChunkNBT(compound, world);
    }

}
