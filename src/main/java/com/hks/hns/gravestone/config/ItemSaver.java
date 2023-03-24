package com.hks.hns.gravestone.config;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import net.minecraft.item.ItemStack;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;

public class ItemSaver {

    /**
     * Save an ItemStack to a file
     *
     * @param stack    the ItemStack to save
     * @param filename the name of the file to save to
     * @throws IOException if there is an error writing to the file
     */
    public static void saveItem(ItemStack stack, String filename) throws IOException {
        Gson gson = new Gson();
        // Serialize the ItemStack to a Json string
        String json = gson.toJson(serializeItemStack(stack));
        // Write the Json string to the file
        FileWriter writer = new FileWriter(filename);
        writer.write(json);
        writer.close();
    }

    /**
     * Load an ItemStack from a file
     *
     * @param filename the name of the file to load from
     * @return the loaded ItemStack
     * @throws IOException if there is an error reading from the file
     */
    public static ItemStack loadItem(String filename) throws IOException {
        Gson gson = new Gson();
        // Read the Json string from the file
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String json = reader.readLine();
        reader.close();
        // Deserialize the Json string to an ItemStack
        return deserializeItemStack(gson.fromJson(json, JsonElement.class));
    }

    /**
     * Serialize an ItemStack to a JsonElement
     *
     * @param stack the ItemStack to serialize
     * @return the serialized JsonElement
     */
    public static JsonElement serializeItemStack(ItemStack stack) {
        var json = ItemStack.CODEC.encode(stack, JsonOps.INSTANCE, JsonOps.INSTANCE.empty());
        if (json.result().isPresent()) {
            return json.result().get();
        } else {
            return null;
        }
    }

    /**
     * Deserialize an ItemStack from a JsonElement
     *
     * @param jsonElement the JsonElement to deserialize
     * @return the deserialized ItemStack
     */
    public static ItemStack deserializeItemStack(JsonElement jsonElement) {
        var jsonDecoded = JsonParser.parseString(jsonElement.toString());
        Optional<Pair<ItemStack, JsonElement>> optional = ItemStack.CODEC.decode(JsonOps.INSTANCE, jsonDecoded).result();
        if (optional.isEmpty()) {
            return ItemStack.EMPTY;
        }

        return optional.get().getFirst();
    }
}
