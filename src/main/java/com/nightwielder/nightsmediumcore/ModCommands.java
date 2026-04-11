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
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("addheart")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(ctx -> addHeart(
                                                ctx.getSource(),
                                                EntityArgument.getPlayer(ctx, "player"),
                                                IntegerArgumentType.getInteger(ctx, "amount"))))))
                .then(Commands.literal("removeheart")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(ctx -> removeHeart(
                                                ctx.getSource(),
                                                EntityArgument.getPlayer(ctx, "player"),
                                                IntegerArgumentType.getInteger(ctx, "amount"))))))
                .then(Commands.literal("setheart")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("amount", IntegerArgumentType.integer(HeartLossHandler.MIN_HEARTS, HeartLossHandler.MAX_HEARTS))
                                        .executes(ctx -> setHeart(
                                                ctx.getSource(),
                                                EntityArgument.getPlayer(ctx, "player"),
                                                IntegerArgumentType.getInteger(ctx, "amount"))))))
                .then(Commands.literal("restoreheart")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> restoreHeart(
                                        ctx.getSource(),
                                        EntityArgument.getPlayer(ctx, "player"))))));
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

        source.sendSuccess(() -> Component.literal("Added " + actualRestore + " heart(s) to " +
                target.getName().getString() + ". They now have " + currentHearts + " max hearts.")
                .withStyle(ChatFormatting.GREEN), true);

        if (source.getEntity() != target)
        {
            target.sendSystemMessage(Component.literal("An admin restored " + actualRestore +
                    " heart(s)! You now have " + currentHearts + " max hearts.")
                    .withStyle(ChatFormatting.GREEN));
        }

        return 1;
    }

    private static int removeHeart(CommandSourceStack source, ServerPlayer target, int amount)
    {
        ServerLevel overworld = source.getServer().overworld();
        HeartLossData data = HeartLossData.get(overworld);

        int currentLost = data.getHeartsLost(target.getUUID());
        int maxLoss = HeartLossHandler.MAX_HEARTS - HeartLossHandler.MIN_HEARTS;
        int newLost = Math.min(currentLost + amount, maxLoss);
        int actualRemoved = newLost - currentLost;

        data.setHeartsLost(target.getUUID(), newLost);
        HeartLossHandler.applyModifier(target, newLost);

        int currentHearts = HeartLossHandler.MAX_HEARTS - newLost;

        source.sendSuccess(() -> Component.literal("Removed " + actualRemoved + " heart(s) from " +
                target.getName().getString() + ". They now have " + currentHearts + " max hearts.")
                .withStyle(ChatFormatting.RED), true);

        if (source.getEntity() != target)
        {
            target.sendSystemMessage(Component.literal("An admin removed " + actualRemoved +
                    " heart(s). You now have " + currentHearts + " max hearts.")
                    .withStyle(ChatFormatting.RED));
        }

        return 1;
    }

    private static int setHeart(CommandSourceStack source, ServerPlayer target, int hearts)
    {
        ServerLevel overworld = source.getServer().overworld();
        HeartLossData data = HeartLossData.get(overworld);

        int clamped = Math.max(HeartLossHandler.MIN_HEARTS, Math.min(hearts, HeartLossHandler.MAX_HEARTS));
        int newLost = HeartLossHandler.MAX_HEARTS - clamped;

        data.setHeartsLost(target.getUUID(), newLost);
        HeartLossHandler.applyModifier(target, newLost);

        source.sendSuccess(() -> Component.literal("Set " + target.getName().getString() +
                "'s max hearts to " + clamped + ".")
                .withStyle(ChatFormatting.GREEN), true);

        if (source.getEntity() != target)
        {
            target.sendSystemMessage(Component.literal("An admin set your max hearts to " + clamped + ".")
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

        source.sendSuccess(() -> Component.literal("Fully restored " + target.getName().getString() +
                "'s max hearts to " + HeartLossHandler.MAX_HEARTS + ".")
                .withStyle(ChatFormatting.GREEN), true);

        if (source.getEntity() != target)
        {
            target.sendSystemMessage(Component.literal("An admin fully restored your max hearts to " +
                    HeartLossHandler.MAX_HEARTS + "!")
                    .withStyle(ChatFormatting.GREEN));
        }

        return 1;
    }
}
