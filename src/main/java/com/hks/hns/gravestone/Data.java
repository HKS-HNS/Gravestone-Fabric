package com.hks.hns.gravestone;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hks.hns.gravestone.config.ItemSaver;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;

public class Data {
    static HashMap<BlockPos, Inventory> playerInventory = new HashMap<>();
    static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    static File saveFile = new File("deaths.json");

    public static HashMap<BlockPos, Inventory> getPlayerInventory() {
        return playerInventory;
    }

    public static void savePlayerInventory() {
        JsonObject jsonObject = new JsonObject();
        JsonArray jsonArray = new JsonArray();
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

        try (FileWriter writer = new FileWriter(saveFile)) {
            gson.toJson(jsonObject, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadPlayerInventory() {
        if (!saveFile.exists()) {
            return;
        }
        playerInventory.clear();
        try (FileReader reader = new FileReader(saveFile)) {
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray jsonArray = jsonObject.get("playerInventory").getAsJsonArray();
            for (JsonElement element : jsonArray) {
                JsonObject posObject = element.getAsJsonObject();
                int x = posObject.get("x").getAsInt();
                int y = posObject.get("y").getAsInt();
                int z = posObject.get("z").getAsInt();
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
