![Banner for @deepsitegg/pewpew](/assets/images/banner.png)

**A fully data-driven gun plugin for Paper** Build guns, shotguns, snipers, SMGs, throwables and attachments entirely in
YAML no code, no resource pack required. Every weapon is a normal item identified by persistent data, so guns drop,
trade, and stack like anything else.

Pewpew aims to give you CrackShot style depth on a modern Paper API: real ballistics, recoil, attachments, ammo, and
per-weapon sounds and effects, all configurable.

---

![bStats](https://bstats.org/signatures/bukkit/pewpew.svg)

---
## Features

### Shooting

- **Hitscan** and **projectile** firing modes
- **Semi-auto** and **full-auto** (`automatic`) - hold to fire at the weapon's true fire rate
- **Burst fire** (`burstCount`)
- **Spread** cones and **camera recoil** that climbs as you fire
- **Movement accuracy** - per-state spread multipliers for sprinting, sneaking, walking, midair and swimming
- **Bloom** - the cone widens while you hold the trigger and recovers when you stop
- **Spray patterns** - give a gun a fixed recoil pattern players can learn and pull against, instead of random kick
- **Bullet drop** - ballistic arc for hitscan, gravity for projectiles
- **Buckshot** - multiple pellets per shot (`bulletCount`), each independently spread
- **Damage falloff** over distance

### Ammo & reloading

- Inventory-fed ammo by `ammoType`, or free-reloading magazines
- **Ammo stats** - per-ammo damage, velocity and entity penetration multipliers, so AP and subsonic rounds differ
- `MAGAZINE` and `SINGLE` (shell-by-shell) reload styles
- **Bolt/pump firearm action** with open/close cycle sounds
- Ammo count shown live as the item's **durability bar**

### Combat depth

- **Headshot** multipliers with a hitmarker ping
- **Critical hits** (chance + multiplier, stacks with headshots)
- **Shield disable** - bullets apply the axe effect to blocking players
- **Potion effects** on the victim and/or shooter
- **Custom death messages** per weapon
- Damage registers as real projectile damage, so Projectile Protection, knockback and kill credit all work
- **Configurable damage type** per weapon - set `minecraft:generic` to ignore armor and deal your exact configured damage

### Attachments

- **Scopes** - aim-down-sights zoom that steadies the weapon (configurable per scope)
- **Barrels** - damage / range modifiers
- **Grips** - recoil reduction
- **Magazines** - capacity and reload-speed modifiers
- Fit and swap attachments in a clean shift-click bench GUI; stats render automatically on the item

### Presentation

- Per-weapon **trail** and **impact particles**
- Configurable **fire / hit sounds** with volume and pitch - including **custom resource-pack sounds**
- Hit feedback messages
- Throwables: explosion, smoke, flash, poison, incendiary

### Tooling

- **Damage dummy** (`/dummy`) - shows your per-hit and total damage; shift-right-click it to fit armor and shields
- **Skript support** - 11 events, 9 expressions and 6 conditions, no addon needed. See [skript.md](assets/docs/skript.md)
- **Developer API** - 11 Bukkit events covering shooting, hits, kills, reloads, scoping, attachments and explosions

---

## Controls

| Action           | Input                                        |
|------------------|----------------------------------------------|
| Fire             | Right-click (hold for automatic)             |
| Reload           | Swap-hands key (default `F`)                 |
| Aim down sights  | Hold sneak with a scoped gun                 |
| Edit attachments | Swap-hands on a gun with attachment slots    |
| Equip dummy      | Shift-right-click a dummy with an empty hand |

---

## Commands & permissions

| Command                      | Description               | Permission                  |
|------------------------------|---------------------------|-----------------------------|
| `/pewpew give <id> [amount]` | Give yourself an item     | `pewpew.command.items.give` |
| `/pewpew list`               | List all registered items | `pewpew.command.items.list` |
| `/pewpew reload`             | Reload configs from disk  | `pewpew.command.reload`     |
| `/pewpew version`            | Show plugin version       | -                           |
| `/dummy`                     | Spawn a damage-test dummy | `pewpew.command.dummy`      |
| `/dummy clear`               | Remove all dummies        | `pewpew.command.dummy`      |

Command permissions default to op, and `pewpew.*` grants all of them.

Every item also gets its own permission, `pewpew.use.<id>`, registered when the item loads. These default to **true**,
so all weapons work out of the box. Take one away to gate a weapon:

```
-pewpew.use.awm          # nobody with this negation can shoot the AWM
pewpew.use.awm           # grant it back to a rank
```

The check covers shooting, throwing and opening the attachment bench, and the denial message is
`no-permission` in `messages.yml`.

---

## Configuration

Items live in `plugins/Pewpew/items/` (`guns.yml`, `ammo.yml`, `attachments.yml`, `throwables.yml`). Defaults are copied
on first run. Edit, then `/pewpew reload`.

A minimal gun:

```yaml
ak47:
  type: GUN
  name: "<gray>AK-47"
  itemModel: "minecraft:iron_horse_armor"
  baseDamage: 8.5
  fireRate: 4
  reloadTime: 60
  ammoType: rifle_round
  maxAmmo: 30
  consumesAmmo: true
  automatic: true
  firingMode: PROJECTILE
  projectileSpeed: 3.5
  range: 64.0
  spread: 2.0
  recoil: 1.2
  headshotMultiplier: 1.5
  allowedAttachmentSlots: [ SCOPE, BARREL, GRIP, MAGAZINE ]
```

Names and messages use [MiniMessage](https://docs.advntr.dev/minimessage/format.html).

**Full field reference:** see [`assets/docs/`](assets/docs/README.md) - every field for each item type with types,
defaults and descriptions:

- [Common fields](assets/docs/common-fields.md) - shared by all items
- [Guns](assets/docs/guns.md)
- [Ammo](assets/docs/ammo.md)
- [Attachments](assets/docs/attachments.md)
- [Throwables](assets/docs/throwables.md)

---

## License

Pewpew is licensed under the [PolyForm Noncommercial License 1.0.0](LICENSE.md) - free to use, modify, and share for
**noncommercial** purposes only.

**Additional permission:** as an exception to the noncommercial restriction, running Pewpew on a server that sells
ranks, cosmetics or other items, or that accepts donations, is permitted. That permission does **not** cover:

- selling, licensing or otherwise commercially distributing Pewpew, or a modified, rebranded or redesigned version of it
- selling Pewpew items - guns, ammo, attachments or throwables - or access to Pewpew features, as store, shop or
  webshop products

Copyright 2026 ThebigTijn (https://deepsite.gg). Redistributions must keep the license and the `Required Notice`
(see [NOTICE](NOTICE)).

