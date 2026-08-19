# Ammo (`type: AMMO`)

Plus the [common fields](common-fields.md). Ammo items are consumed when a gun with `consumesAmmo: true` reloads,
matched by `ammoType`.

| Field           | Type   | Default | Required | Description                                                                                                                                   |
|-----------------|--------|---------|----------|-----------------------------------------------------------------------------------------------------------------------------------------------|
| `ammoType`      | string | -       | yes      | The type tag a gun matches against (e.g. `rifle_round`). A gun pulls only ammo whose `ammoType` equals its own.                               |
| `roundsPerItem` | int    | `1`     | no       | How many rounds one consumed item loads. `1` = a single round, `N` = a box of N, `0` = one item refills the **whole magazine** (a spare mag). |

## Ammo stats

Turn these on first:

```yaml
advanced:
  ammo-stats: true
```

While off, ammo is only a counter and the three fields below do nothing. While on, the ammo currently loaded into a gun
modifies that gun's numbers. Ammo always **multiplies** what the gun defines, so `baseDamage`, `damageType` and the
attachment modifiers stay in charge; ammo is the last layer on top.

| Field                | Type   | Default | Description                                                                                                       |
|----------------------|--------|---------|-------------------------------------------------------------------------------------------------------------------|
| `damageMultiplier`   | double | `1.0`   | Scales the gun's damage. `1.2` = hollow points, `0.8` = subsonic.                                                 |
| `velocityMultiplier` | double | `1.0`   | Scales projectile speed. Only affects `PROJECTILE` guns.                                                          |
| `penetration`        | int    | `0`     | How many entities a shot passes through before stopping. `0` = stops at the first target. Hitscan guns only.      |

```yaml
ap_round:
  type: AMMO
  name: "<gray>Armor Piercing"
  itemModel: "minecraft:iron_nugget"
  ammoType: rifle_round
  damageMultiplier: 1.15
  penetration: 2
```

The gun remembers which ammo item was loaded into it, so two magazines of different ammo behave differently in the same
weapon. Loading a different ammo type replaces what the gun remembers, and each pellet of a buckshot round penetrates
independently.

Reloading consumes whole items: if a box holds more rounds than the magazine has room for, the remainder is spent.
Reloading a nearly-full magazine therefore wastes part of a box, the same way a real tactical reload wastes a mag.

## Examples

Single rounds, loaded one at a time:

```yaml
shotgun_shell:
  type: AMMO
  name: "<gray>12-Gauge Shell"
  itemModel: "minecraft:firework_star"
  ammoType: shotgun_shell
  roundsPerItem: 1
```

A spare magazine that tops the gun up to full in one go:

```yaml
rifle_magazine:
  type: AMMO
  name: "<gray>AK Magazine"
  itemModel: "minecraft:iron_ingot"
  ammoType: rifle_round
  roundsPerItem: 0
```
