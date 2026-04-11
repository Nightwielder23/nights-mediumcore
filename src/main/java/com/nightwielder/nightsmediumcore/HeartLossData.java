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
    private final Map<UUID, Long> cooldownExpiry = new HashMap<>();
    private final Map<UUID, Long> deathGraceExpiry = new HashMap<>();
    private final Map<UUID, Long> combatExpiry = new HashMap<>();
    private final Map<UUID, Long> bedRegenExpiry = new HashMap<>();

    public int getHeartsLost(UUID playerUUID)
    {
        return heartsLost.getOrDefault(playerUUID, 0);
    }

    public void setHeartsLost(UUID playerUUID, int amount)
    {
        heartsLost.put(playerUUID, amount);
        setDirty();
    }

    public long getCooldownExpiry(UUID playerUUID)
    {
        return cooldownExpiry.getOrDefault(playerUUID, 0L);
    }

    public void setCooldownExpiry(UUID playerUUID, long gameTime)
    {
        cooldownExpiry.put(playerUUID, gameTime);
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

    public long getCombatExpiry(UUID playerUUID)
    {
        return combatExpiry.getOrDefault(playerUUID, 0L);
    }

    public void setCombatExpiry(UUID playerUUID, long gameTime)
    {
        combatExpiry.put(playerUUID, gameTime);
        setDirty();
    }

    public long getBedRegenExpiry(UUID playerUUID)
    {
        return bedRegenExpiry.getOrDefault(playerUUID, 0L);
    }

    public void setBedRegenExpiry(UUID playerUUID, long gameTime)
    {
        bedRegenExpiry.put(playerUUID, gameTime);
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag)
    {
        CompoundTag playersTag = new CompoundTag();
        for (Map.Entry<UUID, Integer> entry : heartsLost.entrySet())
        {
            playersTag.putInt(entry.getKey().toString(), entry.getValue());
        }
        tag.put("players", playersTag);

        CompoundTag cooldownTag = new CompoundTag();
        for (Map.Entry<UUID, Long> entry : cooldownExpiry.entrySet())
        {
            cooldownTag.putLong(entry.getKey().toString(), entry.getValue());
        }
        tag.put("cooldowns", cooldownTag);

        CompoundTag graceTag = new CompoundTag();
        for (Map.Entry<UUID, Long> entry : deathGraceExpiry.entrySet())
        {
            graceTag.putLong(entry.getKey().toString(), entry.getValue());
        }
        tag.put("deathGrace", graceTag);

        CompoundTag combatTag = new CompoundTag();
        for (Map.Entry<UUID, Long> entry : combatExpiry.entrySet())
        {
            combatTag.putLong(entry.getKey().toString(), entry.getValue());
        }
        tag.put("combat", combatTag);

        CompoundTag bedRegenTag = new CompoundTag();
        for (Map.Entry<UUID, Long> entry : bedRegenExpiry.entrySet())
        {
            bedRegenTag.putLong(entry.getKey().toString(), entry.getValue());
        }
        tag.put("bedRegen", bedRegenTag);

        return tag;
    }

    private static HeartLossData load(CompoundTag tag)
    {
        HeartLossData data = new HeartLossData();
        CompoundTag playersTag = tag.getCompound("players");
        for (String key : playersTag.getAllKeys())
        {
            data.heartsLost.put(UUID.fromString(key), playersTag.getInt(key));
        }
        CompoundTag cooldownTag = tag.getCompound("cooldowns");
        for (String key : cooldownTag.getAllKeys())
        {
            data.cooldownExpiry.put(UUID.fromString(key), cooldownTag.getLong(key));
        }
        CompoundTag graceTag = tag.getCompound("deathGrace");
        for (String key : graceTag.getAllKeys())
        {
            data.deathGraceExpiry.put(UUID.fromString(key), graceTag.getLong(key));
        }
        CompoundTag combatTag = tag.getCompound("combat");
        for (String key : combatTag.getAllKeys())
        {
            data.combatExpiry.put(UUID.fromString(key), combatTag.getLong(key));
        }
        CompoundTag bedRegenTag = tag.getCompound("bedRegen");
        for (String key : bedRegenTag.getAllKeys())
        {
            data.bedRegenExpiry.put(UUID.fromString(key), bedRegenTag.getLong(key));
        }
        return data;
    }

    public static HeartLossData get(ServerLevel level)
    {
        return level.getDataStorage().computeIfAbsent(HeartLossData::load, HeartLossData::new, DATA_NAME);
    }
}
