// Copyright 2026 Nightwielder23, licensed under CC BY-NC 4.0
package com.nightwielder.nightsmediumcore;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HeartLossData extends SavedData
{
    private static final String DATA_NAME = NightsMediumcore.MODID + "_heartloss";

    private final Map<UUID, Integer> heartsLost = new HashMap<>();
    private final Map<UUID, Long> crystalCooldown = new HashMap<>();
    private final Map<UUID, Long> deathGraceExpiry = new HashMap<>();
    private final Map<UUID, Long> combatCooldown = new HashMap<>();
    private final Map<UUID, Long> bedRegenCooldown = new HashMap<>();
    private final Map<UUID, Long> appleCooldown = new HashMap<>();
    private final Map<UUID, Integer> peakMaxHearts = new HashMap<>();
    private final Map<UUID, Long> respawnImmunityExpiry = new HashMap<>();

    public int getHeartsLost(UUID playerUUID)
    {
        return heartsLost.getOrDefault(playerUUID, 0);
    }

    public void setHeartsLost(UUID playerUUID, int amount)
    {
        heartsLost.put(playerUUID, amount);
        setDirty();
    }

    public long getCrystalCooldown(UUID playerUUID)
    {
        return crystalCooldown.getOrDefault(playerUUID, 0L);
    }

    public void setCrystalCooldown(UUID playerUUID, long gameTime)
    {
        crystalCooldown.put(playerUUID, gameTime);
        setDirty();
    }

    public long getDeathGraceExpiry(UUID playerUUID)
    {
        return deathGraceExpiry.getOrDefault(playerUUID, 0L);
    }

    public void setDeathGraceExpiry(UUID playerUUID, long gameTime)
    {
        deathGraceExpiry.put(playerUUID, gameTime);
        setDirty();
    }

    public long getCombatCooldown(UUID playerUUID)
    {
        return combatCooldown.getOrDefault(playerUUID, 0L);
    }

    public void setCombatCooldown(UUID playerUUID, long gameTime)
    {
        combatCooldown.put(playerUUID, gameTime);
        setDirty();
    }

    public long getBedRegenCooldown(UUID playerUUID)
    {
        return bedRegenCooldown.getOrDefault(playerUUID, 0L);
    }

    public void setBedRegenCooldown(UUID playerUUID, long gameTime)
    {
        bedRegenCooldown.put(playerUUID, gameTime);
        setDirty();
    }

    public long getAppleCooldown(UUID playerUUID)
    {
        return appleCooldown.getOrDefault(playerUUID, 0L);
    }

    public void setAppleCooldown(UUID playerUUID, long gameTime)
    {
        appleCooldown.put(playerUUID, gameTime);
        setDirty();
    }

    public int getPeakMaxHearts(UUID playerUUID)
    {
        return peakMaxHearts.getOrDefault(playerUUID, HeartLossHandler.MAX_HEARTS);
    }

    public void updatePeakMaxHearts(UUID playerUUID, int currentMaxHearts)
    {
        int existing = peakMaxHearts.getOrDefault(playerUUID, HeartLossHandler.MAX_HEARTS);
        if (currentMaxHearts > existing)
        {
            peakMaxHearts.put(playerUUID, currentMaxHearts);
            setDirty();
        }
    }

    public long getRespawnImmunityExpiry(UUID playerUUID)
    {
        return respawnImmunityExpiry.getOrDefault(playerUUID, 0L);
    }

    public void setRespawnImmunityExpiry(UUID playerUUID, long gameTime)
    {
        respawnImmunityExpiry.put(playerUUID, gameTime);
        setDirty();
    }

    public void clearRespawnImmunity(UUID playerUUID)
    {
        if (respawnImmunityExpiry.remove(playerUUID) != null)
        {
            setDirty();
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag)
    {
        CompoundTag heartsLostTag = new CompoundTag();
        for (Map.Entry<UUID, Integer> entry : heartsLost.entrySet())
        {
            heartsLostTag.putInt(entry.getKey().toString(), entry.getValue());
        }
        tag.put("heartsLost", heartsLostTag);

        CompoundTag crystalTag = new CompoundTag();
        for (Map.Entry<UUID, Long> entry : crystalCooldown.entrySet())
        {
            crystalTag.putLong(entry.getKey().toString(), entry.getValue());
        }
        tag.put("crystalCooldown", crystalTag);

        CompoundTag graceTag = new CompoundTag();
        for (Map.Entry<UUID, Long> entry : deathGraceExpiry.entrySet())
        {
            graceTag.putLong(entry.getKey().toString(), entry.getValue());
        }
        tag.put("deathGrace", graceTag);

        CompoundTag combatTag = new CompoundTag();
        for (Map.Entry<UUID, Long> entry : combatCooldown.entrySet())
        {
            combatTag.putLong(entry.getKey().toString(), entry.getValue());
        }
        tag.put("combatCooldown", combatTag);

        CompoundTag bedTag = new CompoundTag();
        for (Map.Entry<UUID, Long> entry : bedRegenCooldown.entrySet())
        {
            bedTag.putLong(entry.getKey().toString(), entry.getValue());
        }
        tag.put("bedRegenCooldown", bedTag);

        CompoundTag appleTag = new CompoundTag();
        for (Map.Entry<UUID, Long> entry : appleCooldown.entrySet())
        {
            appleTag.putLong(entry.getKey().toString(), entry.getValue());
        }
        tag.put("appleCooldown", appleTag);

        CompoundTag peakTag = new CompoundTag();
        for (Map.Entry<UUID, Integer> entry : peakMaxHearts.entrySet())
        {
            peakTag.putInt(entry.getKey().toString(), entry.getValue());
        }
        tag.put("peakMaxHearts", peakTag);

        CompoundTag immunityTag = new CompoundTag();
        for (Map.Entry<UUID, Long> entry : respawnImmunityExpiry.entrySet())
        {
            immunityTag.putLong(entry.getKey().toString(), entry.getValue());
        }
        tag.put("respawnImmunityExpiry", immunityTag);

        return tag;
    }

    private static HeartLossData load(CompoundTag tag)
    {
        HeartLossData data = new HeartLossData();

        // Load heartsLost (supports legacy key "players")
        CompoundTag heartsLostTag = tag.contains("heartsLost") ? tag.getCompound("heartsLost") : tag.getCompound("players");
        for (String key : heartsLostTag.getAllKeys())
        {
            data.heartsLost.put(UUID.fromString(key), heartsLostTag.getInt(key));
        }

        // Load crystalCooldown (supports legacy key "cooldowns")
        CompoundTag crystalTag = tag.contains("crystalCooldown") ? tag.getCompound("crystalCooldown") : tag.getCompound("cooldowns");
        for (String key : crystalTag.getAllKeys())
        {
            data.crystalCooldown.put(UUID.fromString(key), crystalTag.getLong(key));
        }

        CompoundTag graceTag = tag.getCompound("deathGrace");
        for (String key : graceTag.getAllKeys())
        {
            data.deathGraceExpiry.put(UUID.fromString(key), graceTag.getLong(key));
        }

        // Load combatCooldown (supports legacy key "combat")
        CompoundTag combatTag = tag.contains("combatCooldown") ? tag.getCompound("combatCooldown") : tag.getCompound("combat");
        for (String key : combatTag.getAllKeys())
        {
            data.combatCooldown.put(UUID.fromString(key), combatTag.getLong(key));
        }

        // Load bedRegenCooldown (supports legacy key "bedRegen")
        CompoundTag bedTag = tag.contains("bedRegenCooldown") ? tag.getCompound("bedRegenCooldown") : tag.getCompound("bedRegen");
        for (String key : bedTag.getAllKeys())
        {
            data.bedRegenCooldown.put(UUID.fromString(key), bedTag.getLong(key));
        }

        CompoundTag appleTag = tag.getCompound("appleCooldown");
        for (String key : appleTag.getAllKeys())
        {
            data.appleCooldown.put(UUID.fromString(key), appleTag.getLong(key));
        }

        CompoundTag peakTag = tag.getCompound("peakMaxHearts");
        for (String key : peakTag.getAllKeys())
        {
            data.peakMaxHearts.put(UUID.fromString(key), peakTag.getInt(key));
        }

        CompoundTag immunityTag = tag.getCompound("respawnImmunityExpiry");
        for (String key : immunityTag.getAllKeys())
        {
            data.respawnImmunityExpiry.put(UUID.fromString(key), immunityTag.getLong(key));
        }

        return data;
    }

    public static HeartLossData get(ServerLevel level)
    {
        return level.getDataStorage().computeIfAbsent(HeartLossData::load, HeartLossData::new, DATA_NAME);
    }
}
