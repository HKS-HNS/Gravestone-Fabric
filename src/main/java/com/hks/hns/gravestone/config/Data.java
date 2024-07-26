package com.hks.hns.gravestone.config;

import com.google.gson.*;
import com.hks.hns.gravestone.BlockWorldPos;
import com.hks.hns.gravestone.Gravestone;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Data {
    // HashMap that stores the player's inventory at their death location
    static ConcurrentHashMap<BlockWorldPos, Inventory> playerInventory = new ConcurrentHashMap<>();

    // Gson instance with pretty printing enabled
    static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // File to save the player inventory data
    static File saveFile = new File("deaths.json");

    public static ConcurrentHashMap<BlockWorldPos, Inventory> getPlayerInventory() {
        return playerInventory;
    }

    /**
     * Saves the player inventory data to a JSON file.
     * Each BlockPos and its corresponding inventory are saved as a JSON object.
     * The world registry and world ID are also saved for each BlockPos.
     */
    public static void savePlayerInventory() {
        JsonObject jsonObject = new JsonObject();
        JsonArray jsonArray = new JsonArray();

        // Iterate through the HashMap and create a JSON object for each BlockPos and its inventory
        for (BlockWorldPos pos : playerInventory.keySet()) {
            JsonObject posObject = new JsonObject();
            posObject.addProperty("x", pos.getX());
            posObject.addProperty("y", pos.getY());
            posObject.addProperty("z", pos.getZ());
            posObject.addProperty("worldRegistry", String.valueOf(pos.getWorld().getRegistryKey().getRegistry().toString()));
            posObject.addProperty("world", String.valueOf(pos.getWorld().getRegistryKey().getValue().toString()));

            JsonArray inventoryArray = new JsonArray();
            Inventory inventory = playerInventory.get(pos);

            for (int i = 0; i < inventory.size(); i++) {
                if (!inventory.getStack(i).isEmpty()) {
                    inventoryArray.add(ItemSaver.serializeItemStack(inventory.getStack(i)));

                }
            }

            posObject.add("inventory", inventoryArray);
            jsonArray.add(posObject);
        }

        jsonObject.add("playerInventory", jsonArray);

        // Write the JSON data to the save file
        try (FileWriter writer = new FileWriter(saveFile)) {
            gson.toJson(jsonObject, writer);

        } catch (IOException e) {
            Logger.getGlobal().log(Level.SEVERE, "Error saving player inventory data", e);
        }
    }

    /**
     * Loads the player inventory data from a JSON file.
     * Each JSON object in the file represents a BlockPos and its corresponding inventory.
     * The world registry and world ID are also loaded for each BlockPos.
     */
    public static void loadPlayerInventory() {
        // If the save file does not exist, return
        MinecraftServer server = Gravestone.getServer();

        if (!saveFile.exists() || server == null) {
            return;
        }

        // Clear the HashMap
        playerInventory.clear();

        try (FileReader reader = new FileReader(saveFile)) {
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray jsonArray = jsonObject.get("playerInventory").getAsJsonArray();

            // Iterate through the JSON data and create a BlockPos and inventory for each
            for (JsonElement element : jsonArray) {
                JsonObject posObject = element.getAsJsonObject();

                int x = posObject.get("x").getAsInt();
                int y = posObject.get("y").getAsInt();
                int z = posObject.get("z").getAsInt();

                String world = posObject.get("world").getAsString();
                String worldRegistry = posObject.get("worldRegistry").getAsString();

                Identifier dimensionId = Identifier.tryParse(worldRegistry);
                Identifier worldId = Identifier.of(world);

                BlockWorldPos pos = new BlockWorldPos(x, y, z, server.getWorld(RegistryKey.of(RegistryKey.ofRegistry(dimensionId), worldId)));

                JsonArray inventoryArray = posObject.get("inventory").getAsJsonArray();
                Inventory inventory = new SimpleInventory(54);

                for (int i = 0; i < inventoryArray.size(); i++) {
                    if (!inventoryArray.get(i).isJsonNull()) {
                        ItemStack itemStack = ItemSaver.deserializeItemStack(inventoryArray.get(i).getAsJsonObject());
                        inventory.setStack(i, itemStack);

                    }
                }

                playerInventory.put(pos, inventory);
            }
        } catch (IOException e) {
            Logger.getGlobal().log(Level.SEVERE, "Error loading player inventory data", e);
        }
    }
}