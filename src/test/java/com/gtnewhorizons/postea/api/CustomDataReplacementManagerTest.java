package com.gtnewhorizons.postea.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Collections;

import net.minecraft.nbt.NBTTagCompound;

import org.junit.jupiter.api.Test;

import com.gtnewhorizons.postea.utility.VersionStamps;

class CustomDataReplacementManagerTest {

    @Test
    void aRebuiltRootKeepsTheStampOfATransformerThatIsNotRegistered() {
        StubTransformer present = new StubTransformer("mod:present", 2);
        StubTransformer absent = new StubTransformer("mod:absent", 5);
        NBTTagCompound original = new NBTTagCompound();
        CustomDataReplacementManager.transform("mod:storage", original, Arrays.asList(present, absent));
        NBTTagCompound saved = CustomDataReplacementManager.savedStamps(original);

        present.version = 3;
        NBTTagCompound rebuilt = new NBTTagCompound();
        CustomDataReplacementManager.stamp(rebuilt, saved, Collections.singletonList(present));

        NBTTagCompound stamps = VersionStamps.read(rebuilt);
        assertEquals(3, VersionStamps.stored(stamps, "mod:present"));
        assertEquals(5, VersionStamps.stored(stamps, "mod:absent"));
        assertEquals(2, VersionStamps.stored(saved, "mod:present"));
    }

    @Test
    void aThrowingTransformerLeavesTheRootAsRead() {
        StubTransformer rewrites = new StubTransformer("mod:rewrites", 2);
        rewrites.customData = ctx -> ctx.root()
            .setString("data", "new");
        StubTransformer fails = new StubTransformer("mod:fails", 2);
        fails.customData = ctx -> { throw new IllegalStateException("broken"); };
        NBTTagCompound root = new NBTTagCompound();
        root.setString("data", "old");
        NBTTagCompound asRead = (NBTTagCompound) root.copy();

        // The ReportedException's CrashReport initializes FML, which fails outside a launched game.
        assertThrows(
            Throwable.class,
            () -> CustomDataReplacementManager.transform("mod:storage", root, Arrays.asList(rewrites, fails)));
        assertEquals(asRead, root);
    }
}
