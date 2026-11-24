package com.gtnewhorizons.postea.utility;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class IDRegistryTest {

    @TempDir
    File world;

    @AfterEach
    void endWorld() {
        IDRegistry.endWorld();
    }

    @Test
    void theMappingGateOpensOnlyWhenTheBegunWorldsInfoLoads() {
        IDRegistry.beginWorld(world);
        assertFalse(IDRegistry.isMappingApplied());

        IDRegistry.onWorldInfoLoaded();
        assertTrue(IDRegistry.isMappingApplied());

        IDRegistry.endWorld();
        IDRegistry.onWorldInfoLoaded();
        assertFalse(IDRegistry.isMappingApplied());
    }
}
