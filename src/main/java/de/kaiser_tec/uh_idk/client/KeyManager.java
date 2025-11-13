package de.kaiser_tec.uh_idk.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import java.util.HashMap;
import java.util.Map;

public class KeyManager {


    public static final String DEFAULT_KEY_CATEGORY = "key.category.uhidk";

    private HashMap<KeyMapping, KeyCallable> mappings = new HashMap<>();


    public void init(){
        ClientTickEvents.END_CLIENT_TICK.register(this::onEndTick);
    }

    public KeyMapping registerKeyMapping(String name, int key, KeyCallable callable){
        return registerKeyMapping(name, InputConstants.Type.KEYSYM, key, DEFAULT_KEY_CATEGORY,callable);
    }

    public KeyMapping registerKeyMapping(String name, InputConstants.Type type, int key, KeyCallable callable){
        return registerKeyMapping(name,type,key, DEFAULT_KEY_CATEGORY, callable);
    }

    public KeyMapping registerKeyMapping(String name, InputConstants.Type type, int key, String category, KeyCallable callable){
        KeyMapping mapping = KeyBindingHelper.registerKeyBinding(new KeyMapping(name,type,key,category));
        mappings.put(mapping,callable);
        return mapping;
    }


    private void onEndTick(Minecraft minecraft) {
        for(Map.Entry<KeyMapping, KeyCallable> e : mappings.entrySet()){
            KeyMapping mapping = e.getKey();
            KeyCallable callable = e.getValue();
            if(mapping.consumeClick()){
                callable.keyPressed(e.getKey(),minecraft);
            }
            if(mapping.isDown()){
                 callable.keyDownTick(e.getKey(),minecraft);
            }
        }
    }



    @FunctionalInterface
    public interface KeyCallable{
        void keyPressed(KeyMapping key, Minecraft minecraft);
        default void keyDownTick(KeyMapping key, Minecraft minecraft){
        }

    }

}
