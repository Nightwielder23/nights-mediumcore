# Night's Mediumcore

A Minecraft 1.20.1 Forge mod adding permanent heart loss, a lifesteal system, and vampiric items and enchantments. Death has real weight without being as punishing as Hardcore mode.

> The lifesteal PvP system requires multiple players to fully test. Minor multiplayer issues may be addressed in 1.1.1.

## How It Works

Every time you die you permanently lose one maximum heart, down to a minimum of 3. To recover lost hearts you craft Crystal Hearts from a rare underground ore, or configure the mod to let Golden Apples restore hearts instead, or both.

With lifesteal enabled, killing a player has a chance to steal one of their hearts as a droppable Living Heart item. Both systems can run together or independently and are toggleable at any time.

## Recovery Modes

The mod has four recovery modes that control what restores hearts.

**None:** Crystal Hearts give regen only, no heart restore. Golden Apples give vanilla effects only. Only Living Hearts restore hearts. Items that do not restore hearts in the current mode have no cooldown.

**Crystal:** Crystal Hearts restore hearts. Golden Apples give vanilla effects only.

**Apple:** Golden Apples restore hearts. Crystal Hearts give regen only with no cooldown.

**Both:** Crystal Hearts and Golden Apples both restore hearts.

## Items

**Heart Ore** generates between Y=-20 and Y=20, slightly rarer than gold. Requires an iron pickaxe. Drops 1 to 2 Crystal Shards and XP. Fortune compatible.

**Crystal Shards** drop from Heart Ore and are used to craft Crystal Hearts.

**Crystal Hearts** are crafted from one Crystal Shard surrounded by four Gold Ingots. In crystal or both recovery with mediumcore on: restores 1 heart and gives Regen 2. In apple or none recovery with mediumcore on: gives Regen 3 (apple) or Regen 2 (none) with no cooldown since no heart is restored. In lifesteal only with crystal or none recovery: restores 1 heart, no regen. In lifesteal only with both recovery: restores 1 heart and Regen 2. Has a 3 minute cooldown only when actually restoring a heart. Cannot be used within 30 seconds of combat when restoring hearts. No cooldown in creative mode.

**Supreme Crystal Hearts** are crafted from five Crystal Hearts and four Gold Blocks. In crystal or both recovery with mediumcore on: restores all hearts and gives Regeneration 2, Absorption 4, Resistance, and Fire Resistance for 30 seconds. In apple or none recovery with mediumcore on: gives those effects only, no heart restore. In lifesteal only with crystal or none: restores 2 lifesteal hearts up to the configured total cap, no extra effects. In lifesteal only with both: restores 2 lifesteal hearts up to cap and gives Regen 2. No cooldown.

**Living Hearts** drop from players killed in lifesteal mode and can be freely traded. With mediumcore on: restores 1 heart and gives Regen 2 regardless of recovery mode. In lifesteal only with crystal, apple, or none recovery: restores 1 heart, no regen. In lifesteal only with both recovery: restores 1 heart and Regen 2. When lifesteal is off, Living Hearts share the Crystal Heart cooldown. Soulbound; drops on death if mediumcore is enabled.

**Blood Shards** drop from Evokers at 3% and are found in Stronghold library chests at 2%. Used to craft Bloody Hearts.

**Bloody Hearts** are crafted from four Blood Shards surrounding one Living Heart. Used to upgrade the Blood Relic and craft the Vampiric Scythe.

**Heart Relics** are crafted from eight Crystal Hearts surrounding one Supreme Crystal Heart. Increases maximum hearts by 20% rounded up and gives permanent Regen 1. Works in the Curios charm slot or anywhere in your main inventory without Curios.

**Blood Relics** upgrade the Heart Relic at a smithing table using a Heart Relic, a Netherite Upgrade Template, and a Bloody Heart. Keeps all Heart Relic effects and adds a 0.5% additive chance for hostile mobs to drop a Living Heart on kill.

**Vampiric Scythes** are crafted at a smithing table using a Netherite Hoe, a Netherite Upgrade Template, and a Bloody Heart. Has a 0.5% additive chance for mobs to drop a Living Heart on kill and heals 12% of all damage dealt. Cannot have Vampirism, Life Steal, or Life Leech applied to it.

## Golden Apples

In apple or both recovery mode, a regular Golden Apple restores 1 heart plus all vanilla effects. An Enchanted Golden Apple with mediumcore on restores all hearts plus vanilla effects. With lifesteal only active, an Enchanted Golden Apple restores 2 lifesteal hearts instead of all. In crystal or none recovery, neither apple type restores hearts; vanilla effects only apply.

If a golden apple heart restore cooldown is active you will see a private message. Vanilla apple effects always apply regardless. In modes where apples do not restore hearts they have no cooldown.

## Enchantments

**Vampirism** adds 0.1% per level chance for hostile mobs to drop a Living Heart on kill. Max level 3. Found in Nether Fortress and Ancient City chests.

**Life Steal** heals 3% of damage dealt to hostile mobs per level. Max level 3. Found in Stronghold library and Bastion Treasure chests.

**Life Leech** heals 3% of damage dealt to players per level. Max level 3. Found in Stronghold library and Bastion Treasure chests.

None of these can be applied to the Vampiric Scythe.

## Passive Features

Sleeping in a bed restores one heart if your base hearts are below the configured threshold (default 7) and the cooldown has expired. Regen 1 for 30 seconds always applies on wake.

Respawning gives 5 seconds of complete damage immunity from all sources.

Dying twice within 60 seconds only loses one heart. The grace period does not trigger if you are already at the 3 heart minimum.

## Commands

All commands below require OP level 2 unless noted.

`/nightsmediumcore hearts` shows mediumcore hearts, for example 3/10. No permission required.

`/nightsmediumcore hearts living` shows lifesteal hearts, for example 3/10. No permission required.

`/nightsmediumcore hearts total` shows all hearts including accessories. No permission required.

`/nightsmediumcore addheart <player> <amount>` adds mediumcore hearts.

`/nightsmediumcore removeheart <player> <amount>` removes hearts, lifesteal first.

`/nightsmediumcore setheart <player> <amount>` sets total hearts, lifesteal adjusted first.

`/nightsmediumcore restoreheart <player>` restores all hearts.

`/nightsmediumcore recovery <none|crystal|apple|both>` changes recovery mode.

`/nightsmediumcore mode lifesteal` toggles lifesteal system.

`/nightsmediumcore mode mediumcore` toggles mediumcore system.

`/nightsmediumcore mode both` toggles both systems.

`/nightsmediumcore give hearts <player> <amount>` transfers hearts to another player, lifesteal first.

`/nightsmediumcore convert crystal <amount>` converts hearts to Crystal Heart items, lifesteal first.

`/nightsmediumcore convert living <amount>` converts Crystal Hearts to Living Heart items.

`/nightsmediumcore clearcooldown <player>` clears all cooldowns.

## Configuration

Config generates at `config/nightsmediumcore-common.toml` on first launch.

`mediumcoreEnabled` toggles mediumcore heart loss. Default true.

`heartFloor` sets the minimum hearts a player can reach. Default 3.

`deathGracePeriodSeconds` sets seconds before another heart can be lost after death. Default 60.

`crystalCombatCooldownSeconds` sets seconds after combat before Crystal Hearts can restore hearts. Default 30.

`bedRegenCooldownMinutes` sets minutes between bed heart regen. Default 15.

`bedRegenHeartThreshold` sets max base hearts at which bed regen still triggers. Default 7.

`heartRecoveryMode` sets recovery mode: none, crystal, apple, or both. Default crystal.

`appleCooldownSeconds` sets cooldown between apple heart restores in seconds. Default 0.

`appleCombatCooldown` controls whether combat blocks apple heart restore. Default false.

`showHardcoreHearts` toggles custom heart style. Default true.

`respawnImmunityEnabled` toggles respawn immunity. Default true.

`respawnImmunitySeconds` sets seconds of respawn immunity. Default 5.

`lifeStealEnabled` toggles lifesteal system. Default false.

`lifeStealDropChance` sets percentage chance a killed player drops a Living Heart. Default 50 with mediumcore, 100 lifesteal only.

`lifeStealHeartCap` sets max lifesteal hearts a player can have. Default 10.

`lifeStealRespawnCooldown` sets seconds of lifesteal protection after respawn. Default 60.

## Installation

Download Minecraft Forge 1.20.1 (build 47.4.10 recommended), place the jar in your mods folder, and launch. No other mods required.

## Compatibility

Curios API is an optional dependency for the Heart Relic and Blood Relic charm slot. Download at https://modrinth.com/mod/curios

JEI is an optional dependency for browsing recipes and items. Download at https://modrinth.com/mod/jei

## Credits

Crystal Heart and Supreme Crystal Heart textures by Stella Heartilly: https://stella-heartilly.itch.io/heart-gems

Crystal Shard and Blood Shard textures by Pekschi: https://pekschi.itch.io/crystal-animation

Bloody Heart and Living Heart textures by Redreeh: https://redreeh.itch.io/pixelhearts-16x16

Heart Relic texture by studionamepending: https://studionamepending.itch.io/heart-pickup-animated

Vampiric Scythe and Blood Relic textures are placeholder and will be updated in a future release.

## License

Licensed under CC BY-NC 4.0. Free to use in modpacks with credit to Nightwielder23. No commercial use. Full license at https://creativecommons.org/licenses/by-nc/4.0/

## Author

Made by Nightwielder23: https://github.com/Nightwielder23