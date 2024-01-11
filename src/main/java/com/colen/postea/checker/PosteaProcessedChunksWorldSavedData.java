package com.colen.postea.checker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.storage.MapStorage;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;

import akka.japi.Pair;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class PosteaProcessedChunksWorldSavedData extends WorldSavedData {

    private static final String DATA_NAME = "GregTech_GlobalIntValue";
    public static HashSet<Pair<Integer, Integer>> chunkCoordinates = new HashSet<>();
    public static PosteaProcessedChunksWorldSavedData INSTANCE;

    // True if chunk has not been processed, else false.
    public static boolean addChunk(int x, int z) {
        boolean updateChunk = chunkCoordinates.add(new Pair<>(x, z));

        if (updateChunk) {
            // Only mark the world NBT as dirty if we actually process a new chunk, since we will need to save that
            // info.
            INSTANCE.markDirty();
        }

        return updateChunk;
    }

    public PosteaProcessedChunksWorldSavedData() {
        super(DATA_NAME);
    }

    public PosteaProcessedChunksWorldSavedData(String name) {
        super(name);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTagCompound) {
        try {
            byte[] ba = nbtTagCompound.getByteArray(DATA_NAME);
            InputStream byteArrayInputStream = new ByteArrayInputStream(ba);
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            Object data = objectInputStream.readObject();
            chunkCoordinates = (HashSet<Pair<Integer, Integer>>) data;
        } catch (IOException | ClassNotFoundException exception) {
            System.out.println(DATA_NAME + " FAILED");
            exception.printStackTrace();
        }

    }

    @Override
    public void writeToNBT(NBTTagCompound nbtTagCompound) {
        // Write the integer value to NBT
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(chunkCoordinates);
            objectOutputStream.flush();
            byte[] data = byteArrayOutputStream.toByteArray();
            nbtTagCompound.setByteArray(DATA_NAME, data);
        } catch (IOException exception) {
            System.out.println(DATA_NAME + " SAVE FAILED");
            exception.printStackTrace();
        }
    }

    public static void get(World world) {

        chunkCoordinates.clear();

        MapStorage storage = world.mapStorage;
        INSTANCE = (PosteaProcessedChunksWorldSavedData) storage
            .loadData(PosteaProcessedChunksWorldSavedData.class, DATA_NAME);

        if (INSTANCE == null) {
            INSTANCE = new PosteaProcessedChunksWorldSavedData();
            storage.setData(DATA_NAME, INSTANCE);
        }

    }

    // Register event in a static block to ensure it's done as early as possible
    static {
        MinecraftForge.EVENT_BUS.register(new PosteaProcessedChunksWorldSavedData());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onWorldLoad(WorldEvent.Load event) {
        if (!event.world.isRemote && event.world.provider.dimensionId == 0) {
            get(event.world);
        }
    }
}
