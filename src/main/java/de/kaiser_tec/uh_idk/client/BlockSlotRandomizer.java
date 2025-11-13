package de.kaiser_tec.uh_idk.client;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.lwjgl.glfw.GLFW;

import java.util.Random;

public class BlockSlotRandomizer {


    private int[] weights = new int[]{1,1,1,1,1,1,1,1,1};
    private boolean active = false;
    private Random random = new Random();


    public void init(KeyManager manager){
        manager.registerKeyMapping("key.uh_idk.block-slot-randomizer.toggle", GLFW.GLFW_KEY_KP_0,this::onKeyPressed);
        UseBlockCallback.EVENT.register(this::onUseBlock);
        ClientCommandRegistrationCallback.EVENT.register(this::registerCommand);
    }



    private void onKeyPressed(KeyMapping mapping, Minecraft minecraft){
        if(minecraft.player != null){
            active=!active;
            minecraft.player.displayClientMessage(active
                    ?Component.literal("Activated BlockSlotRandomizer")
                    :Component.literal("Deactivated BlockSlotRandomizer"),
                    false
            );
        }
    }


    private InteractionResult onUseBlock(Player player, Level level, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if(active && player instanceof LocalPlayer){
            Inventory inv = player.getInventory();
            if(weights[inv.getSelectedSlot()]>0 && inv.getSelectedItem().getItem() instanceof BlockItem){
                int rand = 0;
                int[] current = new int[9];
                int i;
                for(i = 0; i < 9; i++){
                    if(inv.getItem(i).getItem() instanceof BlockItem){
                        rand+=weights[i];
                        current[i]=weights[i];
                    }else{
                        current[i]=0;
                    }
                }
                rand = random.nextInt(rand);
                for(i = 0; i < 9 && rand >= 0; i++){
                    rand-=current[i];
                }
                inv.setSelectedSlot(--i);
            }
        }
        return InteractionResult.PASS;
    }


    private void registerCommand(CommandDispatcher<FabricClientCommandSource> commandDispatcher, CommandBuildContext commandBuildContext) {

        LiteralArgumentBuilder<FabricClientCommandSource> toggle = ClientCommandManager.literal("toggle").executes(this::toggleCommand);
        LiteralArgumentBuilder<FabricClientCommandSource> weights = ClientCommandManager.literal("weights")
                .then(
                        ClientCommandManager.literal("all") .then(
                                        ClientCommandManager.argument("weight", IntegerArgumentType.integer(0))
                                                .executes(this::allWeightsCommand)
                                )
                ).then(
                        ClientCommandManager.argument("slot", IntegerArgumentType.integer(1,9))
                                .then(
                                        ClientCommandManager.argument("weight", IntegerArgumentType.integer(0))
                                                .executes(this::weightsCommand)
                                )
                )
                .then(
                        ClientCommandManager.literal("selected-slot")
                                .then(
                                        ClientCommandManager.argument("weight", IntegerArgumentType.integer(0))
                                                .executes(this::weightsSelectedCommand)
                                )
                );


        commandDispatcher.register(
                ClientCommandManager.literal("bsr")
                        .then(toggle)
                        .then(weights)

        );
        commandDispatcher.register(
                ClientCommandManager.literal("block-slot-randomizer")
                        .then(toggle)
                        .then(weights)

        );
    }
    private int weightsCommand(CommandContext<FabricClientCommandSource> ctx) throws CommandSyntaxException{
        int slot = ctx.getArgument("slot",Integer.class);
        int weight = ctx.getArgument("weight", Integer.class);

        weights[slot-1]=weight;

        ctx.getSource().sendFeedback(Component.literal(String.format("Set weight of slot %s to %s",slot,weight)));

        return 1;
    }
    private int weightsSelectedCommand(CommandContext<FabricClientCommandSource> ctx) throws CommandSyntaxException{
        int slot = ctx.getSource().getPlayer().getInventory().getSelectedSlot();
        int weight = ctx.getArgument("weight", Integer.class);

        weights[slot]=weight;

        ctx.getSource().sendFeedback(Component.literal(String.format("Set weight of slot %s to %s",slot+1,weight)));

        return 1;
    }
    private int allWeightsCommand(CommandContext<FabricClientCommandSource> ctx) throws CommandSyntaxException{
        int weight = ctx.getArgument("weight", Integer.class);
        for(int i = 0; i < 9;i++){
            weights[i]=weight;
        }
        ctx.getSource().sendFeedback(Component.literal("Set weights to default"));
        return 1;
    }
    private int toggleCommand(CommandContext<FabricClientCommandSource> ctx) throws CommandSyntaxException{
        active=!active;
        ctx.getSource().sendFeedback(active
                        ?Component.literal("Activated BlockSlotRandomizer")
                        :Component.literal("Deactivated BlockSlotRandomizer")
        );
        return 1;
    }
}
