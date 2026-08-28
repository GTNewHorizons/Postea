package com.gtnewhorizons.postea.utility;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.TreeMap;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;

/**
 * Every namespaced block and item name a world has mapped to a numeric id, with every id it held, oldest first.
 * FML drops a missing name from {@code level.dat} on the first save after its content disappears; this table keeps
 * the name so transformers keyed on it keep resolving in later sessions. Persisted as
 * {@code <world>/postea/known-ids.json}, format version 1:
 *
 * <pre>
 * { "version": 1, "blocks": { "mod:block": [300, 301] }, "items": { "mod:item": [4200] } }
 * </pre>
 */
public final class WorldIdTable {

    static final String FILE_NAME = "known-ids.json";
    private static final int FORMAT_VERSION = 1;

    private final Map<String, IntList> blocks = new TreeMap<>();
    private final Map<String, IntList> items = new TreeMap<>();

    /**
     * Loads {@code file}; an absent file yields an empty table. Throws {@link IllegalStateException} on a file that
     * cannot be read or parsed.
     */
    public static WorldIdTable load(File file) {
        WorldIdTable table = new WorldIdTable();
        if (!file.exists()) return table;
        try {
            JsonObject root = new JsonParser()
                .parse(new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8))
                .getAsJsonObject();
            if (root.get("version")
                .getAsInt() != FORMAT_VERSION) {
                throw new IllegalStateException(file + " has an unsupported format version");
            }
            read(root.getAsJsonObject("blocks"), table.blocks);
            read(root.getAsJsonObject("items"), table.items);
        } catch (IOException | RuntimeException e) {
            throw new IllegalStateException("Cannot read the retired id table " + file, e);
        }
        return table;
    }

    private static void read(JsonObject object, Map<String, IntList> into) {
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            IntList ids = new IntArrayList();
            for (JsonElement id : entry.getValue()
                .getAsJsonArray()) {
                ids.add(id.getAsInt());
            }
            into.put(entry.getKey(), ids);
        }
    }

    /**
     * Writes the table through a temporary file so a crash mid-write never truncates it.
     */
    public void save(File file) {
        JsonObject root = new JsonObject();
        root.addProperty("version", FORMAT_VERSION);
        root.add("blocks", write(blocks));
        root.add("items", write(items));
        File temp = new File(file.getPath() + ".tmp");
        try {
            Files.createDirectories(
                file.toPath()
                    .getParent());
            Files.write(
                temp.toPath(),
                new GsonBuilder().create()
                    .toJson(root)
                    .getBytes(StandardCharsets.UTF_8));
            Files.move(temp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot write the retired id table " + file, e);
        }
    }

    private static JsonObject write(Map<String, IntList> from) {
        JsonObject object = new JsonObject();
        for (Map.Entry<String, IntList> entry : from.entrySet()) {
            JsonArray ids = new JsonArray();
            for (int id : entry.getValue()) {
                ids.add(new JsonPrimitive(id));
            }
            object.add(entry.getKey(), ids);
        }
        return object;
    }

    /**
     * Records a world's saved name-to-id map, in which a name prefixed with U+0001 is a block. Returns whether
     * any pair was new.
     */
    public boolean merge(Map<String, Integer> dataList) {
        boolean changed = false;
        for (Map.Entry<String, Integer> entry : dataList.entrySet()) {
            String prefixed = entry.getKey();
            boolean block = prefixed.charAt(0) == '\u0001';
            Map<String, IntList> into = block ? blocks : items;
            IntList ids = into.computeIfAbsent(block ? prefixed.substring(1) : prefixed, name -> new IntArrayList());
            if (!ids.contains((int) entry.getValue())) {
                ids.add((int) entry.getValue());
                changed = true;
            }
        }
        return changed;
    }

    public IntList blockIds(String name) {
        return blocks.getOrDefault(name, IntLists.EMPTY_LIST);
    }

    public IntList itemIds(String name) {
        return items.getOrDefault(name, IntLists.EMPTY_LIST);
    }
}
