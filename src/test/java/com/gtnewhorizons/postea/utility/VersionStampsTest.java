package com.gtnewhorizons.postea.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Collections;

import net.minecraft.nbt.NBTTagCompound;

import org.junit.jupiter.api.Test;

import com.gtnewhorizons.postea.api.IVersionedTransformer;
import com.gtnewhorizons.postea.api.StubTransformer;

class VersionStampsTest {

    @Test
    void anUnstampedContainerReadsAsUnstamped() {
        NBTTagCompound container = new NBTTagCompound();

        assertNull(VersionStamps.read(container));
        assertEquals(IVersionedTransformer.UNSTAMPED, VersionStamps.stored(null, "mod:key"));
    }

    @Test
    void writeWithoutCarriedStampsWritesOnlyRegisteredKeys() {
        NBTTagCompound container = new NBTTagCompound();

        VersionStamps.write(container, null, Collections.singletonList(new StubTransformer("mod:key", 1)));

        assertEquals(1, VersionStamps.stored(VersionStamps.read(container), "mod:key"));
        assertEquals(IVersionedTransformer.UNSTAMPED, VersionStamps.stored(VersionStamps.read(container), "other:key"));
    }
}
