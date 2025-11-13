package de.kaiser_tec.uh_idk.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import org.lwjgl.glfw.GLFW;

public class UhIdkClient implements ClientModInitializer {




    private KeyManager keyManager;
    private BlockSlotRandomizer blockSlotRandomizer;
    private AutoClicker autoClicker;



    @Override
    public void onInitializeClient() {
        keyManager = new KeyManager();
        blockSlotRandomizer = new BlockSlotRandomizer();
        autoClicker = new AutoClicker();
        keyManager.init();
        blockSlotRandomizer.init(keyManager);
        autoClicker.init();
    }


}
