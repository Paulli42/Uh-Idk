package de.kaiser_tec.uh_idk.client;

import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import net.minecraft.client.Minecraft;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class AutoClicker {
    Minecraft minecraft;
    long nextClick = Long.MAX_VALUE;
    long clickInterval;
    Method startAttack;

    public void init() {
        minecraft = Minecraft.getInstance();
        try {
            MappingResolver resolver = FabricLoader.getInstance().getMappingResolver();
            startAttack = Minecraft.class.getDeclaredMethod(resolver.mapMethodName("intermediary","net.minecraft.class_310","method_1536","()Z"));
            startAttack.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        ClientCommandRegistrationCallback.EVENT.register((d, c)->
                d.register(ClientCommandManager.literal("ac").
                        then(ClientCommandManager.argument("interval", LongArgumentType.longArg(-1))
                                .executes(this::onAcClickCommand))));
        ClientTickEvents.START_CLIENT_TICK.register(this::tick);

    }

    private void tick(Minecraft m){
        startAttack.setAccessible(true);
        if(System.currentTimeMillis()>=nextClick){
            nextClick+=clickInterval;
            try {
                if(minecraft.player!=null){
                    int miss = minecraft.missTime;
                    minecraft.missTime = 0;
                    startAttack.invoke(m);
                    minecraft.missTime = miss;
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private int onAcClickCommand(CommandContext<FabricClientCommandSource> context){
        clickInterval = context.getArgument("interval",Long.class);
        if(clickInterval == -1){
            nextClick = Long.MAX_VALUE;
        }else{
            nextClick = System.currentTimeMillis()+clickInterval;
        }
        return 0;
    }

}
