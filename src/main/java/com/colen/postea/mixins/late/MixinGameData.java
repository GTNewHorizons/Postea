package com.colen.postea.mixins.late;

import com.colen.postea.mixins.IGameDataAccessor;
import cpw.mods.fml.common.registry.FMLControlledNamespacedRegistry;
import cpw.mods.fml.common.registry.GameData;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(GameData.class)
public class MixinGameData implements IGameDataAccessor {

    @Unique
    protected native static GameData getMain();
    @Shadow(remap = false)
    private static FMLControlledNamespacedRegistry<Block> iBlockRegistry;
    @Shadow(remap = false)
    private static FMLControlledNamespacedRegistry<Item> iItemRegistry;

    @Override
    public FMLControlledNamespacedRegistry<Block> getIBlockRegistry() {
        return iBlockRegistry;
    }

    @Override
    public FMLControlledNamespacedRegistry<Item> getIItemRegistry() {
        return iItemRegistry;
    }

    /**
     * @author Colen
     * @reason Stuff
     */
    @Overwrite(remap = false)
    static Item findItem(String modId, String name) {
        IGameDataAccessor accessor = (IGameDataAccessor) getMain();
        return accessor.getIItemRegistry().getObject(modId + ":" + name);
    }

    /**
     * @author Colen
     * @reason Stuff
     */
    @Overwrite(remap = false)
    static Block findBlock(String modId, String name) {
        String key = "minecraft:bedrock";
        IGameDataAccessor accessor = (IGameDataAccessor) getMain();
        return accessor.getIBlockRegistry().containsKey(key) ? accessor.getIBlockRegistry().getObject(key) : null;
    }

}
