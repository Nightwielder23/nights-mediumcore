package com.nightwielder.nightsmediumcore;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class ModCommands
{
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        dispatcher.register(Commands.literal("nightsmediumcore")
                .then(Commands.literal("hearts")
                        .executes(ctx -> showHearts(ctx.getSource())))
                .then(Commands.literal("addheart")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(ctx -> addHeart(
                                                ctx.getSource(),
                                                EntityArgument.getPlayer(ctx, "player"),
                                                IntegerArgumentType.getInteger(ctx, "amount"))))))
                .then(Commands.literal("removeheart")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(ctx -> removeHeart(
                                                ctx.getSource(),
                                                EntityArgument.getPlayer(ctx, "player"),
                                                IntegerArgumentType.getInteger(ctx, "amount"))))))
                .then(Commands.literal("setheart")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1, HeartLossHandler.MAX_HEARTS))
                                        .executes(ctx -> setHeart(
                                                ctx.getSource(),
                                                EntityArgument.getPlayer(ctx, "player"),
                                                IntegerArgumentType.getInteger(ctx, "amount"))))))
                .then(Commands.literal("restoreheart")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> restoreHeart(
                                        ctx.getSource(),
                                        EntityArgument.getPlayer(ctx, "player"))))));
    }

    private static int showHearts(CommandSourceStack source)
    {
        if (!(source.getEntity() instanceof ServerPlayer player))
        {
            source.sendFailure(Component.literal("This command can only be used by a player."));
            return 0;
        }

        ServerLevel overworld = source.getServer().overworld();
        HeartLossData data = HeartLossData.get(overworld);
        int heartsLost = data.getHeartsLost(player.getUUID());
        int currentHearts = HeartLossHandler.MAX_HEARTS - heartsLost;

        player.sendSystemMessage(Component.literal("Mediumcore hearts: " + currentHearts + "/" + HeartLossHandler.MAX_HEARTS + " base hearts")
                .withStyle(ChatFormatting.GREEN));
        player.sendSystemMessage(Component.literal("(Additional hearts from other mods are not tracked here.)")
                .withStyle(ChatFormatting.GRAY));

        return 1;
    }

    private static int addHeart(CommandSourceStack source, ServerPlayer target, int amount)
    {
        ServerLevel overworld = source.getServer().overworld();
        HeartLossData data = HeartLossData.get(overworld);

        int currentLost = data.getHeartsLost(target.getUUID());
        int actualRestore = Math.min(amount, currentLost);
        int newLost = currentLost - actualRestore;

        data.setHeartsLost(target.getUUID(), newLost);
        HeartLossHandler.applyModifier(target, newLost);

        int currentHearts = HeartLossHandler.MAX_HEARTS - newLost;

        source.sendSystemMessage(Component.literal("Added " + actualRestore + " base heart(s) to " +
                target.getName().getString() + ". They now have " + currentHearts + " base hearts.")
                .withStyle(ChatFormatting.GREEN));

        if (source.getEntity() != target)
        {
            target.sendSystemMessage(Component.literal("An admin restored " + actualRestore +
                    " base heart(s)! You now have " + currentHearts + " base hearts.")
                    .withStyle(ChatFormatting.GREEN));
        }

        return 1;
    }

    private static int removeHeart(CommandSourceStack source, ServerPlayer target, int amount)
    {
        ServerLevel overworld = source.getServer().overworld();
        HeartLossData data = HeartLossData.get(overworld);

        int currentLost = data.getHeartsLost(target.getUUID());
        int maxLoss = HeartLossHandler.MAX_HEARTS - HeartLossHandler.getMinHearts();
        int newLost = Math.min(currentLost + amount, maxLoss);
        int actualRemoved = newLost - currentLost;

        data.setHeartsLost(target.getUUID(), newLost);
        HeartLossHandler.applyModifier(target, newLost);

        int currentHearts = HeartLossHandler.MAX_HEARTS - newLost;

        source.sendSystemMessage(Component.literal("Removed " + actualRemoved + " base heart(s) from " +
                target.getName().getString() + ". They now have " + currentHearts + " base hearts.")
                .withStyle(ChatFormatting.RED));

        if (source.getEntity() != target)
        {
            target.sendSystemMessage(Component.literal("An admin removed " + actualRemoved +
                    " base heart(s). You now have " + currentHearts + " base hearts.")
                    .withStyle(ChatFormatting.RED));
        }

        return 1;
    }

    private static int setHeart(CommandSourceStack source, ServerPlayer target, int hearts)
    {
        ServerLevel overworld = source.getServer().overworld();
        HeartLossData data = HeartLossData.get(overworld);

        int clamped = Math.max(HeartLossHandler.getMinHearts(), Math.min(hearts, HeartLossHandler.MAX_HEARTS));
        int newLost = HeartLossHandler.MAX_HEARTS - clamped;

        data.setHeartsLost(target.getUUID(), newLost);
        HeartLossHandler.applyModifier(target, newLost);

        source.sendSystemMessage(Component.literal("Set " + target.getName().getString() +
                "'s base hearts to " + clamped + ".")
                .withStyle(ChatFormatting.GREEN));

        if (source.getEntity() != target)
        {
            target.sendSystemMessage(Component.literal("An admin set your base hearts to " + clamped + ".")
                    .withStyle(ChatFormatting.GREEN));
        }

        return 1;
    }

    private static int restoreHeart(CommandSourceStack source, ServerPlayer target)
    {
        ServerLevel overworld = source.getServer().overworld();
        HeartLossData data = HeartLossData.get(overworld);

        data.setHeartsLost(target.getUUID(), 0);
        HeartLossHandler.applyModifier(target, 0);

        source.sendSystemMessage(Component.literal("Fully restored " + target.getName().getString() +
                "'s base hearts to " + HeartLossHandler.MAX_HEARTS + ".")
                .withStyle(ChatFormatting.GREEN));

        if (source.getEntity() != target)
        {
            target.sendSystemMessage(Component.literal("An admin fully restored your base hearts to " +
                    HeartLossHandler.MAX_HEARTS + "!")
                    .withStyle(ChatFormatting.GREEN));
        }

        return 1;
    }
}
