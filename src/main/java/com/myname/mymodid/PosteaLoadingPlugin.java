package com.myname.mymodid;

import com.falsepattern.gasstation.IEarlyMixinLoader;
import cpw.mods.fml.relauncher.FMLLaunchHandler;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import jdk.jfr.Name;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@IFMLLoadingPlugin.TransformerExclusions({ "mods.mymod" })
@Name("mymod")
@IFMLLoadingPlugin.SortingIndex(1500)
@IFMLLoadingPlugin.MCVersion("1.7.10")
public final class PosteaLoadingPlugin implements IEarlyMixinLoader, IFMLLoadingPlugin {

    public static final String ItemStackTransformer = "mods.battlegear2.coremod.transformers.EntityPlayerTransformer";

    public static File debugOutputLocation;
    public static final String[] transformers = new String[] { ItemStackTransformer };

    @Override
    public String[] getASMTransformerClass() {
        return transformers;
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        debugOutputLocation = new File(data.get("mcLocation").toString(), "ItemStack edited classes");
    }

    @Override
    public List<String> getMixinConfigs() {
        List<String> list = new ArrayList<>();
        list.add("mixins.json" );
        return list;
    }

}
