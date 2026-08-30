package com.gtnewhorizons.postea.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Collections;

import net.minecraft.nbt.NBTTagCompound;

import org.junit.jupiter.api.Test;

import com.gtnewhorizons.postea.api.IVersionedTransformer;

class VersionStampsTest {

    private static IVersionedTransformer transformer(String key, int version) {
        return new IVersionedTransformer() {

            @Override
            public String key() {
                return key;
            }

            @Override
            public int currentVersion() {
                return version;
            }
        };
    }

    @Test
    void anUnstampedContainerReadsAsUnstamped() {
        NBTTagCompound container = new NBTTagCompound();

        assertNull(VersionStamps.read(container));
        assertEquals(VersionStamps.UNSTAMPED, VersionStamps.stored(null, "mod:key"));
    }

    @Test
    void writeStampsRegisteredKeysAtTheirCurrentVersionAndCarriesUnknownKeys() {
        NBTTagCompound carried = new NBTTagCompound();
        carried.setInteger("absentmod:key", 7);
        carried.setInteger("mod:key", 2);
        NBTTagCompound container = new NBTTagCompound();

        VersionStamps.write(container, carried, Collections.singletonList(transformer("mod:key", 3)));

        NBTTagCompound stamps = VersionStamps.read(container);
        assertEquals(3, VersionStamps.stored(stamps, "mod:key"));
        assertEquals(7, VersionStamps.stored(stamps, "absentmod:key"));
        assertEquals(2, carried.getInteger("mod:key"));
    }

    @Test
    void writeWithoutCarriedStampsWritesOnlyRegisteredKeys() {
        NBTTagCompound container = new NBTTagCompound();

        VersionStamps.write(container, null, Collections.singletonList(transformer("mod:key", 1)));

        assertEquals(1, VersionStamps.stored(VersionStamps.read(container), "mod:key"));
        assertEquals(VersionStamps.UNSTAMPED, VersionStamps.stored(VersionStamps.read(container), "other:key"));
    }
}
