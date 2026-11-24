package com.gtnewhorizons.postea.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;

import net.minecraft.nbt.NBTTagCompound;

import org.junit.jupiter.api.Test;

import com.gtnewhorizons.postea.utility.VersionStamps;
import com.gtnewhorizons.postea.utility.VersionedPass;

class VersionedPassTest {

    /// Data saved while a later step of the same load fails must not be transformed a second time on the next load.
    @Test
    void aTransformedRootCarriesTheCurrentStamps() {
        StubTransformer transformer = new StubTransformer("mod:t", 2);
        NBTTagCompound root = new NBTTagCompound();
        root.setString("data", "old");

        VersionedPass.runAtomically(
            root,
            Collections.singletonList(transformer),
            "Player",
            "someone",
            (t, data, stored) -> data.setString("data", "new"));

        assertEquals("new", root.getString("data"));
        assertEquals(2, VersionStamps.stored(VersionStamps.read(root), "mod:t"));
    }
}
