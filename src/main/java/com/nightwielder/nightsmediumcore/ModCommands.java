// Copyright 2026 Nightwielder23, licensed under CC BY-NC 4.0
package com.nightwielder.nightsmediumcore;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class ModCommands
{
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        dispatcher.register(Commands.literal("nightsmediumcore")
                .then(Commands.literal("hearts")
                        .executes(ctx -> showHearts(ctx.getSource()))
                        .then(Commands.literal("total")
                                .executes(ctx -> showTotalHearts(ctx.getSource()))))
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
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1, 20))
                                        .executes(ctx -> setHeart(
                                                ctx.getSource(),
                                                EntityArgument.getPlayer(ctx, "player"),
                                                IntegerArgumentType.getInteger(ctx, "amount"))))))
                .then(Commands.literal("restoreheart")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> restoreHeart(
                                        ctx.getSource(),
                                        EntityArgument.getPlayer(ctx, "player")))))
                .then(Commands.literal("recovery")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("mode", StringArgumentType.word())
                                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                                        new String[]{"crystal", "apple", "both"}, builder))
                                .executes(ctx -> setMode(
                                        ctx.getSource(),
                                        StringArgumentType.getString(ctx, "mode")))))
                .then(Commands.literal("mode")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("mediumcore")
                                .then(Commands.argument("state", StringArgumentType.word())
                                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                                                new String[]{"on", "off", "both"}, builder))
                                        .executes(ctx -> toggleBool(
                                                ctx.getSource(), "mediumcore",
                                                StringArgumentType.getString(ctx, "state")))))
                        .then(Commands.literal("lifesteal")
                                .then(Commands.argument("state", StringArgumentType.word())
                                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                                                new String[]{"on", "off", "both"}, builder))
                                        .executes(ctx -> toggleBool(
                                                ctx.getSource(), "lifesteal",
                                                StringArgumentType.getString(ctx, "state"))))))
                .then(Commands.literal("give")
                        .then(Commands.literal("hearts")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                                .executes(ctx -> giveHearts(
                                                        ctx.getSource(),
                                                        EntityArgument.getPlayer(ctx, "player"),
                                                        IntegerArgumentType.getInteger(ctx, "amount")))))))
                .then(Commands.literal("convert")
                        .then(Commands.literal("crystal")
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(ctx -> convertCrystal(
                                                ctx.getSource(),
                                                IntegerArgumentType.getInteger(ctx, "amount"))))))
                .then(Commands.literal("clearcooldown")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> clearCooldown(
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

        player.sendSystemMessage(Component.literal(currentHearts + "/" + HeartLossHandler.MAX_HEARTS)
                .withStyle(ChatFormatting.GREEN));

        return 1;
    }

    private static int showTotalHearts(CommandSourceStack source)
    {
        if (!(source.getEntity() instanceof ServerPlayer player))
        {
            source.sendFailure(Component.literal("This command can only be used by a player."));
            return 0;
        }

        int totalHearts = (int) (player.getMaxHealth() / 2);

        player.sendSystemMessage(Component.literal(String.valueOf(totalHearts))
                .withStyle(ChatFormatting.GREEN));

        return 1;
    }

    private static int addHeart(CommandSourceStack source, ServerPlayer target, int amount)
    {
        ServerLevel overworld = source.getServer().overworld();
        HeartLossData data = HeartLossData.get(overworld);

        int currentLost = data.getHeartsLost(target.getUUID());
        int minLost = HeartLossHandler.MAX_HEARTS - 20;
        int newLost = Math.max(currentLost - amount, minLost);
        int actualRestore = currentLost - newLost;

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

        int clamped = Math.max(HeartLossHandler.getMinHearts(), Math.min(hearts, 20));
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

    private static int setMode(CommandSourceStack source, String mode)
    {
        if (!mode.equals("crystal") && !mode.equals("apple") && !mode.equals("both"))
        {
            source.sendFailure(Component.literal("Invalid mode! Use: crystal, apple, or both."));
            return 0;
        }

        ModConfig.HEART_RECOVERY_MODE.set(mode);
        ModConfig.SPEC.save();

        source.sendSystemMessage(Component.literal("Heart recovery mode set to: " + mode)
                .withStyle(ChatFormatting.GREEN));

        return 1;
    }

    private static int toggleBool(CommandSourceStack source, String which, String state)
    {
        if (state.equalsIgnoreCase("both"))
        {
            ModConfig.MEDIUMCORE_ENABLED.set(true);
            ModConfig.LIFESTEAL_ENABLED.set(true);
            ModConfig.SPEC.save();
            source.sendSystemMessage(Component.literal(
                    "Mediumcore and Lifesteal both enabled (combined mode).")
                    .withStyle(ChatFormatting.GREEN));
            return 1;
        }

        boolean value;
        if (state.equalsIgnoreCase("on")) value = true;
        else if (state.equalsIgnoreCase("off")) value = false;
        else
        {
            source.sendFailure(Component.literal("Invalid state! Use: on, off, or both."));
            return 0;
        }

        String label;
        if (which.equals("mediumcore"))
        {
            ModConfig.MEDIUMCORE_ENABLED.set(value);
            label = "Mediumcore";
        }
        else
        {
            ModConfig.LIFESTEAL_ENABLED.set(value);
            label = "Lifesteal";
        }
        ModConfig.SPEC.save();

        source.sendSystemMessage(Component.literal(label + " " + (value ? "enabled" : "disabled") + ".")
                .withStyle(value ? ChatFormatting.GREEN : ChatFormatting.YELLOW));
        return 1;
    }

    private static int giveHearts(CommandSourceStack source, ServerPlayer target, int amount)
    {
        if (!(source.getEntity() instanceof ServerPlayer sender))
        {
            source.sendFailure(Component.literal("This command can only be used by a player."));
            return 0;
        }
        if (sender.getUUID().equals(target.getUUID()))
        {
            source.sendFailure(Component.literal("You cannot give hearts to yourself."));
            return 0;
        }

        ServerLevel overworld = source.getServer().overworld();
        HeartLossData data = HeartLossData.get(overworld);

        int senderLost = data.getHeartsLost(sender.getUUID());
        int minHearts = HeartLossHandler.getMinHearts();
        int senderAvailable = (HeartLossHandler.MAX_HEARTS - senderLost) - minHearts;
        if (senderAvailable <= 0)
        {
            source.sendFailure(Component.literal("You are already at the heart floor and cannot give hearts."));
            return 0;
        }

        int targetLost = data.getHeartsLost(target.getUUID());
        int targetMinLost = HeartLossHandler.MAX_HEARTS - 20;
        int targetCanReceive = targetLost - targetMinLost;
        int transferred = Math.min(Math.min(amount, senderAvailable), targetCanReceive);
        if (transferred <= 0)
        {
            source.sendFailure(Component.literal(
                    target.getName().getString() + " is already at the maximum of 20 base hearts."));
            return 0;
        }

        int newSenderLost = senderLost + transferred;
        data.setHeartsLost(sender.getUUID(), newSenderLost);
        HeartLossHandler.applyModifier(sender, newSenderLost);

        int newTargetLost = targetLost - transferred;
        data.setHeartsLost(target.getUUID(), newTargetLost);
        HeartLossHandler.applyModifier(target, newTargetLost);

        int senderHearts = HeartLossHandler.MAX_HEARTS - newSenderLost;
        sender.sendSystemMessage(Component.literal("Transferred " + transferred + " heart(s) to " +
                target.getName().getString() + ". You now have " + senderHearts + " base hearts.")
                .withStyle(ChatFormatting.GOLD));

        target.sendSystemMessage(Component.literal(sender.getName().getString() +
                " gave you " + transferred + " heart(s)!")
                .withStyle(ChatFormatting.GREEN));

        return 1;
    }

    private static int convertCrystal(CommandSourceStack source, int amount)
    {
        if (!(source.getEntity() instanceof ServerPlayer player))
        {
            source.sendFailure(Component.literal("This command can only be used by a player."));
            return 0;
        }

        ServerLevel overworld = source.getServer().overworld();
        HeartLossData data = HeartLossData.get(overworld);

        int currentLost = data.getHeartsLost(player.getUUID());
        int minHearts = HeartLossHandler.getMinHearts();
        int available = (HeartLossHandler.MAX_HEARTS - currentLost) - minHearts;
        if (available <= 0)
        {
            source.sendFailure(Component.literal("You are already at the heart floor and cannot convert hearts."));
            return 0;
        }

        int converted = Math.min(amount, available);
        int newLost = currentLost + converted;
        data.setHeartsLost(player.getUUID(), newLost);
        HeartLossHandler.applyModifier(player, newLost);

        ItemStack stack = new ItemStack(ModItems.CRYSTAL_HEART.get(), converted);
        if (!player.getInventory().add(stack))
        {
            player.drop(stack, false);
        }

        int hearts = HeartLossHandler.MAX_HEARTS - newLost;
        player.sendSystemMessage(Component.literal("Converted " + converted +
                " heart(s) into Crystal Heart item(s). You now have " + hearts + " base hearts.")
                .withStyle(ChatFormatting.LIGHT_PURPLE));

        return 1;
    }

    private static int clearCooldown(CommandSourceStack source, ServerPlayer target)
    {
        ServerLevel overworld = source.getServer().overworld();
        HeartLossData data = HeartLossData.get(overworld);

        data.setCrystalCooldown(target.getUUID(), 0L);
        data.setCombatCooldown(target.getUUID(), 0L);
        data.setAppleCooldown(target.getUUID(), 0L);
        data.setBedRegenCooldown(target.getUUID(), 0L);
        data.setDeathGraceExpiry(target.getUUID(), 0L);

        source.sendSystemMessage(Component.literal("Cleared all cooldowns for " +
                target.getName().getString() + ".")
                .withStyle(ChatFormatting.GREEN));

        if (source.getEntity() != target)
        {
            target.sendSystemMessage(Component.literal("An admin cleared all your cooldowns.")
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
