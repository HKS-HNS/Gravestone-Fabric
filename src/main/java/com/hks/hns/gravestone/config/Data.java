package com.hks.hns.gravestone.config;

import com.google.gson.*;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class Data {
    // HashMap that stores the player's inventory at their death location
    static HashMap<BlockPos, Inventory> playerInventory = new HashMap<>();

    // Gson instance with pretty printing enabled
    static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // File to save the player inventory data
    static File saveFile = new File("deaths.json");

    public static HashMap<BlockPos, Inventory> getPlayerInventory() {
        return playerInventory;
    }

    public static void savePlayerInventory() {
        JsonObject jsonObject = new JsonObject();
        JsonArray jsonArray = new JsonArray();

        // Iterate through the HashMap and create a JSON object for each BlockPos and its inventory
        for (BlockPos pos : playerInventory.keySet()) {
            JsonObject posObject = new JsonObject();
            posObject.addProperty("x", pos.getX());
            posObject.addProperty("y", pos.getY());
            posObject.addProperty("z", pos.getZ());

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
            e.printStackTrace();
        }
    }

    public static void loadPlayerInventory() {
        // If the save file does not exist, return
        if (!saveFile.exists()) {
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

                // TODO: Create own BlockPos class that has world as a field
                BlockPos pos = new BlockPos(x, y, z);

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
            e.printStackTrace();
        }
    }
}
