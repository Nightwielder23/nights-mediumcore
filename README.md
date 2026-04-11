# Night's Mediumcore

A Minecraft 1.20.1 Forge mod that adds a permanent heart loss system. Death carries real consequences without being as brutal as Hardcore mode.

## What This Mod Does

Every time you die, you permanently lose one maximum heart, down to a minimum of 3 hearts. To get them back you need to craft and use a Crystal Heart, which requires a rare new ore found deep underground. If you prefer, you can also configure the mod so that Golden Apples restore hearts instead, or have both options available at once.

It's designed for players who want survival to feel meaningful. Death matters here, but it's never game over.

## Items and Blocks

**Heart Ore** generates underground between Y=-20 and Y=20, slightly rarer than gold. You need at least an iron pickaxe to mine it. Each ore drops 1 to 2 Crystal Shards and a bit of XP, and Fortune enchantments work on it.

**Crystal Shards** are what you get from mining Heart Ore. They're used to craft Crystal Hearts.

**Crystal Hearts** are crafted from one Crystal Shard surrounded by four Gold Ingots. Right-clicking one restores a single maximum heart. There's a 2 minute cooldown between uses and you can't use one within 3 minutes of being in combat.

**Supreme Crystal Hearts** are crafted from four Crystal Hearts, four Gold Blocks, and one Crystal Shard. Right-clicking one restores all of your lost maximum hearts at once with no cooldown.

## Golden Apple Mode

The mod supports three heart recovery modes that you can switch between at any time. In **crystal mode** (the default), only Crystal Hearts restore hearts. In **apple mode**, consuming a regular Golden Apple restores one heart and an Enchanted Golden Apple restores all hearts, on top of their normal vanilla effects. In **both mode**, Crystal Hearts and Golden Apples both work. You can change the mode in the config file or with the `/nightsmediumcore mode` command.

## Bed Regen

Sleeping in a bed will restore one maximum heart if you are currently below 7 hearts. This has a configurable cooldown that defaults to 15 minutes, so you can't just spam sleep to recover.

## Commands

`/nightsmediumcore hearts` shows your current base mediumcore heart count and doesn't require any permissions, so any player can check their own status. Adding `total` at the end shows your full heart count including any bonuses added by other mods.

The following commands require OP level 2:

`/nightsmediumcore addheart <player> <amount>` adds max hearts to a player.
`/nightsmediumcore removeheart <player> <amount>` removes max hearts from a player.
`/nightsmediumcore setheart <player> <amount>` sets a player's max hearts to a specific number.
`/nightsmediumcore restoreheart <player>` fully restores all hearts for a player.
`/nightsmediumcore mode <crystal|apple|both>` changes the heart recovery mode at runtime.

## Configuration

A config file is generated at `config/nightsmediumcore-common.toml` the first time you launch. You can adjust the heart floor, the grace period after death, the combat cooldown for crystals, the bed regen cooldown, and the heart recovery mode all from there without touching any code.

## Installation

Download Minecraft Forge 1.20.1 (build 47.4.10 recommended) from the official Forge site, then place the Night's Mediumcore jar file in your mods folder and launch the game. No other mods are required.

## License

This mod is licensed under CC BY-NC 4.0. You are free to use it in modpacks and share it as long as you credit Nightwielder23. Commercial use is not permitted. Full license details at https://creativecommons.org/licenses/by-nc/4.0/

## Author

Made by Nightwielder23. You can find my other projects at https://github.com/Nightwielder23
