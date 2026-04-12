# Night's Mediumcore

A Minecraft 1.20.1 Forge mod adding permanent heart loss, an optional lifesteal system, and vampiric items and enchantments. Death has real weight without being as punishing as Hardcore mode.

> The lifesteal PvP system requires multiple players to fully test. Minor multiplayer issues may be addressed in 1.1.2.

## What This Mod Does

Every time you die you permanently lose one maximum heart, down to a minimum of 3. To recover you craft Crystal Hearts from a rare underground ore. Sleep in a bed to slowly recover hearts over time. That is the default experience.

The mod also includes an optional lifesteal system, configurable recovery modes, and a suite of vampiric items and enchantments for players who want a deeper experience. All of this is off by default and documented in the advanced sections below.

## Items

**Heart Ore** generates between Y=-20 and Y=20, slightly rarer than gold. Requires an iron pickaxe. Drops 1 to 2 Crystal Shards and XP. Fortune compatible.

**Crystal Shards** drop from Heart Ore and are used to craft Crystal Hearts.

**Crystal Hearts** are crafted from one Crystal Shard surrounded by four Gold Ingots. Right clicking one restores a lost heart and gives a brief regen boost. Has a 3 minute cooldown and cannot be used within 30 seconds of combat. No cooldown in creative mode.

**Supreme Crystal Hearts** are crafted from five Crystal Hearts and four Gold Blocks. Right clicking one restores all lost hearts at once and gives the same effects as an Enchanted Golden Apple. No cooldown.

**Heart Relics** are crafted from eight Crystal Hearts surrounding one Supreme Crystal Heart. Increases your maximum hearts by 20% rounded up and gives permanent Regen 1 while equipped. Works in the Curios charm slot or anywhere in your main inventory without Curios. Right click to auto-equip if Curios is installed.

## Bed Regen

Sleeping restores one heart if your base hearts are below the configured threshold (default 7) and the cooldown has expired. Regen 1 for 30 seconds always applies on wake regardless of cooldown.

## Respawn Immunity

Respawning gives 5 seconds of complete damage immunity so you have time to react without instantly dying again.

## Death Grace Period

Dying twice within 60 seconds only loses one heart. The grace period does not trigger if you are already at the 3 heart minimum.

## Commands

All commands require OP level 2 unless noted.

`/nightsmediumcore hearts` shows your mediumcore hearts, for example 3/10. No permission required.

`/nightsmediumcore hearts living` shows your lifesteal hearts, for example 3/10. No permission required.

`/nightsmediumcore hearts total` shows all hearts including accessory bonuses. No permission required.

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

---

## Advanced: Recovery Modes

The recovery mode controls what items restore hearts. Change it with `/nightsmediumcore recovery` or in the config.

**None:** Crystal Hearts give regen only with no heart restore and no cooldown. Golden Apples give vanilla effects only. Only Living Hearts restore hearts.

**Crystal (default):** Crystal Hearts restore hearts. Golden Apples give vanilla effects only.

**Apple:** Golden Apples restore hearts on top of their vanilla effects. Crystal Hearts give regen only with no cooldown.

**Both:** Crystal Hearts and Golden Apples both restore hearts.

**Crystal Heart effects by mode with mediumcore on:**
In crystal or both recovery: restores 1 heart, gives Regen 2.
In apple or none recovery: gives Regen 3 (apple) or Regen 2 (none), no heart restore, no cooldown.

**Supreme Crystal Heart effects by mode with mediumcore on:**
In crystal or both recovery: restores all hearts, gives Enchanted Golden Apple effects.
In apple or none recovery: gives those effects only, no heart restore.

**Golden Apple effects by mode:**
In apple or both recovery: restores 1 heart plus all vanilla effects.
In crystal or none recovery: vanilla effects only.

**Enchanted Golden Apple effects by mode with mediumcore on:**
In apple or both recovery: restores all hearts plus vanilla effects.
In crystal or none recovery: vanilla effects only.

---

## Advanced: Lifesteal System

Enable lifesteal with `/nightsmediumcore mode lifesteal` or set `lifeStealEnabled` to true in the config.

When lifesteal is on, killing another player has a configurable chance to permanently steal one of their hearts, dropping it as a Living Heart item. The victim always respects the 3 heart floor. Recently respawned players are protected for a configurable window.

Hearts from Living Hearts are tracked separately as lifesteal hearts. They are removed before mediumcore hearts on death. Crystal Hearts and Golden Apples restore mediumcore hearts first then lifesteal hearts. The total cap is 10 mediumcore and 10 lifesteal for 20 combined.

**Living Heart effects by mode:**
With mediumcore on in any recovery: restores 1 heart, gives Regen 2.
In lifesteal only with crystal, apple, or none recovery: restores 1 heart, no regen.
In lifesteal only with both recovery: restores 1 heart, gives Regen 2.
When lifesteal is off, Living Hearts share the Crystal Heart cooldown.
Living Hearts are soulbound and drop on death if mediumcore is enabled, but can be freely traded between players.

**Supreme Crystal Heart in lifesteal only mode:**
In crystal or none recovery: restores 2 lifesteal hearts up to the total cap, no extra effects.
In both recovery: restores 2 lifesteal hearts up to the total cap and gives Regen 2.

**Enchanted Golden Apple in lifesteal only mode:**
In apple or both recovery: restores 2 lifesteal hearts plus vanilla effects instead of all hearts.

**Vampiric Items:**

**Blood Shards** drop from Evokers at 3% and are found in Stronghold library chests at 2%. Used to craft Bloody Hearts.

**Bloody Hearts** are crafted from four Blood Shards surrounding one Living Heart. Used to upgrade the Blood Relic and craft the Vampiric Scythe.

**Blood Relics** upgrade the Heart Relic at a smithing table using a Heart Relic, a Netherite Upgrade Template, and a Bloody Heart. Keeps all Heart Relic effects and adds a 0.5% additive chance for hostile mobs to drop a Living Heart on kill.

**Vampiric Scythes** are crafted at a smithing table using a Netherite Hoe, a Netherite Upgrade Template, and a Bloody Heart. Has a 0.5% additive chance for mobs to drop a Living Heart on kill and heals 12% of all damage dealt. Cannot have Vampirism, Life Steal, or Life Leech applied to it.

**Enchantments:**

**Vampirism** adds 0.1% per level chance for hostile mobs to drop a Living Heart on kill. Max level 3. Found in Nether Fortress and Ancient City chests.

**Life Steal** heals 3% of damage dealt to hostile mobs per level. Max level 3. Found in Stronghold library and Bastion Treasure chests.

**Life Leech** heals 3% of damage dealt to players per level. Max level 3. Found in Stronghold library and Bastion Treasure chests.

None of these can be applied to the Vampiric Scythe.

---

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

---

## Installation

Download Minecraft Forge 1.20.1 (build 47.4.10 recommended), place the jar in your mods folder, and launch. No other mods required.

## Compatibility

Curios API is an optional dependency for the Heart Relic and Blood Relic charm slot. Download at https://modrinth.com/mod/curios

JEI is an optional dependency for browsing recipes and items. Download at https://modrinth.com/mod/jei

JER is supported automatically for Heart Ore spawn and drop information. Download at https://modrinth.com/mod/just-enough-resources-jer

Jade is supported automatically for block and item information overlays. Download at https://modrinth.com/mod/jade

Epic Fight is an optional dependency for Vampiric Scythe greatsword animations. Download at https://modrinth.com/mod/epic-fight

Better Combat is an optional dependency for Vampiric Scythe claymore attack animations. Download at https://modrinth.com/mod/better-combat

Patchouli is an optional dependency that adds the Heart Codex, an in-game guidebook covering all mod mechanics. New players receive it automatically on first join. Download at https://modrinth.com/mod/patchouli

## Credits

Crystal Heart and Supreme Crystal Heart textures by Stella Heartilly: https://stella-heartilly.itch.io/heart-gems

Crystal Shard and Blood Shard textures by Pekschi: https://pekschi.itch.io/crystal-animation

Bloody Heart and Living Heart textures by Redreeh: https://redreeh.itch.io/pixelhearts-16x16

Heart Relic texture by studionamepending: https://studionamepending.itch.io/heart-pickup-animated

Blood Relic texture by Free Game Assets: https://free-game-assets.itch.io/free-shield-and-amulet-rpg-icons

Vampiric Scythe texture is placeholder (netherite hoe) and will be updated in a future release.

## License

Licensed under CC BY-NC 4.0. Free to use in modpacks with credit to Nightwielder23. No commercial use. Full license at https://creativecommons.org/licenses/by-nc/4.0/

## Author

Made by Nightwielder23: https://github.com/Nightwielder23