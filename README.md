# Recycler Bench

A NeoForge mod for Minecraft 1.21.1 (NeoForge 21.1.248) that adds a recycler bench. Put a tool or a piece of armor in it and it grinds the item back into part of the ingredients used to craft it.

A diamond chestplate costs 8 diamonds. At the default rate of 0.6 that is `8 * 0.6 = 4.8`, which rounds to **5 diamonds**. Values of 0.5 and above round up (`4.5` becomes `5`). Values below 0.5 round down (`4.3` becomes `4`, `0.4` becomes nothing).

## Crafting

The bench is crafted with seven iron ingots, a hopper, and a piston:

```
III
IHI
IPI
```

`I` iron ingot, `H` hopper, `P` piston. It needs a stone pickaxe or better to pick back up.

## What can be recycled

Swords, tools, bows, crossbows, tridents, maces, fishing rods, shears, shields, elytra, brushes, flint and steel, and armor (including horse and wolf armor). Modded items count when they use those classes or the `c:tools` / `c:armors` tags, and when the game has a crafting or smithing recipe for them.

The bench reads that recipe. Tag ingredients, such as the planks in a wooden pickaxe, return the first item in the tag. A custom line in the config replaces that result.

Smithing recipes are included, so netherite gear returns its template, netherite ingot, and diamond piece. Because a single ingredient at 60% is `0.6`, which rounds up to `1`, those one-count ingredients come back whole. Add the netherite item to `disabled_items` if a world should not allow that.

## Config

Edit `config/recyclerbench-common.toml` in the Minecraft instance, or use Mods → Recycler Bench → Config. On a dedicated server, edit the copy in the server's `config` folder. The useful keys are:

- `return_rate` — default `0.6`
- `process_ticks` — default `40` (two seconds)
- `play_chainsaw` — default `true`
- `automatic_equipment` — default `true`. Set this to `false` to recycle only `extra_recipes`
- `extra_recipes` — custom items, including gear from other mods
- `disabled_items` — item ids that are never accepted

Custom recipe format, with the full crafting cost before the return rate is applied:

```toml
extra_recipes = [
  "coolmod:ruby_chestplate=coolmod:ruby*8",
  "coolmod:ruby_pickaxe=coolmod:ruby*3,minecraft:stick*2"
]
```

`coolmod:ruby_chestplate` then returns `round(8 * return_rate)` rubies. Up to nine output stacks fit in the bench. If the output slots are full, the bench waits.

## Requirements

- Minecraft 1.21.1
- NeoForge 21.1.248 or a newer 21.1 build

## Build

Java 21 is required to compile (Gradle downloads it). Java 24 can run Gradle itself.

```bat
gradlew build
```

The mod jar is written to `build/libs`. `gradlew runClient` launches a client with the mod loaded.
