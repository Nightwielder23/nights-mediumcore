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
        return data;
    }

    public static HeartLossData get(ServerLevel level)
    {
        return level.getDataStorage().computeIfAbsent(HeartLossData::load, HeartLossData::new, DATA_NAME);
    }
}
