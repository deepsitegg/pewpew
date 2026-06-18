# Guns (`type: GUN`)

Plus the [common fields](common-fields.md). All times are in **ticks** (20 ticks = 1 second). All distances are in **blocks**.

## Core

| Field | Type | Default | Required | Description |
|-------|------|---------|----------|-------------|
| `baseDamage` | double | - | yes | Damage per shot before modifiers. For multi-pellet guns this is **per pellet**. |
| `fireRate` | int (ticks) | - | yes | Ticks between trigger pulls. Lower = faster. |
| `reloadTime` | int (ticks) | - | yes | Reload duration. With `reloadType: SINGLE` this is the delay **per round**. |
| `maxAmmo` | int | - | yes | Magazine size. `0` makes the gun infinite-ammo (never reloads, no durability bar). |
| `range` | double | - | yes | Maximum hitscan trace distance, or projectile reach. |
| `firingMode` | enum | - | yes | `HITSCAN` (instant ray) or `PROJECTILE` (thrown snowball). |
| `projectileSpeed` | double | - | only for `PROJECTILE` | Launch velocity of the projectile. |

## Ammo & reloading

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `ammoType` | string | `default` | Matches ammo items with the same `ammoType`. Only used when `consumesAmmo` is true. |
| `consumesAmmo` | bool | `false` | When true, reloads draw matching ammo items from the inventory. When false, reloads refill for free. |
| `reloadType` | enum | `MAGAZINE` | `MAGAZINE` refills in one timed action; `SINGLE` loads one round per `reloadTime` (shell-by-shell). |
| `actionOpenTime` | int (ticks) | `0` | Bolt/pump open delay added after each shot, with a sound. `0` disables the action cycle. |
| `actionCloseTime` | int (ticks) | `0` | Bolt/pump close delay before the gun is ready again, with a sound. |

## Firing behavior

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `automatic` | bool | `false` | Hold right-click to fire continuously at `fireRate`. False fires once per click. |
| `burstCount` | int | `1` | Shots fired per trigger pull, 2 ticks apart. |
| `bulletCount` | int | `1` | Pellets/projectiles per shot, each independently spread (shotgun buckshot). |
| `spread` | double (degrees) | `1.5` | Bullet cone half-angle. `0` = pinpoint. Scaled by grip and scope. |
| `recoil` | double (degrees) | `0.0` | Upward camera kick per shot, plus slight random horizontal sway. Scaled by grip and scope. |
| `bulletDrop` | double | `0.0` | For `HITSCAN`: vertical curve per block (ballistic arc). For `PROJECTILE`: any value `> 0` enables gravity. `0` flies straight. |

## Damage modifiers

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `headshotMultiplier` | double | `1.0` | Damage multiplier on a head hit. `1.0` = no bonus. |
| `critChance` | double (0–1) | `0.0` | Chance per shot to crit. |
| `critMultiplier` | double | `1.5` | Crit damage multiplier. Stacks with `headshotMultiplier`. |
| `falloffStart` | double (blocks) | `0.0` | Distance where damage starts dropping. |
| `falloffEnd` | double (blocks) | `0.0` | Distance where damage reaches the minimum. `0` (or ≤ start) disables falloff. |
| `falloffMinMultiplier` | double | `1.0` | Damage multiplier at and beyond `falloffEnd` (e.g. `0.25` = 25% damage at long range). |
| `shieldDisableTime` | int (ticks) | `0` | Hitting a blocking player disables their shield for this long (the axe effect). `0` = off. |

## Effects on hit

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `victimEffects` | list | empty | Potion effects applied to the entity hit. Each entry is `TYPE:durationTicks:amplifier` (amplifier optional, 0-based). |
| `shooterEffects` | list | empty | Potion effects applied to the shooter on a landed hit. Same format. |

Example: `"POISON:60:1"` = Poison II for 3 seconds. Effect names are vanilla, e.g. `SLOWNESS`, `SPEED`, `BLINDNESS`.

## Presentation

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `trailParticle` | particle | `CRIT` | Particle drawn along the bullet path / projectile trail. `null`/omit for none. |
| `impactParticle` | particle | none | Particle burst at the point of impact. |
| `fireSound` | sound | default | Played positionally on each shot. See [sound format](#sound-format). |
| `hitSound` | sound | default hitmarker | Played to the shooter on a landed hit. |
| `hitMessage` | text | none | Action-bar message to the shooter on hit. Placeholders `%victim%`, `%damage%`. |
| `deathMessage` | text | vanilla | Replaces the death message when this gun kills. Placeholders `%victim%`, `%killer%`, `%weapon%`. |

Use simple particle names (e.g. `FLAME`, `SMOKE`, `CRIT`, `SOUL_FIRE_FLAME`, `LARGE_SMOKE`). Particles that need extra data (`DUST`, `BLOCK`, `ITEM`) are not supported.

### Sound format

A sound is either a scalar name or a block:

```yaml
fireSound: "ENTITY_BLAZE_SHOOT"        # name, volume/pitch default to 1.0

fireSound:                              # or a block
  key: "minecraft:entity.blaze.shoot"   # vanilla key, or a custom resource-pack key
  volume: 1.0
  pitch: 1.4
```

Names without a namespace are treated as vanilla and converted (`ENTITY_BLAZE_SHOOT` → `minecraft:entity.blaze.shoot`). Anything with a `:` is used as-is, so custom resource-pack sounds work (`mypack:gun.ak.fire`).

## Attachments

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `allowedAttachmentSlots` | list of enum | empty | Which slots this gun accepts: `SCOPE`, `BARREL`, `GRIP`, `MAGAZINE`. See [attachments.md](attachments.md). |
