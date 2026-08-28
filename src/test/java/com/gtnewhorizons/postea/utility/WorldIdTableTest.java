package com.gtnewhorizons.postea.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

class WorldIdTableTest {

    @TempDir
    File dir;

    private File file() {
        return new File(dir, "known-ids.json");
    }

    private static IntList ids(int... ids) {
        return IntArrayList.wrap(ids);
    }

    private static Map<String, Integer> savedIds(Object... namesAndIds) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < namesAndIds.length; i += 2) {
            map.put((String) namesAndIds[i], (Integer) namesAndIds[i + 1]);
        }
        return map;
    }

    @Test
    void mergeRecordsEveryIdANameHeldInObservationOrderAndReportsChanges() {
        WorldIdTable table = new WorldIdTable();

        assertTrue(table.merge(savedIds("\u0001mod:block", 300, "mod:item", 4200)));
        assertFalse(table.merge(savedIds("\u0001mod:block", 300, "mod:item", 4200)));
        assertTrue(table.merge(savedIds("\u0001mod:block", 301)));

        assertEquals(ids(300, 301), table.blockIds("mod:block"));
        assertEquals(ids(4200), table.itemIds("mod:item"));
        assertEquals(ids(), table.blockIds("mod:item"));
    }

    @Test
    void saveAndLoadRoundTrip() {
        WorldIdTable table = new WorldIdTable();
        table.merge(savedIds("\u0001mod:block", 300, "mod:item", 4200));
        table.merge(savedIds("\u0001mod:block", 301));

        table.save(file());
        WorldIdTable loaded = WorldIdTable.load(file());

        assertEquals(ids(300, 301), loaded.blockIds("mod:block"));
        assertEquals(ids(4200), loaded.itemIds("mod:item"));
    }

    @Test
    void anAbsentFileLoadsEmptyAndAMalformedFileFailsLoudly() throws IOException {
        assertEquals(
            ids(),
            WorldIdTable.load(file())
                .blockIds("mod:block"));

        Files.write(file().toPath(), "{\"version\":1,\"blocks\":5}".getBytes(StandardCharsets.UTF_8));

        assertThrows(IllegalStateException.class, () -> WorldIdTable.load(file()));
    }
}
