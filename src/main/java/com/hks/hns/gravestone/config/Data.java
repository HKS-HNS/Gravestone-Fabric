package com.hks.hns.gravestone.config;

import com.google.gson.*;
import com.hks.hns.gravestone.BlockWorldPos;
import com.hks.hns.gravestone.Gravestone;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerMetadata;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.core.jmx.Server;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Watchable;
import java.util.HashMap;

public class Data {
    // HashMap that stores the player's inventory at their death location
    static HashMap<BlockWorldPos, Inventory> playerInventory = new HashMap<>();

    // Gson instance with pretty printing enabled
    static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // File to save the player inventory data
    static File saveFile = new File("deaths.json");

    public static HashMap<BlockWorldPos, Inventory> getPlayerInventory() {
        return playerInventory;
    }

    public static void savePlayerInventory() {
        JsonObject jsonObject = new JsonObject();
        JsonArray jsonArray = new JsonArray();

        // Iterate through the HashMap and create a JSON object for each BlockPos and its inventory
        for (BlockWorldPos pos : playerInventory.keySet()) {
            JsonObject posObject = new JsonObject();
            posObject.addProperty("x", pos.getX());
            posObject.addProperty("y", pos.getY());
            posObject.addProperty("z", pos.getZ());
            posObject.addProperty("world", pos.getWorld().toString());

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
                // TODO: Create own BlockPos class that has world as a field
                //get server world with world name

                BlockWorldPos pos = new BlockWorldPos(x, y, z, Identifier.tryParse(world));

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
