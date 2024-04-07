![MC Drops Logo](./src/main/resources/mcdrops_logo.png)

<a href="LICENSE.md"><img align="right" alt="AGPL-3.0 Logo"
src="https://www.gnu.org/graphics/agplv3-155x51.png">
</a>

MCDrops is a mod that periodically drops random items for active players.

This is currently a **Forge** mod for Minecraft **1.18.2**.

# Configuration
The mod automatically creates config files for the mod on first start.
These are **server-specific** configs, so you'll find them under `saves/<your world>/serverconfig/mcdrops/`.
They include comments, and should be fairly self-explanatory to customize.

When in doubt, check the log files. The mod outputs info / warning messages for improper configuration.

## Drop interval
Drops can be configured to happen every X seconds or in a random interval.
The random interval is recalculated after each drop, so it will be different every time.

```toml
# Every 10 minutes
drop_interval = 600

# Or, each delay is a random interval between 5 and 20 minutes
enable_variable_interval = true
min_drop_interval = 300
max_drop_interval = 1200
```

## Defining a drop
Create an entry like this in your world's `drops.toml` file:

```toml
[[drops]]
tag = "registry id here"
weight = 20
count = 5
min = 5
max = 15
```
- `tag` -- This is the registry ID for the dropped item (e.g. `minecraft:torch`).
  Items from other mods can be used here too.
  If the item can't be found when the mod loads, it is skipped (i.e. the mod fails safe).
- `weight` -- The chance this item is dropped. 
  This is internally converted to a percentage based on the sum of all weights.
- `count` -- The number of the given item to drop. Defaults to `1`.

### Random ranges
If you use `min` and/or `max` instead of `count` for the drop amount, the mod
will drop a random amount of the given item. `count` takes priority over `min` and `max`,
so don't combine them!

- `min` -- The lower bound of the random drop amount (inclusive). Defaults to `1`.
- `max` -- The upper bound of the random drop amount (inclusive). Defaults to `min`.

# Credits
The Funbox crew for being my guinea pigs.<br/>
Logo generated with https://fontmeme.com/minecraft-font/

# License
Copyright 2014 [Mimickal](https://github.com/Mimickal)<br/>
This code is licensed under the
[AGPL-3.0](https://www.gnu.org/licenses/agpl-3.0-standalone.html) license.<br/>
Basically, any modifications to this code must be made open source.
