# Throwables (`type: THROWABLE`)

Plus the [common fields](common-fields.md). Thrown with right-click; detonate after a fuse.

| Field | Type | Default | Required | Description |
|-------|------|---------|----------|-------------|
| `effect` | enum | - | yes | What happens on detonation. One of `EXPLOSION`, `SMOKE`, `FLASH`, `POISON`, `FIRE`. |
| `fuseTime` | int (ticks) | `0` | no | Delay between throw and detonation (20 ticks = 1 second). |
| `blastRadius` | double (blocks) | `0.0` | no | Effect radius at the point of detonation. |
| `throwForce` | double | `0.0` | no | Launch velocity when thrown. |

## Effects

| Effect | Description |
|--------|-------------|
| `EXPLOSION` | Damaging blast within `blastRadius`. |
| `SMOKE` | Smoke cloud for cover. |
| `FLASH` | Blinds players who can see the flash. |
| `POISON` | Lingering poison cloud (area denial). |
| `FIRE` | Incendiary - sets the area alight. |

## Example

```yaml
frag_grenade:
  type: THROWABLE
  name: "<gray>Frag Grenade"
  itemModel: "minecraft:iron_nugget"
  effect: EXPLOSION
  fuseTime: 60
  blastRadius: 5.0
  throwForce: 1.8
```
