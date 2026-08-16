# Better Piglin Trades

A NeoForge mod for Minecraft 1.21.1 that makes piglin bartering fully data-driven via datapacks.

## Features

- **Data-Driven Trades**: Define custom bartering items and their rewards using JSON files
- **Datapack Support**: Modpack makers can add, modify, or remove trades without code changes
- **JEI Integration**: View all possible barter outputs, with the chance of each one, when checking uses on trade items
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
- `loot_table`: The loot table to use for generating rewards. Required unless `enabled` is false
- `priority`: Higher priority trades override lower ones for the same item (default: 0)
- `enabled`: Set to false to take the item out of bartering entirely (default: true)

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

## Replacing Default Trades

Trade definitions are ordinary datapack files, so a pack loaded after the mod can replace one by writing a valid trade definition to the same path, for example `data/betterpiglintrades/piglin_trades/gold_block.json`. The `priority` field does the same job across namespaces.

## Removing Trades

Set `enabled` to false. The item stops being a barter currency, piglins no longer admire or accept it, and it disappears from JEI:

```json
{
  "item": "minecraft:gold_nugget",
  "enabled": false
}
```

`loot_table` is not needed on a disabled trade. Disabled definitions still take part in priority resolution, so to switch off a trade another pack added at priority 50, use a higher priority:

```json
{
  "item": "minecraft:gold_nugget",
  "enabled": false,
  "priority": 100
}
```

Pointing a trade at an empty loot table is not the same thing: piglins still accept and consume the item, they just give nothing back.

## Requirements

- Minecraft 1.21.1
- NeoForge 21.1.x

## Optional Dependencies

- **JEI** (Just Enough Items): Shows possible barter outputs in recipe view, sorted by chance, with the per-roll chance on each output's tooltip. Loot table outputs are synced from server to client, so JEI displays outputs from both mod JAR trades and external datapacks, in singleplayer and on servers.

  Chances are read from the loot table entry weights. Tables that roll more than once (the default gold block trade rolls 2 to 4 times) show the chance for a single roll, so an item can turn up more often than its listed figure. `alternatives`, `group` and `sequence` entries are approximated by their weights rather than evaluated against conditions.

## License

This project is licensed under the [MIT License](LICENSE.md).
