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
        int cap = LifeStealHandler.resolvedHeartCap();

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

        int minHearts = HeartLossHandler.getMinHearts();
        int cap = LifeStealHandler.resolvedHeartCap();

        int senderLs = data.getLifeStealHearts(sender.getUUID());
        int senderLost = data.getHeartsLost(sender.getUUID());
        int senderMcAvailable = (HeartLossHandler.MAX_HEARTS - senderLost) - minHearts;
        int senderTotalAvailable = senderLs + senderMcAvailable;
        if (senderTotalAvailable <= 0)
        {
            source.sendFailure(Component.literal("You have no hearts to give."));
            return 0;
        }

        int targetLs = data.getLifeStealHearts(target.getUUID());
        int targetLost = data.getHeartsLost(target.getUUID());
        int targetLsCapacity = Math.max(0, cap - targetLs);
        int targetMcCapacity = targetLost;
        int targetCapacity = targetLsCapacity + targetMcCapacity;

        int transferred = Math.min(Math.min(amount, senderTotalAvailable), targetCapacity);
        if (transferred <= 0)
        {
            source.sendFailure(Component.literal(
                    target.getName().getString() + " cannot receive any more hearts."));
            return 0;
        }

        // Drain sender: lifesteal first, then mediumcore
        int fromSenderLs = Math.min(transferred, senderLs);
        int fromSenderMc = transferred - fromSenderLs;
        data.setLifeStealHearts(sender.getUUID(), senderLs - fromSenderLs);
        int newSenderLost = senderLost + fromSenderMc;
        data.setHeartsLost(sender.getUUID(), newSenderLost);
        HeartLossHandler.applyModifier(sender, newSenderLost);
        LifeStealHandler.applyBonusModifier(sender, senderLs - fromSenderLs);

        // Fill target: lifesteal first, then mediumcore
        int toTargetLs = Math.min(transferred, targetLsCapacity);
        int toTargetMc = transferred - toTargetLs;
        data.setLifeStealHearts(target.getUUID(), targetLs + toTargetLs);
        int newTargetLost = targetLost - toTargetMc;
        data.setHeartsLost(target.getUUID(), newTargetLost);
        HeartLossHandler.applyModifier(target, newTargetLost);
        LifeStealHandler.applyBonusModifier(target, targetLs + toTargetLs);

        sender.sendSystemMessage(Component.literal("Transferred " + transferred + " heart(s) to " +
                target.getName().getString() + ".")
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
        int currentLs = data.getLifeStealHearts(player.getUUID());
        int minHearts = HeartLossHandler.getMinHearts();
        int mcAvailable = (HeartLossHandler.MAX_HEARTS - currentLost) - minHearts;
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
                " heart(s) into Crystal Heart item(s).")
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

        int available = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++)
        {
            ItemStack s = player.getInventory().getItem(i);
            if (s.is(ModItems.CRYSTAL_HEART.get()))
                available += s.getCount();
        }
        if (available <= 0)
        {
            source.sendFailure(Component.literal("You have no Crystal Hearts to convert."));
            return 0;
        }

        int converted = Math.min(amount, available);
        int toRemove = converted;
        for (int i = 0; i < player.getInventory().getContainerSize() && toRemove > 0; i++)
        {
            ItemStack s = player.getInventory().getItem(i);
            if (s.is(ModItems.CRYSTAL_HEART.get()))
            {
                int take = Math.min(s.getCount(), toRemove);
                s.shrink(take);
                toRemove -= take;
            }
        }

        ItemStack result = new ItemStack(ModItems.LIVING_HEART.get(), converted);
        if (!player.getInventory().add(result))
            player.drop(result, false);

        player.sendSystemMessage(Component.literal("Converted " + converted +
                " Crystal Heart(s) into Living Heart(s).")
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
