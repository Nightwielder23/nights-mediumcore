# Night's Mediumcore

**A Minecraft 1.20.1 Forge mod that introduces a permanent heart loss system, making death carry real consequences without being as punishing as Hardcore mode.**

---

## Overview

Night's Mediumcore changes how death works in Minecraft. Every time you die, you permanently lose one maximum heart — down to a floor of 3 hearts. To recover lost hearts you must find and use Crystal Hearts, crafted from a rare new ore found deep underground.

This mod is designed for players who want survival to feel meaningful without the all-or-nothing nature of Hardcore mode. Death matters, but recovery is always possible.

---

## Features

### Heart Loss System
- Dying permanently removes 1 maximum heart
- Minimum floor of 3 hearts — you can never go below this
- A 60-second grace period after death prevents losing two hearts in quick succession
- Heart loss only affects your base health, not bonus hearts from other mods

### Heart Ore
- A new ore called **Heart Ore** generates underground between Y=-20 and Y=20
- Slightly rarer than gold ore
- Requires an iron pickaxe or better to mine
- Drops 1-2 Crystal Shards, with Fortune compatibility
- Drops 3-7 XP when mined

### Crystal Shards
- Dropped by Heart Ore
- Used to craft Crystal Hearts

### Crystal Heart
- Crafted from 1 Crystal Shard surrounded by 4 Gold Ingots
- Right-click to consume and restore 1 maximum heart
- 2-minute cooldown between uses
- Cannot be used within 3 minutes of combat

### Supreme Crystal Heart
- Crafted from 4 Crystal Hearts, 4 Gold Blocks, and 1 Crystal Shard
- Right-click to consume and restore all lost maximum hearts
- No cooldown

### Bed Regen
- Sleeping in a bed restores 1 maximum heart if you are below 7 hearts
- Has a configurable cooldown (default 15 minutes)

### Commands
| Command | Permission | Description |
|---|---|---|
| `/nightsmediumcore hearts` | Anyone | Shows your current base mediumcore hearts |
| `/nightsmediumcore hearts total` | Anyone | Shows total hearts including bonuses from other mods |
| `/nightsmediumcore addheart <player> <amount>` | OP | Adds max hearts to a player |
| `/nightsmediumcore removeheart <player> <amount>` | OP | Removes max hearts from a player |
| `/nightsmediumcore setheart <player> <amount>` | OP | Sets a player's max hearts |
| `/nightsmediumcore restoreheart <player>` | OP | Restores all hearts for a player |

---

## Installation

1. Install [Minecraft Forge 1.20.1](https://files.minecraftforge.net/) (recommended build 47.4.10)
2. Download the latest release of Night's Mediumcore
3. Place the `.jar` file in your `mods` folder
4. Launch the game

---

## Configuration

A config file is generated at `config/nightsmediumcore-common.toml` on first launch. The following options are available:

| Option | Default | Description |
|---|---|---|
| `heartFloor` | 3 | Minimum hearts a player can reach |
| `deathGracePeriodSeconds` | 60 | Seconds after death before another heart can be lost |
| `crystalCombatCooldownSeconds` | 180 | Seconds after combat before crystals can be used |
| `bedRegenCooldownMinutes` | 15 | Minutes between bed heart regeneration |
| `showHardcoreHearts` | true | Whether to show custom heart style |

---

## License

This mod is licensed under [CC BY-NC 4.0](https://creativecommons.org/licenses/by-nc/4.0/).
You must credit **Nightwielder23** when using this mod in any work. Commercial use is not permitted.

---

## Author

Made by **Nightwielder23**
GitHub: [https://github.com/Nightwielder23](https://github.com/Nightwielder23)
