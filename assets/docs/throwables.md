# Throwables (`type: THROWABLE`)

Plus the [common fields](common-fields.md). Thrown with right-click; detonate after a fuse.

| Field | Type | Default | Required | Description |
|-------|------|---------|----------|-------------|
| `effect` | enum | - | yes | What happens on detonation. One of `EXPLOSION`, `SMOKE`, `FLASH`, `POISON`, `FIRE`. |
| `fuseTime` | int (ticks) | `0` | no | Delay between throw and detonation (20 ticks = 1 second). |
| `blastRadius` | double (blocks) | `0.0` | no | Effect radius at the point of detonation. |
| `throwForce` | double | `0.0` | no | Launch velocity when thrown. |

### Effect tuning (all optional)

These override the built-in defaults per effect. Omit any to keep the default. Duration/amplifier use `-1` to mean "use the built-in default", so setting `0` is a real value (level-1 amplifier / instant), not "unset".

| Field | Type | Default | Applies to | Description |
|-------|------|---------|------------|-------------|
| `explosionDamage` | double | `12.0` | EXPLOSION | Max damage at the center of the blast (scales down to the edge of `blastRadius`). |
| `explosionKnockback` | double | `1.2` | EXPLOSION | Outward knockback strength at the center. |
| `effectDuration` | int (ticks) | built-in | SMOKE / POISON / FLASH | Cloud lifetime (SMOKE 200, POISON 140) or flash blindness length (FLASH 100; nausea = 1.4×). |
| `effectAmplifier` | int | built-in | SMOKE / POISON / FLASH | Potion level of the applied effect (POISON default 1, others 0). |
| `fireTicks` | int (ticks) | `80` | FIRE | How long entities in the radius burn. |

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
