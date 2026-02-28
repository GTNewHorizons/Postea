package com.gtnewhorizons.postea.mixins.preinit;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.gtnewhorizons.postea.utility.IDRegistry;

import cpw.mods.fml.common.registry.GameData;

@Mixin(value = GameData.class, remap = false)
public abstract class MixinGameData {

    @Inject(
        method = "injectWorldIDMap(Ljava/util/Map;Ljava/util/Set;Ljava/util/Map;Ljava/util/Map;Ljava/util/Set;Ljava/util/Set;ZZ)Ljava/util/List;",
        at = @At("HEAD"),
        require = 1)
    @SuppressWarnings("unused")
    private static void Postea$injectWorldIDMap(Map<String, Integer> dataList, Set<Integer> blockedIds,
        Map<String, String> blockAliases, Map<String, String> itemAliases, Set<String> blockSubstitutions,
        Set<String> itemSubstitutions, boolean injectFrozenData, boolean isLocalWorld,
        CallbackInfoReturnable<List<String>> cir) {
        IDRegistry.registerWorldSpecificIDs(dataList);
    }
}
