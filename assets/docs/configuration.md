# Configuration (`config.yml`)

`plugins/Pewpew/config.yml` holds the server-wide options. Everything item-related lives in `items/` instead; plugin
hooks, `messages.yml` and `sounds.yml` are covered in [integrations.md](integrations.md). Run `/pewpew reload` after
editing.

| Option                      | Type | Default | Description                                                                                     |
|-----------------------------|------|---------|---------------------------------------------------------------------------------------------------|
| `config-version`            | int  | -       | Format version, bumped by releases that change this file. Pewpew migrates it on startup and keeps a `.bak` next to it. Do not edit by hand. |
| `compatibility.legacy-spread` | bool | `false` | Old spread rotated around the world axes: a square cone that shrank when looking up or down. `false` is a true cone of `spread` degrees. Turning it off changes where pellets land, so retune `spread`. |
| `advanced.extends`          | bool | `false` | Allow `extends:` on items. See [common fields](common-fields.md#inheriting-from-another-item).   |
| `advanced.abstract`         | bool | `false` | Allow `abstract: true` template entries. Needs `extends` to be useful.                           |
| `advanced.ammo-stats`       | bool | `false` | Ammo items carry `damageMultiplier`, `velocityMultiplier` and `penetration`. Off means ammo is only a counter. See [ammo.md](ammo.md#ammo-stats). |
| `advanced.magazines`        | bool | `false` | Magazines become real items that hold rounds. Off means `MAGAZINE` items are not registered at all. See [magazines.md](magazines.md). |
| `impacts.enabled`           | bool | `true`  | A shot that lands on a block leaves a small dark square where it hit.                            |
| `impacts.max-per-gun`       | int  | `8`     | Newest impacts kept per gun; firing past this removes that gun's oldest. Each one also disappears after 30 seconds. Minimum `1`. |
| `auto-reload`               | bool | `false` | Firing a gun that just ran dry starts a reload on its own instead of only dry-firing. Held automatic fire never auto-reloads: the trigger has to be pressed again. |
| `lore.stat-display`         | bool | `true`  | Append generated stat lines to gun and attachment lore. `false` leaves only your own `lore`.     |
| `integrations.*`            | -    | -       | WorldGuard, CombatTagPlus and OpenMinetopia hooks. See [integrations.md](integrations.md).        |

While an `advanced` feature is off, an item using it loads as if the field were not there and Pewpew logs a warning
naming the option to enable, rather than silently doing nothing.

Impacts are display entities: they are never written to disk and are gone after a restart.
