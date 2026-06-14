# Ammo (`type: AMMO`)

Plus the [common fields](common-fields.md). Ammo items are consumed when a gun with `consumesAmmo: true` reloads, matched by `ammoType`.

| Field | Type | Default | Required | Description |
|-------|------|---------|----------|-------------|
| `ammoType` | string | — | yes | The type tag a gun matches against (e.g. `rifle_round`). A gun pulls only ammo whose `ammoType` equals its own. |
| `roundsPerItem` | int | `1` | no | How many rounds one consumed item loads. `1` = a single round, `N` = a box of N, `0` = one item refills the **whole magazine** (a spare mag). |

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
