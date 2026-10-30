# Recycler Bench

A mod that adds a recycler bench for Minecraft 1.21.1 (NeoForge) and Minecraft 1.20.1 (Forge). Put a tool or a piece of armor in it and it grinds the item back into part of the ingredients used to craft it.

A diamond chestplate costs 8 diamonds. At the default rate of 0.6 that is `8 * 0.6 = 4.8`, which rounds to **5 diamonds**. Values of 0.5 and above round up (`4.5` becomes `5`). Values below 0.5 round down (`4.3` becomes `4`, `0.4` becomes nothing).

## Crafting

The bench is crafted with four iron ingots, three copper ingots, a hopper, and a piston:

```
ICI
CHC
IPI
```

`I` iron ingot, `C` copper ingot, `H` hopper, `P` piston. It needs a stone pickaxe or better to pick back up.

## What can be recycled

Swords, tools, bows, crossbows, tridents, maces, fishing rods, shears, shields, elytra, brushes, flint and steel, and armor (including horse and wolf armor). Modded items count when they use those classes or the `c:tools` / `c:armors` tags, and when the game has a crafting or smithing recipe for them.

The bench reads that recipe. Tag ingredients, such as the planks in a wooden pickaxe, return the first item in the tag. A custom line in the config replaces that result.

An item with durability under 50% remaining is destroyed and returns nothing. At exactly 50% it still pays out. Items without a durability bar ignore this.

Netherite gear returns only the netherite ingot. The smithing template and the diamond piece stay in the machine's recipe, but they are not given back. One ingot at 60% is `0.6`, which rounds up to 1, so the ingot comes back whole.

## Config

Edit `config/recyclerbench-common.toml` in the Minecraft instance, or use Mods → Recycler Bench → Config. On a dedicated server, edit the copy in the server's `config` folder. The useful keys are:

- `return_rate` — default `0.6`
- `min_durability` — default `0.5`. Remaining durability below this returns nothing. `0` ignores wear
- `netherite_ingot_only` — default `true`. A recipe that includes a netherite ingot returns only that ingot
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

`coolmod:ruby_chestplate` then returns `round(8 * return_rate)` rubies. A line with one ingredient returns only that item, so a modded tool can give back a single custom ingot:

```toml
extra_recipes = [
  "coolmod:ruby_sword=coolmod:ruby*1"
]
```

That line replaces the automatic recipe. The bench prints the durability and netherite rules above the inventory, and hovering them shows this format. Up to nine output stacks fit in the bench. If the output slots are full, the bench waits.

## Requirements

The two jars are separate. A 1.21.1 world needs the NeoForge jar, and a 1.20.1 world needs the Forge jar.

- Minecraft 1.21.1 with NeoForge 21.1.248 or a newer 21.1 build. Jar: `build/libs/recyclerbench-1.0.0.jar`
- Minecraft 1.20.1 with Forge 47.4.28 or a newer 47.x build. Jar: `forge-1.20.1/build/libs/recyclerbench-forge-1.20.1-1.0.0.jar`

## Build

Java 21 compiles the 1.21.1 project and Java 17 compiles the 1.20.1 project. Gradle downloads both. Java 24 can run Gradle itself.

```bat
gradlew build
cd forge-1.20.1
gradlew build
```

`gradlew runClient` launches the 1.21.1 client. `gradlew :forge-1.20.1:runClient` launches the 1.20.1 client.
