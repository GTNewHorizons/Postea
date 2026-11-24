package com.gtnewhorizons.postea.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import org.junit.jupiter.api.Test;

class VersionedStackTransformerTest {

    /// Bumps every stack's damage by one, or rewrites nothing when `rewrite` is false.
    private static final class DamageBump extends VersionedStackTransformer {

        private final boolean rewrite;
        final List<Integer> requestedVersions = new ArrayList<>();

        DamageBump(boolean rewrite) {
            this.rewrite = rewrite;
        }

        @Override
        public String key() {
            return "test:damage";
        }

        @Override
        public int currentVersion() {
            return 9;
        }

        @Override
        protected Consumer<NBTTagCompound> stackTransform(int storedVersion) {
            requestedVersions.add(storedVersion);
            return rewrite ? stack -> stack.setShort("Damage", (short) (stack.getShort("Damage") + 1)) : null;
        }
    }

    private static NBTTagCompound holding(String listKey, NBTTagCompound stack) {
        NBTTagList list = new NBTTagList();
        list.appendTag(stack);
        NBTTagCompound holder = new NBTTagCompound();
        holder.setTag(listKey, list);
        return holder;
    }

    private static NBTTagCompound stack() {
        NBTTagCompound stack = new NBTTagCompound();
        stack.setShort("id", (short) 1);
        stack.setByte("Count", (byte) 1);
        return stack;
    }

    /// Runs `transformer` over a chunk, player and custom-data context holding one stack each, and returns the
    /// stacks' damage values in that order.
    private static List<Integer> damageAfter(DamageBump transformer) {
        NBTTagCompound chunkStack = stack();
        NBTTagCompound tileEntity = holding("Items", chunkStack);
        tileEntity.setString("id", "Chest");
        NBTTagCompound levelTag = new NBTTagCompound();
        levelTag.setTag("TileEntities", holding("list", tileEntity).getTagList("list", 10));
        NBTTagCompound playerStack = stack();
        NBTTagCompound customStack = stack();
        customStack.setString("id", "minecraft:stone");

        transformer.transformChunk(new ChunkTransformContext(null, null, levelTag, 1));
        transformer.transformPlayer(new PlayerDataTransformContext(null, holding("Inventory", playerStack), 2));
        transformer
            .transformCustomData(new CustomDataTransformContext("test:storage", holding("Items", customStack), 3));

        return Arrays.asList(
            (int) chunkStack.getShort("Damage"),
            (int) playerStack.getShort("Damage"),
            (int) customStack.getShort("Damage"));
    }

    @Test
    void aStackTransformRewritesStacksInChunksPlayersAndCustomData() {
        DamageBump transformer = new DamageBump(true);

        assertEquals(Arrays.asList(1, 1, 1), damageAfter(transformer));
        assertEquals(Arrays.asList(1, 2, 3), transformer.requestedVersions);
    }

    @Test
    void aNullStackTransformLeavesEveryContextUntouched() {
        assertEquals(Arrays.asList(0, 0, 0), damageAfter(new DamageBump(false)));
    }
}
