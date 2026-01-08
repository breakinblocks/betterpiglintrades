# Better Piglin Trades

A NeoForge mod for Minecraft 1.21.1 that makes piglin bartering fully data-driven via datapacks.

## Features

- **Data-Driven Trades**: Define custom bartering items and their rewards using JSON files
- **Datapack Support**: Modpack makers can add, modify, or remove trades without code changes
- **JEI Integration**: View all possible barter outputs when checking uses on trade items
- **Tiered Rewards**: Includes default trades for gold nuggets, gold ingots, and gold blocks with appropriately scaled rewards

## Default Trades

| Trade Item | Reward Tier |
|------------|-------------|
| Gold Nugget | Small rewards (iron nuggets, soul sand, fire charges) |
| Gold Ingot | Vanilla-like rewards (ender pearls, potions, obsidian) |
| Gold Block | Premium rewards (netherite scrap, diamonds, enchanted books, 2-4 rolls) |

## Creating Custom Trades

An example datapack is included in the `exampledatapack/` folder for reference.

### Datapack Structure

```
my_datapack/
├── pack.mcmeta
└── data/my_namespace/
    ├── piglin_trades/
    │   └── cobblestone.json        # Trade definition
    └── loot_table/gameplay/piglin_bartering/
        └── cobblestone.json        # Loot table
```

### pack.mcmeta

```json
{
  "pack": {
    "pack_format": 48,
    "description": "My piglin trades"
  }
}
```

### Trade Definition

Create a JSON file in `data/<namespace>/piglin_trades/<name>.json`:

```json
{
  "item": "minecraft:cobblestone",
  "loot_table": "my_namespace:gameplay/piglin_bartering/cobblestone",
  "priority": 0
}
```

**Fields:**
- `item`: The item piglins will accept for bartering (registry name)
- `loot_table`: The loot table to use for generating rewards
- `priority`: Higher priority trades override lower ones for the same item (default: 0)

### Loot Table

Create a matching loot table at `data/<namespace>/loot_table/<path>.json`:

```json
{
  "type": "minecraft:barter",
  "pools": [
    {
      "rolls": 1,
      "entries": [
        {
          "type": "minecraft:item",
          "name": "minecraft:ender_pearl",
          "weight": 10
        },
        {
          "type": "minecraft:item",
          "name": "minecraft:ghast_tear",
          "weight": 5
        },
        {
          "type": "minecraft:item",
          "name": "minecraft:blaze_powder",
          "weight": 10
        }
      ]
    }
  ]
}
```

The `weight` field controls relative drop chances. Use `functions` for count ranges:

```json
{
  "type": "minecraft:item",
  "name": "minecraft:diamond",
  "weight": 10,
  "functions": [
    {
      "function": "minecraft:set_count",
      "count": { "type": "minecraft:uniform", "min": 1, "max": 3 }
    }
  ]
}
```

## Overriding Default Trades

To override a default trade, create a trade definition with the same item and a higher priority:

```json
{
  "item": "minecraft:gold_ingot",
  "loot_table": "mypack:gameplay/piglin_bartering/custom_gold_ingot",
  "priority": 100
}
```

## Removing Trades

To effectively remove a trade, create a trade definition that points to an empty loot table.

## Requirements

- Minecraft 1.21.1
- NeoForge 21.1.x

## Optional Dependencies

- **JEI** (Just Enough Items): Shows possible barter outputs in recipe view. Loot table outputs are automatically synced from server to client, so JEI displays all possible outputs from both mod JAR trades and external datapacks.

## License

This project is licensed under the [MIT License](LICENSE.md).
