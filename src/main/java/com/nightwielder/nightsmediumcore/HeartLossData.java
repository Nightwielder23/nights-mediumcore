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
    private final Map<UUID, Long> respawnImmunityExpiry = new HashMap<>();
    private final Map<UUID, Integer> lifeStealHearts = new HashMap<>();
    private final Map<UUID, Double> initialMaxHealth = new HashMap<>();

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

    public long getRespawnImmunityExpiry(UUID playerUUID)
    {
        return respawnImmunityExpiry.getOrDefault(playerUUID, 0L);
    }

    public void setRespawnImmunityExpiry(UUID playerUUID, long gameTime)
    {
        respawnImmunityExpiry.put(playerUUID, gameTime);
        setDirty();
    }

    public int getLifeStealHearts(UUID playerUUID)
    {
        return lifeStealHearts.getOrDefault(playerUUID, 0);
    }

    public void setLifeStealHearts(UUID playerUUID, int amount)
    {
        lifeStealHearts.put(playerUUID, Math.max(0, amount));
        setDirty();
    }

    public boolean hasInitialMaxHealth(UUID playerUUID)
    {
        return initialMaxHealth.containsKey(playerUUID);
    }

    public double getInitialMaxHealth(UUID playerUUID)
    {
        return initialMaxHealth.getOrDefault(playerUUID, 0.0);
    }

    public void setInitialMaxHealth(UUID playerUUID, double maxHealth)
    {
        initialMaxHealth.put(playerUUID, maxHealth);
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

        CompoundTag immunityTag = new CompoundTag();
        for (Map.Entry<UUID, Long> entry : respawnImmunityExpiry.entrySet())
        {
            immunityTag.putLong(entry.getKey().toString(), entry.getValue());
        }
        tag.put("respawnImmunityExpiry", immunityTag);

        CompoundTag lsHeartsTag = new CompoundTag();
        for (Map.Entry<UUID, Integer> entry : lifeStealHearts.entrySet())
        {
            lsHeartsTag.putInt(entry.getKey().toString(), entry.getValue());
        }
        tag.put("lifeStealHearts", lsHeartsTag);

        CompoundTag initialMaxTag = new CompoundTag();
        for (Map.Entry<UUID, Double> entry : initialMaxHealth.entrySet())
        {
            initialMaxTag.putDouble(entry.getKey().toString(), entry.getValue());
        }
        tag.put("initialMaxHealth", initialMaxTag);

        return tag;
    }

    private static HeartLossData load(CompoundTag tag)
    {
        HeartLossData data = new HeartLossData();

        // Load heartsLost (supports legacy key "players")
        CompoundTag heartsLostTag = tag.contains("heartsLost") ? tag.getCompound("heartsLost") : tag.getCompound("players");
        for (String key : heartsLostTag.getAllKeys())
        {
            try { data.heartsLost.put(UUID.fromString(key), heartsLostTag.getInt(key)); }
            catch (IllegalArgumentException ignored) {}
        }

        // Load crystalCooldown (supports legacy key "cooldowns")
        CompoundTag crystalTag = tag.contains("crystalCooldown") ? tag.getCompound("crystalCooldown") : tag.getCompound("cooldowns");
        for (String key : crystalTag.getAllKeys())
        {
            try { data.crystalCooldown.put(UUID.fromString(key), crystalTag.getLong(key)); }
            catch (IllegalArgumentException ignored) {}
        }

        CompoundTag graceTag = tag.getCompound("deathGrace");
        for (String key : graceTag.getAllKeys())
        {
            try { data.deathGraceExpiry.put(UUID.fromString(key), graceTag.getLong(key)); }
            catch (IllegalArgumentException ignored) {}
        }

        // Load combatCooldown (supports legacy key "combat")
        CompoundTag combatTag = tag.contains("combatCooldown") ? tag.getCompound("combatCooldown") : tag.getCompound("combat");
        for (String key : combatTag.getAllKeys())
        {
            try { data.combatCooldown.put(UUID.fromString(key), combatTag.getLong(key)); }
            catch (IllegalArgumentException ignored) {}
        }

        // Load bedRegenCooldown (supports legacy key "bedRegen")
        CompoundTag bedTag = tag.contains("bedRegenCooldown") ? tag.getCompound("bedRegenCooldown") : tag.getCompound("bedRegen");
        for (String key : bedTag.getAllKeys())
        {
            try { data.bedRegenCooldown.put(UUID.fromString(key), bedTag.getLong(key)); }
            catch (IllegalArgumentException ignored) {}
        }

        CompoundTag appleTag = tag.getCompound("appleCooldown");
        for (String key : appleTag.getAllKeys())
        {
            try { data.appleCooldown.put(UUID.fromString(key), appleTag.getLong(key)); }
            catch (IllegalArgumentException ignored) {}
        }

        CompoundTag immunityTag = tag.getCompound("respawnImmunityExpiry");
        for (String key : immunityTag.getAllKeys())
        {
            try { data.respawnImmunityExpiry.put(UUID.fromString(key), immunityTag.getLong(key)); }
            catch (IllegalArgumentException ignored) {}
        }

        CompoundTag lsHeartsTag = tag.getCompound("lifeStealHearts");
        for (String key : lsHeartsTag.getAllKeys())
        {
            try { data.lifeStealHearts.put(UUID.fromString(key), lsHeartsTag.getInt(key)); }
            catch (IllegalArgumentException ignored) {}
        }

        CompoundTag initialMaxTag = tag.getCompound("initialMaxHealth");
        for (String key : initialMaxTag.getAllKeys())
        {
            try { data.initialMaxHealth.put(UUID.fromString(key), initialMaxTag.getDouble(key)); }
            catch (IllegalArgumentException ignored) {}
        }

        return data;
    }

    public static HeartLossData get(ServerLevel level)
    {
        return level.getDataStorage().computeIfAbsent(HeartLossData::load, HeartLossData::new, DATA_NAME);
    }
}
