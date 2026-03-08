package com.gtnewhorizons.postea.mixins;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.gtnewhorizon.gtnhmixins.ILateMixinLoader;
import com.gtnewhorizon.gtnhmixins.LateMixin;

import cpw.mods.fml.common.Loader;

@LateMixin
public final class PosteaLateLoadingPlugin implements ILateMixinLoader {

    @Override
    public String getMixinConfig() {
        return "mixins.postea.late.json";
    }

    @Override
    public List<String> getMixins(Set<String> loadedCoreMods) {
        final List<String> mixins = new ArrayList<>();
        if (Loader.isModLoaded("chunkapi")) {
            mixins.add("MixinDataRegistryImpl");
        }
        return mixins;
    }
}
