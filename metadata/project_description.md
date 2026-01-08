Data-driven piglin bartering! Define custom trade items and loot tables via datapacks. Includes tiered trades for gold nuggets, ingots, and blocks with JEI support.

## Features

**Better Piglin Trades** transforms piglin bartering into a fully data-driven system that modpack makers can customize without writing any code.

### Tiered Trading System

The mod includes three default trade tiers:

| Trade Item | Reward Quality |
|------------|----------------|
| **Gold Nugget** | Basic rewards - iron nuggets, soul sand, fire charges |
| **Gold Ingot** | Standard rewards - ender pearls, potions, obsidian (vanilla-like) |
| **Gold Block** | Premium rewards - netherite scrap, diamonds, enchanted books |

### Fully Customizable

Create your own trades by adding JSON files to your datapack:

**Trade Definition** (`data/mypack/piglin_trades/emerald.json`):
```json
{
  "item": "minecraft:emerald",
  "loot_table": "mypack:gameplay/piglin_bartering/emerald",
  "priority": 0
}
```

Then create a matching loot table with your desired rewards!

### JEI Integration

When JEI is installed, press "U" on any trade item to see all possible barter rewards. The display shows:
- The input item required
- All possible output items with average quantities

## For Modpack Makers

- **Add new trades**: Create trade definitions for any item
- **Override existing trades**: Use the `priority` field (higher wins)
- **Remove trades**: Point to an empty loot table
- **Balance rewards**: Full control over loot table weights and quantities

## Requirements

- Minecraft 1.21.1
- NeoForge 21.1+

## Optional

- JEI (Just Enough Items) - For viewing possible barter outputs
