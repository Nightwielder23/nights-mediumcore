# Night's Mediumcore

A Minecraft 1.20.1 Forge mod that adds a permanent heart loss system. Death carries real consequences without being as brutal as Hardcore mode.

## What This Mod Does

Every time you die, you permanently lose one maximum heart, down to a minimum of 3 hearts. To get them back you need to craft and use a Crystal Heart, which requires a rare new ore found deep underground. If you prefer, you can also configure the mod so that Golden Apples restore hearts instead, or have both options available at once.

It is designed for players who want survival to feel meaningful. Death matters here, but it is never game over.

## Items and Blocks

**Heart Ore** generates underground between Y=-20 and Y=20, slightly rarer than gold. You need at least an iron pickaxe to mine it. Each ore drops 1 to 2 Crystal Shards and a bit of XP, and Fortune enchantments work on it.

**Crystal Shards** are what you get from mining Heart Ore. They are used to craft Crystal Hearts.

**Crystal Hearts** are crafted from one Crystal Shard surrounded by four Gold Ingots. Right-clicking one restores a single maximum heart and grants Regen 1 for 10 seconds as a small bonus for using it strategically. There is a 3 minute cooldown between uses and you cannot use one within 30 seconds of being in combat. You will see a message if you try to use one while either cooldown is active. Cooldowns never apply in creative mode, and the item will not be consumed in creative mode either.

**Supreme Crystal Hearts** are crafted from five Crystal Hearts and four Gold Blocks. Right-clicking one restores all of your lost maximum hearts at once and grants Regeneration 2, Absorption 4, Resistance, and Fire Resistance for 30 seconds, the same effects as an Enchanted Golden Apple. There is no cooldown on Supreme Crystal Hearts.

**Heart Relics** are crafted from eight Crystal Hearts surrounding one Supreme Crystal Heart. When worn in the Curios charm slot, or anywhere in your main inventory if Curios is not installed, the relic increases your maximum hearts by 20% rounded up and gives you a permanent Regen 1 effect while equipped. The bonus hearts show as an extra row above your base hearts. If Curios is installed you can right click the relic to equip it directly into the charm slot.

## Golden Apple Mode

The mod supports three heart recovery modes that you can switch between at any time. In **crystal mode** (the default), only Crystal Hearts restore hearts. In **apple mode**, consuming a regular Golden Apple restores one heart and an Enchanted Golden Apple restores all hearts, on top of their normal vanilla effects. In **both mode**, Crystal Hearts and Golden Apples both work.

By default there is no cooldown on apple heart restore and it works regardless of whether you are in combat. Both of these behaviours can be changed in the config if you want a stricter experience. If a cooldown is set and you eat a Golden Apple before it expires, you will still receive the vanilla apple effects but the heart restore simply will not happen that time.

## Bed Regen

Sleeping in a bed will restore one maximum heart if your current base heart count is below the configured threshold, which defaults to 7 hearts, and the cooldown has expired. You will always receive Regen 1 for 30 seconds when you wake up regardless of the cooldown.

## Respawn Immunity

When you respawn after dying you receive 5 seconds of complete damage immunity. All damage from all sources is blocked during this window so you have time to react without instantly dying again.

## Death Grace Period

If you die twice within 60 seconds only one heart is lost. A grace period activates after each death preventing consecutive heart loss. This grace period only triggers if you actually have hearts left to lose and will not show any message if you are already at the minimum of 3 hearts.

## Commands

`/nightsmediumcore hearts` shows your current and maximum base mediumcore hearts, for example Base hearts: 7/10. This command is available to all players without any permissions.

`/nightsmediumcore hearts total` shows your full heart count including any bonuses added by other mods or the Heart Relic, for example Total hearts: 12/12. Also available to all players.

`/nightsmediumcore addheart <player> <amount>` adds maximum hearts to a player. Requires OP level 2.

`/nightsmediumcore removeheart <player> <amount>` removes maximum hearts from a player. Requires OP level 2.

`/nightsmediumcore setheart <player> <amount>` sets a player's maximum hearts to a specific number. Requires OP level 2.

`/nightsmediumcore restoreheart <player>` fully restores all hearts for a player. Requires OP level 2.

`/nightsmediumcore mode <crystal|apple|both>` changes the heart recovery mode at runtime without a restart. Requires OP level 2.

`/nightsmediumcore clearcooldown <player>` clears all active cooldowns for a player. Requires OP level 2.

## Configuration

A config file is generated at `config/nightsmediumcore-common.toml` the first time you launch. The following options are available.

`heartFloor` controls the minimum number of hearts a player can reach. Default is 3.

`deathGracePeriodSeconds` controls how many seconds must pass after a death before another heart can be lost. Default is 60.

`crystalCombatCooldownSeconds` controls how many seconds after combat must pass before Crystal Hearts can be used. Default is 180.

`bedRegenCooldownMinutes` controls how many minutes must pass between bed heart regeneration. Default is 15.

`bedRegenHeartThreshold` controls the maximum base heart count at which bed regen will still work. Default is 7.

`heartRecoveryMode` sets the heart recovery mode. Valid values are crystal, apple, and both. Default is crystal.

`appleCooldownSeconds` sets a cooldown in seconds between apple heart restores. Default is 0 meaning no cooldown.

`appleCombatCooldown` controls whether apple heart restore is blocked during combat. Default is false.

`showHardcoreHearts` controls whether the custom heart style is shown. Default is true.

## Installation

Download Minecraft Forge 1.20.1 (build 47.4.10 recommended) from the official Forge site, then place the Night's Mediumcore jar file in your mods folder and launch the game. No other mods are required.

## Compatibility

Curios API is supported as an optional dependency. If installed, the Heart Relic can be equipped in the charm slot and right clicking it in your hand will automatically equip it. Without Curios it works anywhere in your main inventory. Download Curios API at https://modrinth.com/mod/curios

JEI is supported as an optional dependency for conveniently browsing all mod recipes and items. Download JEI at https://modrinth.com/mod/jei

## Credits

The Crystal Heart and Supreme Crystal Heart textures are by Temok, find the original at https://temok.itch.io/heart-container-animated-in-pixel-art

The Heart Relic texture is by studionamepending, find the original at https://studionamepending.itch.io/heart-pickup-animated

## License

This mod is licensed under CC BY-NC 4.0. You are free to use it in modpacks and share it as long as you credit Nightwielder23. Commercial use is not permitted. Full license details at https://creativecommons.org/licenses/by-nc/4.0/

## Author

Made by Nightwielder23. You can find my other projects at https://github.com/Nightwielder23
