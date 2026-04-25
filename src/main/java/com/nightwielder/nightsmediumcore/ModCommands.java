// Copyright 2026 Nightwielder23, licensed under CC BY-NC 4.0
package com.nightwielder.nightsmediumcore;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
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
        LiteralCommandNode<CommandSourceStack> node = dispatcher.register(Commands.literal("nights_mediumcore")
                .then(Commands.literal("hearts")
                        .executes(ctx -> showHearts(ctx.getSource()))
                        .then(Commands.literal("total")
                                .executes(ctx -> showTotalHearts(ctx.getSource())))
                        .then(Commands.literal("living")
                                .executes(ctx -> showLivingHearts(ctx.getSource()))))
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
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1, 60))
                                        .executes(ctx -> setHeart(
                                                ctx.getSource(),
                                                EntityArgument.getPlayer(ctx, "player"),
                                                IntegerArgumentType.getInteger(ctx, "amount"))))))
                .then(Commands.literal("basehearts")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("value", IntegerArgumentType.integer(1, 30))
                                .executes(ctx -> setBaseHearts(
                                        ctx.getSource(),
                                        IntegerArgumentType.getInteger(ctx, "value")))))
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
                                        new String[]{"crystal", "apple", "both", "none"}, builder))
                                .executes(ctx -> setMode(
                                        ctx.getSource(),
                                        StringArgumentType.getString(ctx, "mode")))))
                .then(Commands.literal("mode")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("mediumcore")
                                .executes(ctx -> setHeartMode(ctx.getSource(), "mediumcore")))
                        .then(Commands.literal("lifesteal")
                                .executes(ctx -> setHeartMode(ctx.getSource(), "lifesteal")))
                        .then(Commands.literal("both")
                                .executes(ctx -> setHeartMode(ctx.getSource(), "both"))))
                .then(Commands.literal("convert")
                        .then(Commands.literal("crystal")
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(ctx -> convertCrystal(
                                                ctx.getSource(),
                                                IntegerArgumentType.getInteger(ctx, "amount")))))
                        .then(Commands.literal("living")
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(ctx -> convertLiving(
                                                ctx.getSource(),
                                                IntegerArgumentType.getInteger(ctx, "amount"))))))
                .then(Commands.literal("clearcooldown")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> clearCooldown(
                                        ctx.getSource(),
                                        EntityArgument.getPlayer(ctx, "player"))))));
        dispatcher.register(Commands.literal("nm").redirect(node));
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
        int initial = HeartLossHandler.getInitialHearts(player);
        int currentHearts = initial - heartsLost;

        player.sendSystemMessage(Component.literal(currentHearts + "/" + initial)
                .withStyle(ChatFormatting.GREEN));

        return 1;
    }

    private static int showLivingHearts(CommandSourceStack source)
    {
        if (!(source.getEntity() instanceof ServerPlayer player))
        {
            source.sendFailure(Component.literal("This command can only be used by a player."));
            return 0;
        }

        ServerLevel overworld = source.getServer().overworld();
        HeartLossData data = HeartLossData.get(overworld);
        int ls = data.getLifeStealHearts(player.getUUID());
        int cap = HeartLossHandler.MAX_HEARTS;

        player.sendSystemMessage(Component.literal(ls + "/" + cap)
                .withStyle(ChatFormatting.LIGHT_PURPLE));

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
        int actualRestore = Math.min(amount, currentLost);
        int newLost = currentLost - actualRestore;

        data.setHeartsLost(target.getUUID(), newLost);
        HeartLossHandler.applyModifier(target, newLost);

        int currentHearts = HeartLossHandler.getInitialHearts(target) - newLost;

        source.sendSystemMessage(Component.literal("Added " + actualRestore + " heart(s) to " +
                target.getName().getString() + ". They now have " + currentHearts + " hearts.")
                .withStyle(ChatFormatting.GREEN));

        if (source.getEntity() != target)
        {
            target.sendSystemMessage(Component.literal("An admin restored " + actualRestore +
                    " heart(s)! You now have " + currentHearts + " hearts.")
                    .withStyle(ChatFormatting.GREEN));
        }

        return 1;
    }

    private static int removeHeart(CommandSourceStack source, ServerPlayer target, int amount)
    {
        ServerLevel overworld = source.getServer().overworld();
        HeartLossData data = HeartLossData.get(overworld);

        int currentLost = data.getHeartsLost(target.getUUID());
        int initial = HeartLossHandler.getInitialHearts(target);
        int maxLoss = Math.max(0, initial - HeartLossHandler.getMinHearts());
        int newLost = Math.min(currentLost + amount, maxLoss);
        int actualRemoved = newLost - currentLost;

        data.setHeartsLost(target.getUUID(), newLost);
        HeartLossHandler.applyModifier(target, newLost);

        int currentHearts = initial - newLost;

        source.sendSystemMessage(Component.literal("Removed " + actualRemoved + " heart(s) from " +
                target.getName().getString() + ". They now have " + currentHearts + " hearts.")
                .withStyle(ChatFormatting.RED));

        if (source.getEntity() != target)
        {
            target.sendSystemMessage(Component.literal("An admin removed " + actualRemoved +
                    " heart(s). You now have " + currentHearts + " hearts.")
                    .withStyle(ChatFormatting.RED));
        }

        return 1;
    }

    private static int setHeart(CommandSourceStack source, ServerPlayer target, int hearts)
    {
        ServerLevel overworld = source.getServer().overworld();
        HeartLossData data = HeartLossData.get(overworld);

        int mcMax = HeartLossHandler.getInitialHearts(target);
        int lsCap = LifeStealHandler.resolvedHeartCap();
        int min = HeartLossHandler.getMinHearts();
        int hardCap = lsCap == Integer.MAX_VALUE ? 60 : mcMax + Math.max(0, lsCap);
        int clamped = Math.max(min, Math.min(hearts, hardCap));

        int newMc = Math.min(clamped, mcMax);
        int newLs = clamped - newMc;
        int newLost = mcMax - newMc;

        data.setHeartsLost(target.getUUID(), newLost);
        HeartLossHandler.applyModifier(target, newLost);
        data.setLifeStealHearts(target.getUUID(), newLs);
        LifeStealHandler.applyBonusModifier(target, newLs);

        source.sendSystemMessage(Component.literal("Set " + target.getName().getString() +
                "'s hearts to " + clamped + ".")
                .withStyle(ChatFormatting.GREEN));

        if (source.getEntity() != target)
        {
            target.sendSystemMessage(Component.literal("An admin set your hearts to " + clamped + ".")
                    .withStyle(ChatFormatting.GREEN));
        }

        return 1;
    }

    private static int setMode(CommandSourceStack source, String mode)
    {
        if (!mode.equals("crystal") && !mode.equals("apple") && !mode.equals("both") && !mode.equals("none"))
        {
            source.sendFailure(Component.literal("Invalid mode! Use: crystal, apple, both, or none."));
            return 0;
        }

        ModConfig.HEART_RECOVERY_MODE.set(mode);
        ModConfig.SPEC.save();

        source.sendSystemMessage(Component.literal("Heart recovery mode set to: " + mode)
                .withStyle(ChatFormatting.GREEN));

        return 1;
    }

    private static int setHeartMode(CommandSourceStack source, String mode)
    {
        boolean mc;
        boolean ls;
        String label;
        switch (mode)
        {
            case "mediumcore": mc = true; ls = false; label = "Mediumcore only"; break;
            case "lifesteal":  mc = false; ls = true;  label = "Lifesteal only"; break;
            case "both":       mc = true;  ls = true;  label = "Mediumcore and Lifesteal (combined)"; break;
            default:
                source.sendFailure(Component.literal("Invalid mode! Use: mediumcore, lifesteal, or both."));
                return 0;
        }

        ModConfig.MEDIUMCORE_ENABLED.set(mc);
        ModConfig.LIFESTEAL_ENABLED.set(ls);
        ModConfig.SPEC.save();

        source.sendSystemMessage(Component.literal("Heart mode: " + label + ".")
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
        int currentLs = data.getLifeStealHearts(player.getUUID());
        int minHearts = HeartLossHandler.getMinHearts();
        int initial = HeartLossHandler.getInitialHearts(player);
        int mcAvailable = (initial - currentLost) - minHearts;
        int totalAvailable = currentLs + mcAvailable;
        if (totalAvailable <= 0)
        {
            source.sendFailure(Component.literal("You have no hearts to convert."));
            return 0;
        }

        int converted = Math.min(amount, totalAvailable);
        int fromLs = Math.min(converted, currentLs);
        int fromMc = converted - fromLs;

        data.setLifeStealHearts(player.getUUID(), currentLs - fromLs);
        LifeStealHandler.applyBonusModifier(player, currentLs - fromLs);

        int newLost = currentLost + fromMc;
        data.setHeartsLost(player.getUUID(), newLost);
        HeartLossHandler.applyModifier(player, newLost);

        ItemStack stack = new ItemStack(ModItems.CRYSTAL_HEART.get(), converted);
        if (!player.getInventory().add(stack))
        {
            player.drop(stack, false);
        }

        player.sendSystemMessage(Component.literal("Converted " + converted +
                (converted == 1 ? " heart into Crystal Heart item." : " hearts into Crystal Heart items."))
                .withStyle(ChatFormatting.LIGHT_PURPLE));

        return 1;
    }

    private static int convertLiving(CommandSourceStack source, int amount)
    {
        if (!(source.getEntity() instanceof ServerPlayer player))
        {
            source.sendFailure(Component.literal("This command can only be used by a player."));
            return 0;
        }

        ServerLevel overworld = source.getServer().overworld();
        HeartLossData data = HeartLossData.get(overworld);

        int currentLost = data.getHeartsLost(player.getUUID());
        int currentLs = data.getLifeStealHearts(player.getUUID());
        int minHearts = HeartLossHandler.getMinHearts();
        int initial = HeartLossHandler.getInitialHearts(player);
        int mcAvailable = (initial - currentLost) - minHearts;
        int totalAvailable = currentLs + mcAvailable;
        if (totalAvailable <= 0)
        {
            source.sendFailure(Component.literal("You have no hearts to convert."));
            return 0;
        }

        int converted = Math.min(amount, totalAvailable);
        int fromLs = Math.min(converted, currentLs);
        int fromMc = converted - fromLs;

        data.setLifeStealHearts(player.getUUID(), currentLs - fromLs);
        LifeStealHandler.applyBonusModifier(player, currentLs - fromLs);

        int newLost = currentLost + fromMc;
        data.setHeartsLost(player.getUUID(), newLost);
        HeartLossHandler.applyModifier(player, newLost);

        ItemStack stack = new ItemStack(ModItems.LIVING_HEART.get(), converted);
        if (!player.getInventory().add(stack))
            player.drop(stack, false);

        player.sendSystemMessage(Component.literal("Converted " + converted +
                (converted == 1 ? " heart into Living Heart item." : " hearts into Living Heart items."))
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

        int initial = HeartLossHandler.getInitialHearts(target);

        source.sendSystemMessage(Component.literal("Fully restored " + target.getName().getString() +
                "'s hearts to " + initial + ".")
                .withStyle(ChatFormatting.GREEN));

        if (source.getEntity() != target)
        {
            target.sendSystemMessage(Component.literal("An admin fully restored your hearts to " +
                    initial + "!")
                    .withStyle(ChatFormatting.GREEN));
        }

        return 1;
    }

    private static int setBaseHearts(CommandSourceStack source, int value)
    {
        ModConfig.BASE_HEARTS.set(value);
        ModConfig.SPEC.save();

        int floor = HeartLossHandler.getMinHearts();
        ServerLevel overworld = source.getServer().overworld();
        HeartLossData data = HeartLossData.get(overworld);

        // Re-clamp existing heart loss for every online player against the new floor
        for (ServerPlayer player : source.getServer().getPlayerList().getPlayers())
        {
            int initial = HeartLossHandler.getInitialHearts(player);
            int maxLoss = Math.max(0, initial - floor);
            int currentLost = data.getHeartsLost(player.getUUID());
            if (currentLost > maxLoss)
            {
                data.setHeartsLost(player.getUUID(), maxLoss);
                HeartLossHandler.applyModifier(player, maxLoss);
            }
        }

        source.sendSystemMessage(Component.literal(
                "Base hearts set to " + value + ". Effective floor is now " + floor + " hearts.")
                .withStyle(ChatFormatting.GREEN));
        return 1;
    }
}
