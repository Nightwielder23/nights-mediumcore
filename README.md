# Night's Mediumcore

A Minecraft 1.20.1 Forge mod that adds a permanent heart loss system, a lifesteal system, and a suite of vampiric items and enchantments. Death carries real consequences without being as brutal as Hardcore mode.

## What This Mod Does

Every time you die, you permanently lose one maximum heart, down to a minimum of 3 hearts. To get them back you need to craft and use a Crystal Heart, which requires a rare new ore found deep underground. If you prefer, you can also configure the mod so that Golden Apples restore hearts instead, or have both options available at once.

With the optional lifesteal system enabled, killing another player has a chance to steal one of their hearts, dropping it as a Living Heart item you can use to gain a permanent heart of your own.

Both the mediumcore and lifesteal systems can be toggled independently in the config or with commands, so you can run just one or both together.

> **Note:** The lifesteal PvP system requires multiple players to fully test. Some multiplayer interactions may have minor issues in this release that will be addressed in 1.1.1.

## Items and Blocks

**Heart Ore** generates underground between Y=-20 and Y=20, slightly rarer than gold. You need at least an iron pickaxe to mine it. Each ore drops 1 to 2 Crystal Shards and a bit of XP, and Fortune enchantments work on it.

**Crystal Shards** are what you get from mining Heart Ore. They are used to craft Crystal Hearts.

**Crystal Hearts** are crafted from one Crystal Shard surrounded by four Gold Ingots. Right-clicking one restores a single maximum heart, filling mediumcore hearts first then lifesteal hearts. In crystal mode it grants Regen 1 for 10 seconds on use.In crystal mode it grants Regen 2 for 10 seconds on use. In apple recovery mode only it grants Regen 3 for 30 seconds instead. There is a 3 minute cooldown between uses and you cannot use one within 30 seconds of being in combat. You will see a message if you try to use one while either cooldown is active. Cooldowns never apply in creative mode, and the item will not be consumed in creative mode either.

**Supreme Crystal Hearts** are crafted from five Crystal Hearts and four Gold Blocks. Right-clicking one restores all of your lost maximum hearts at once and grants Regeneration 2, Absorption 4, Resistance, and Fire Resistance for 30 seconds, the same effects as an Enchanted Golden Apple. There is no cooldown on Supreme Crystal Hearts.

**Living Hearts** are dropped by players when killed in lifesteal mode. Right-clicking one permanently increases your heart count by 1. If your mediumcore hearts are below 10 it fills mediumcore hearts first, otherwise it fills lifesteal hearts. When lifesteal mode is off, Living Hearts share the Crystal Heart cooldown and grant Regen 1 for 30 seconds on use. When lifesteal mode is on, Living Hearts have no cooldown. Living Hearts are soulbound and will drop on your death if mediumcore is enabled, but can be freely traded and given to other players.

**Blood Shards** are a rare drop from Evokers at 3% chance and can also be found in Stronghold library chests at 2% chance. They are used to craft Bloody Hearts.

**Bloody Hearts** are crafted from four Blood Shards surrounding one Living Heart. They are used as the upgrade material for both the Blood Relic and the Vampiric Scythe.

**Heart Relics** are crafted from eight Crystal Hearts surrounding one Supreme Crystal Heart. When worn in the Curios charm slot, or anywhere in your main inventory if Curios is not installed, the relic increases your maximum hearts by 20% rounded up and gives you a permanent Regen 1 effect while equipped. The bonus hearts show as an extra row above your base hearts. If Curios is installed you can right click the relic to equip it directly into the charm slot.

**Blood Relics** are an endgame upgrade to the Heart Relic, crafted at a smithing table using a Heart Relic, a Netherite Upgrade Smithing Template, and a Bloody Heart. The Blood Relic has all the effects of the Heart Relic and additionally gives a 0.5% additive chance for hostile mobs to drop a Living Heart on kill.

**Vampiric Scythes** are powerful endgame weapons crafted at a smithing table using a Netherite Hoe, a Netherite Upgrade Smithing Template, and a Bloody Heart. The scythe has a built in 0.5% additive chance for hostile mobs to drop a Living Heart on kill and heals you for 12% of all damage dealt. The Vampirism, Life Steal, and Life Leech enchantments cannot be applied to it.

## Lifesteal System

When lifesteal mode is enabled, killing another player has a configurable chance to permanently remove one of their hearts and drop it as a Living Heart item. The victim still respects the 3 heart floor and cannot be stolen from below that. Players who have recently respawned are protected from lifesteal for a configurable cooldown period to prevent spawn killing.

Hearts gained from Living Hearts are tracked separately as lifesteal hearts and are removed before mediumcore hearts when you die. Crystal Hearts and Golden Apples restore mediumcore hearts first, and if those are full they restore lifesteal hearts instead. Lifesteal hearts can be transferred and converted through commands just like mediumcore hearts.

The heart cap depends on which modes are active. In mediumcore only mode the cap is 10 mediumcore hearts. In lifesteal only mode the cap is 20 lifesteal hearts. When both are enabled the cap is 20 total hearts split as up to 10 mediumcore and up to 10 lifesteal.

## Golden Apple Mode

The mod supports three heart recovery modes that you can switch between at any time. In **crystal mode** (the default), only Crystal Hearts restore hearts. In **apple mode**, consuming a regular Golden Apple restores one heart and an Enchanted Golden Apple restores all hearts, on top of their normal vanilla effects. In **both mode**, Crystal Hearts and Golden Apples both work.

Golden Apples follow the same heart filling logic as Crystal Hearts, filling mediumcore hearts first then lifesteal hearts. If a cooldown is active when you eat a Golden Apple you will see a message letting you know. The vanilla apple effects still apply regardless.

By default there is no cooldown on apple heart restore and it works regardless of whether you are in combat. Both of these behaviours can be changed in the config. If a cooldown is set and you eat a Golden Apple before it expires, the heart restore simply will not happen that time.

## Enchantments

**Vampirism** can be applied to any weapon except the Vampiric Scythe. Each level adds a 0.1% additive chance for hostile mobs to drop a Living Heart on kill. Max level 3. Found as enchanted books in Nether Fortress chests and Ancient City chests with higher levels being rarer.

**Life Steal** can be applied to any weapon except the Vampiric Scythe. Each level heals you for 3% of damage dealt to hostile mobs. Max level 3. Found as enchanted books in Stronghold library chests and Bastion Treasure chests with higher levels being rarer.

**Life Leech** can be applied to any weapon except the Vampiric Scythe. Each level heals you for 3% of damage dealt to players. Max level 3. Found as enchanted books in Stronghold library chests and Bastion Treasure chests with higher levels being rarer.

## Bed Regen

Sleeping in a bed will restore one maximum heart if your current base heart count is below the configured threshold, which defaults to 7 hearts, and the cooldown has expired. You will always receive Regen 1 for 30 seconds when you wake up regardless of the cooldown.

## Respawn Immunity

When you respawn after dying you receive 5 seconds of complete damage immunity. All damage from all sources is blocked during this window so you have time to react without instantly dying again.

## Death Grace Period

If you die twice within 60 seconds only one heart is lost. A grace period activates after each death preventing consecutive heart loss. This grace period only triggers if you actually have hearts left to lose and will not show any message if you are already at the minimum of 3 hearts.

## Commands

`/nightsmediumcore hearts` shows your current mediumcore heart count and maximum, for example 3/10. Available to all players.

`/nightsmediumcore hearts living` shows your current lifesteal heart count and maximum, for example 3/10. Available to all players.

`/nightsmediumcore hearts total` shows your total heart count including all hearts and accessory bonuses as a plain number, for example 14. Available to all players.

`/nightsmediumcore addheart <player> <amount>` adds mediumcore hearts to a player. Requires OP level 2.

`/nightsmediumcore removeheart <player> <amount>` removes hearts from a player, removing lifesteal hearts first then mediumcore hearts. Requires OP level 2.

`/nightsmediumcore setheart <player> <amount>` sets a player's total heart count, adjusting lifesteal hearts first then mediumcore hearts. Requires OP level 2.

`/nightsmediumcore restoreheart <player>` fully restores all hearts for a player. Requires OP level 2.

`/nightsmediumcore recovery <crystal|apple|both>` changes the heart recovery mode at runtime. Requires OP level 2.

`/nightsmediumcore mode lifesteal` toggles the lifesteal system at runtime. Requires OP level 2.

`/nightsmediumcore mode mediumcore` toggles the mediumcore heart loss system at runtime. Requires OP level 2.

`/nightsmediumcore mode both` toggles both systems at runtime. Requires OP level 2.

`/nightsmediumcore give hearts <player> <amount>` transfers hearts from you to another player. Lifesteal hearts are transferred first. Requires OP level 2.

`/nightsmediumcore convert crystal <amount>` converts your hearts into Crystal Heart items. Lifesteal hearts are converted first. Requires OP level 2.

`/nightsmediumcore convert living <amount>` converts Crystal Hearts from your inventory into Living Heart items. Requires OP level 2.

`/nightsmediumcore clearcooldown <player>` clears all active cooldowns for a player. Requires OP level 2.

## Configuration

A config file is generated at `config/nightsmediumcore-common.toml` the first time you launch. The following options are available.

`mediumcoreEnabled` controls whether the mediumcore heart loss on death system is active. Default is true.

`heartFloor` controls the minimum number of hearts a player can reach. Default is 3.

`deathGracePeriodSeconds` controls how many seconds must pass after a death before another heart can be lost. Default is 60.

`crystalCombatCooldownSeconds` controls how many seconds after combat must pass before Crystal Hearts can be used. Default is 30.

`bedRegenCooldownMinutes` controls how many minutes must pass between bed heart regeneration. Default is 15.

`bedRegenHeartThreshold` controls the maximum base heart count at which bed regen will still work. Default is 7.

`heartRecoveryMode` sets the heart recovery mode. Valid values are crystal, apple, and both. Default is crystal.

`appleCooldownSeconds` sets a cooldown in seconds between apple heart restores. Default is 0 meaning no cooldown.

`appleCombatCooldown` controls whether apple heart restore is blocked during combat. Default is false.

`showHardcoreHearts` controls whether the custom heart style is shown. Default is true.

`respawnImmunityEnabled` controls whether players receive damage immunity on respawn. Default is true.

`respawnImmunitySeconds` controls how many seconds of immunity players receive on respawn. Default is 5.

`lifeStealEnabled` controls whether the lifesteal system is active. Default is false.

`lifeStealDropChance` controls the percentage chance a player drops a Living Heart when killed in lifesteal mode. Default is 50 if mediumcore is also enabled, 100 if only lifesteal is enabled.

`lifeStealHeartCap` controls the maximum lifesteal hearts a player can have. Default is 10.

`lifeStealRespawnCooldown` controls how many seconds after respawning a player is protected from lifesteal. Default is 60.

## Installation

Download Minecraft Forge 1.20.1 (build 47.4.10 recommended) from the official Forge site, then place the Night's Mediumcore jar file in your mods folder and launch the game. No other mods are required.

## Compatibility

Curios API is supported as an optional dependency. If installed, the Heart Relic and Blood Relic can be equipped in the charm slot and right clicking them in your hand will automatically equip them. Without Curios they work anywhere in your main inventory. Download Curios API at https://modrinth.com/mod/curios

JEI is supported as an optional dependency for conveniently browsing all mod recipes and items. Download JEI at https://modrinth.com/mod/jei

## Credits

The Crystal Heart and Supreme Crystal Heart textures are by Stella Heartilly, find the originals at https://stella-heartilly.itch.io/heart-gems

The Crystal Shard and Blood Shard textures are by Pekschi, find the originals at https://pekschi.itch.io/crystal-animation

The Bloody Heart and Living Heart textures are by Redreeh, find the originals at https://redreeh.itch.io/pixelhearts-16x16

The Heart Relic texture is by studionamepending, find the original at https://studionamepending.itch.io/heart-pickup-animated

The Vampiric Scythe and Blood Relic textures are placeholder and will be updated in a future release.

## License

This mod is licensed under CC BY-NC 4.0. You are free to use it in modpacks and share it as long as you credit Nightwielder23. Commercial use is not permitted. Full license details at https://creativecommons.org/licenses/by-nc/4.0/

## Author

Made by Nightwielder23. You can find my other projects at https://github.com/Nightwielder23