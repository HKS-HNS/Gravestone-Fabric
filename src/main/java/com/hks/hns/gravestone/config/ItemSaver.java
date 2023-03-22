package com.hks.hns.gravestone.config;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import net.minecraft.item.ItemStack;
import java.io.*;
import java.util.Optional;

public class ItemSaver {

    // Save an ItemStack to a file
    public static void saveItem(ItemStack stack, String filename) throws IOException {
        Gson gson = new Gson();
        String json = gson.toJson(serializeItemStack(stack));
        FileWriter writer = new FileWriter(filename);
        writer.write(json);
        writer.close();
    }

    // Load an ItemStack from a file
    public static ItemStack loadItem(String filename) throws IOException {
        Gson gson = new Gson();
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String json = reader.readLine();
        reader.close();
        return deserializeItemStack(gson.fromJson(json, JsonElement.class));
    }

    // Serialize an ItemStack to a JsonElement
    public static JsonElement serializeItemStack(ItemStack stack) {
        var json = ItemStack.CODEC.encode(stack, JsonOps.INSTANCE, JsonOps.INSTANCE.empty());
        if (json.result().isPresent()) {
            return json.result().get();
        } else {
            return null;
        }
    }

    // Deserialize an ItemStack from a JsonElement
    public static ItemStack deserializeItemStack(JsonElement jsonElement) {
        var jsonDecoded = JsonParser.parseString(jsonElement.toString());
        Optional<Pair<ItemStack, JsonElement>> optional = ItemStack.CODEC.decode(JsonOps.INSTANCE, jsonDecoded).result();
        if(optional.isEmpty()){
            return ItemStack.EMPTY;
        }

        return optional.get().getFirst();
    }
}
