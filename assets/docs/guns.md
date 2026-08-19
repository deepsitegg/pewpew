# Guns (`type: GUN`)

Plus the [common fields](common-fields.md). All times are in **ticks** (20 ticks = 1 second). All distances are in
**blocks**.

## Core

| Field             | Type        | Default             | Required              | Description                                                                                                                                                                                                                               |
|-------------------|-------------|---------------------|-----------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `baseDamage`      | double      | -                   | yes                   | Damage per shot before modifiers. For multi-pellet guns this is **per pellet**.                                                                                                                                                           |
| `fireRate`        | double (ticks) | -                | yes                   | Ticks between trigger pulls, decimals allowed (`1200 / rpm`, e.g. `1.33` ≈ 900 RPM). Lower = faster.                                                                                                                                      |
| `reloadTime`      | int (ticks) | -                   | yes                   | Reload duration. With `reloadType: SINGLE` this is the delay **per round**.                                                                                                                                                               |
| `maxAmmo`         | int         | -                   | yes                   | Magazine size. `0` makes the gun infinite-ammo (never reloads, no durability bar).                                                                                                                                                        |
| `range`           | double      | -                   | yes                   | Maximum hitscan trace distance, or projectile reach.                                                                                                                                                                                      |
| `firingMode`      | enum        | -                   | yes                   | `HITSCAN` (instant ray) or `PROJECTILE` (thrown snowball).                                                                                                                                                                                |
| `projectileSpeed` | double      | -                   | only for `PROJECTILE` | Launch velocity of the projectile.                                                                                                                                                                                                        |
| `projectileModel` | material    | snowball            | no                    | Item shown for the projectile in flight (e.g. `minecraft:fire_charge`).                                                                                                                                                                   |
| `trajectory`      | enum        | (uses `bulletDrop`) | no                    | `FLAT` (no gravity) or `ARC` (lobbed). Overrides `bulletDrop` gravity for `PROJECTILE`.                                                                                                                                                   |
| `payload`         | string      | -                   | no                    | Id of a throwable. The projectile flies as that throwable's model; its **fuse starts when fired** and it detonates with the throwable's **effect** when the fuse runs out — wherever it is (airburst or after landing). Grenade launcher. |

## Explosive projectiles

Add an `explosive:` block to a `PROJECTILE` gun to detonate on impact (rocket launcher). Mutually exclusive with
`payload` — `payload` reuses a throwable's effect instead.

| Field                   | Type        | Default | Description                                                                  |
|-------------------------|-------------|---------|------------------------------------------------------------------------------|
| `blastRadius`           | double      | `4.0`   | Explosion radius; entity damage and knockback fall off linearly to the edge. |
| `explosionDamage`       | double      | `12.0`  | Damage at the center.                                                        |
| `explosionKnockback`    | double      | `1.2`   | Knockback at the center.                                                     |
| `damageBlocks`          | bool        | `false` | Destroy breakable blocks in the radius (no drops).                           |
| `rebuild.enabled`       | bool        | `false` | Restore destroyed blocks after the blast.                                    |
| `rebuild.delay`         | int (ticks) | `100`   | Delay before regen starts.                                                   |
| `rebuild.blocksPerTick` | int         | `2`     | Regen speed.                                                                 |

## Ammo & reloading

| Field             | Type        | Default    | Description                                                                                          |
|-------------------|-------------|------------|------------------------------------------------------------------------------------------------------|
| `ammoType`        | string      | `default`  | Matches ammo items with the same `ammoType`. Only used when `consumesAmmo` is true.                  |
| `consumesAmmo`    | bool        | `false`    | When true, reloads draw matching ammo items from the inventory. When false, reloads refill for free. |
| `reloadType`      | enum        | `MAGAZINE` | `MAGAZINE` refills in one timed action; `SINGLE` loads one round per `reloadTime` (shell-by-shell).  |
| `actionOpenTime`  | int (ticks) | `0`        | Bolt/pump open delay added after each shot, with a sound. `0` disables the action cycle.             |
| `actionCloseTime` | int (ticks) | `0`        | Bolt/pump close delay before the gun is ready again, with a sound.                                   |

## Firing behavior

| Field           | Type             | Default | Description                                                                                                                     |
|-----------------|------------------|---------|---------------------------------------------------------------------------------------------------------------------------------|
| `automatic`     | bool             | `false` | Hold right-click to fire continuously at `fireRate`. False fires once per click.                                                |
| `burstCount`    | int              | `1`     | Shots fired per trigger pull.                                                                                                   |
| `burstDelay`    | int (ticks)      | `2`     | Ticks between the shots of a burst. Only used when `burstCount` is above 1.                                                     |
| `bulletCount`   | int              | `1`     | Pellets/projectiles per shot, each independently spread (shotgun buckshot).                                                     |
| `spread`        | double (degrees) | `1.5`   | Bullet cone half-angle. `0` = pinpoint. Scaled by grip and scope.                                                               |
| `recoil`        | double (degrees) | `0.0`   | Camera kick strength per shot. Scaled by grip and scope, then shaped by `recoilProfile`.                                        |
| `knockback`     | double           | `0.0`   | Extra knockback pushed onto the victim on a landed hit, away from the shooter. `0` = vanilla only.                              |
| `selfKnockback` | double           | `0.0`   | Recoil shove on the shooter (backward), for hand-cannon feel. `0` = none.                                                       |
| `bulletDrop`    | double           | `0.0`   | For `HITSCAN`: vertical curve per block (ballistic arc). For `PROJECTILE`: any value `> 0` enables gravity. `0` flies straight. |

## Spread by movement, and bloom

By default a gun's `spread` is the same whether you are standing still, sprinting or falling. Two optional blocks change
that. Both are off unless configured, so leaving them out keeps the old behaviour exactly.

`spreadModifiers` multiplies `spread` by the shooter's state. The movement states are exclusive and checked in the order
sprinting, sneaking, walking, standing, so only one of them applies. `midair` and `inWater` then multiply on top of that.

```yaml
spread: 2.0
spreadModifiers:
  sprinting: 2.5
  walking: 1.4
  sneaking: 0.6
  standing: 1.0
  midair: 3.0
  inWater: 1.5
```

| Field       | Type   | Default | Description                                                            |
|-------------|--------|---------|------------------------------------------------------------------------|
| `sprinting` | double | `1.0`   | Applied while sprinting.                                               |
| `sneaking`  | double | `1.0`   | Applied while sneaking, and beats `walking`.                           |
| `walking`   | double | `1.0`   | Applied while moving on foot without sprinting or sneaking.            |
| `standing`  | double | `1.0`   | Applied while not moving.                                              |
| `midair`    | double | `1.0`   | Multiplied in whenever the shooter is off the ground.                  |
| `inWater`   | double | `1.0`   | Multiplied in whenever the shooter is in water.                        |

**Bloom** widens the cone as you keep firing and closes it again once you stop, so holding the trigger costs accuracy.

```yaml
bloomPerShot: 0.15
bloomMax: 2.0
bloomDecay: 0.08
```

| Field          | Type                     | Default | Description                                                                          |
|----------------|--------------------------|---------|--------------------------------------------------------------------------------------|
| `bloomPerShot` | double (degrees)         | `0.0`   | Extra spread added by every shot. `0` disables bloom.                                |
| `bloomMax`     | double (degrees)         | `0.0`   | Ceiling on accumulated bloom. `0` = no ceiling.                                      |
| `bloomDecay`   | double (degrees / tick)  | `0.0`   | How fast bloom shrinks while not firing. `0` means it never recovers, so set it.     |

Bloom is added to `spread` after the state multipliers, so `spreadModifiers` scales the weapon's base accuracy while
bloom is a flat penalty for sustained fire.

## Recoil profile

Optional `recoilProfile` map per gun. Every key is optional; omitting the whole block keeps the defaults below, which
reproduce the old behavior. Angles are expressed as a fraction of `recoil` (after grip/scope scaling), so `recoil` stays
the single strength knob.

```yaml
recoil: 1.2
recoilProfile:
  horizontalMean: 0.25
  horizontalVariance: 0.15
  verticalVariance: 0.2
```

| Field                | Type                  | Default | Description                                                                                                                                                                                                                |
|----------------------|-----------------------|---------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `verticalMean`       | double                | `1.0`   | Upward kick as a fraction of `recoil`. `1.0` = the full value.                                                                                                                                                             |
| `verticalVariance`   | double                | `0.0`   | Random ± around `verticalMean`, as a fraction of `recoil`. `0` = every shot kicks identically.                                                                                                                             |
| `horizontalMean`     | double                | `0.0`   | Constant sideways drift per shot. Positive = right, negative = left. `0` = no bias.                                                                                                                                        |
| `horizontalVariance` | double                | `0.3`   | Random ± sideways sway. Set to `0` together with `horizontalMean` for pure vertical recoil.                                                                                                                                |
| `smoothing`          | double (0.01–1)       | `0.35`  | How fast the camera chases the accumulated kick. `1.0` = instant snap, lower = softer climb.                                                                                                                               |
| `damping`            | double (0–1)          | `0.15`  | Per-tick decay of the accumulated kick. Higher = the climb dies out sooner during sustained fire.                                                                                                                          |
| `recovery`           | double (degrees/tick) | `0.6`   | How fast the camera returns to where you were aiming. `0` = no recovery pass, the kick only fades out through `damping`.                                                                                                   |
| `recoveryPenalty`    | double (0–1)          | `0.0`   | Share of every kick the camera never gives back, as a percentage. `0.0` = full recovery, `0.25` = a quarter of each shot's kick sticks and your aim walks up over a burst, `1.0` = nothing recovers. Needs `recovery > 0`. |
| `speed`              | double                | `1.0`   | Multiplier on the per-tick rotation actually applied. `<1` = the whole animation plays slower.                                                                                                                             |
| `maxAccumulation`    | double (degrees)      | `12.0`  | Ceiling on total accumulated kick, so full-auto cannot walk your camera into the sky. `0` = no ceiling.                                                                                                                    |
| `pattern`            | list of `[x, y]`      | -       | A fixed spray pattern. See below. When set, it replaces `verticalMean`/`verticalVariance`/`horizontalMean`/`horizontalVariance`.                                                                                          |
| `patternLoop`        | bool                  | `false` | What happens when a magazine outlasts the pattern. `false` = keep repeating the last step, `true` = start over from the first.                                                                                            |
| `patternReset`       | int (ticks)           | `20`    | How long the player must stop firing before the pattern restarts from shot one. `20` = one second.                                                                                                                        |

### Spray patterns

By default recoil is statistical: every shot rolls its own kick, so a burst is never quite the same twice. A `pattern`
makes it deterministic instead, the way an AK sprays the same shape every time in a competitive shooter. Players can
learn it and pull against it, which turns recoil control into a skill rather than a dice roll.

Each entry is `[horizontal, vertical]`, as a fraction of `recoil`. Positive horizontal is right, positive vertical is up.
Shot one uses the first entry, shot two the second, and so on.

```yaml
recoil: 1.2
recoilProfile:
  smoothing: 0.5
  recovery: 0.4
  pattern:
    - [ 0.0, 1.0 ]   # first shots climb straight up
    - [ 0.0, 1.1 ]
    - [ 0.1, 1.2 ]
    - [ 0.3, 1.0 ]   # then pull right
    - [ 0.4, 0.8 ]
    - [ 0.2, 0.6 ]
    - [ -0.2, 0.5 ]  # and drift back left
    - [ -0.4, 0.4 ]
```

The counter resets after `patternReset` ticks without firing, so tapping restarts the spray while holding the trigger
walks it. `damping`, `recovery`, `smoothing` and `maxAccumulation` all still apply on top - the pattern decides the
direction of each kick, the rest decides how the camera moves.

## Damage modifiers

| Field                  | Type            | Default | Description                                                                                |
|------------------------|-----------------|---------|--------------------------------------------------------------------------------------------|
| `headshotMultiplier`   | double          | `1.0`   | Damage multiplier on a head hit. `1.0` = no bonus.                                         |
| `critChance`           | double (0–1)    | `0.0`   | Chance per shot to crit.                                                                   |
| `critMultiplier`       | double          | `1.5`   | Crit damage multiplier. Stacks with `headshotMultiplier`.                                  |
| `falloffStart`         | double (blocks) | `0.0`   | Distance where damage starts dropping.                                                     |
| `falloffEnd`           | double (blocks) | `0.0`   | Distance where damage reaches the minimum. `0` (or ≤ start) disables falloff.              |
| `falloffMinMultiplier` | double          | `1.0`   | Damage multiplier at and beyond `falloffEnd` (e.g. `0.25` = 25% damage at long range).     |
| `shieldDisableTime`    | int (ticks)     | `0`     | Hitting a blocking player disables their shield for this long (the axe effect). `0` = off. |
| `damageType`           | damage type     | `minecraft:arrow` | The Minecraft damage type this gun deals.                                        |

### `damageType`

Damage is applied *after* falloff, headshot and crit multipliers. The damage type decides what Minecraft then does with
that number.

- `minecraft:arrow` (the default) makes the shot behave like an arrow: armor, Protection and Projectile Protection scale
  the damage down, exactly like a bow.
- `minecraft:generic` ignores armor and Protection, so the damage you configure is the damage the target takes. Use this
  when you want your configured numbers to be exact and armor to be irrelevant to guns.

Any damage type from the game registry works. Unknown values fall back to `minecraft:arrow` with a console warning.

## Effects on hit

| Field            | Type | Default | Description                                                                                                           |
|------------------|------|---------|-----------------------------------------------------------------------------------------------------------------------|
| `victimEffects`  | list | empty   | Potion effects applied to the entity hit. Each entry is `TYPE:durationTicks:amplifier` (amplifier optional, 0-based). |
| `shooterEffects` | list | empty   | Potion effects applied to the shooter on a landed hit. Same format.                                                   |

Example: `"POISON:60:1"` = Poison II for 3 seconds. Effect names are vanilla, e.g. `SLOWNESS`, `SPEED`, `BLINDNESS`.

## Presentation

| Field            | Type     | Default           | Description                                                                                      |
|------------------|----------|-------------------|--------------------------------------------------------------------------------------------------|
| `trailParticle`  | particle | `CRIT`            | Particle drawn along the bullet path / projectile trail. `null`/omit for none.                   |
| `impactParticle` | particle | none              | Particle burst at the point of impact.                                                           |
| `fireSound`      | sound    | default           | Played positionally on each shot. See [sound format](#sound-format).                             |
| `hitSound`       | sound    | default hitmarker | Played to the shooter on a landed hit.                                                           |
| `hitMessage`     | text     | none              | Action-bar message to the shooter on hit. Placeholders `%victim%`, `%damage%`.                   |
| `deathMessage`   | text     | vanilla           | Replaces the death message when this gun kills. Placeholders `%victim%`, `%killer%`, `%weapon%`. |

Use simple particle names (e.g. `FLAME`, `SMOKE`, `CRIT`, `SOUL_FIRE_FLAME`, `LARGE_SMOKE`). Particles that need extra
data (`DUST`, `BLOCK`, `ITEM`) are not supported.

### Sound format

A sound is either a scalar name or a block:

```yaml
fireSound: "ENTITY_BLAZE_SHOOT"        # name, volume/pitch default to 1.0

fireSound: # or a block
  key: "minecraft:entity.blaze.shoot"   # vanilla key, or a custom resource-pack key
  volume: 1.0
  pitch: 1.4

fireSound: # or a list, all played together (layering)
  - key: "minecraft:entity.blaze.shoot"
    volume: 1.0
    pitch: 1.4
  - "minecraft:entity.generic.explode"
```

Names without a namespace are treated as vanilla and converted (`ENTITY_BLAZE_SHOOT` → `minecraft:entity.blaze.shoot`).
Anything with a `:` is used as-is, so custom resource-pack sounds work (`mypack:gun.ak.fire`).

## Attachments

| Field                    | Type         | Default | Description                                                                                                |
|--------------------------|--------------|---------|------------------------------------------------------------------------------------------------------------|
| `allowedAttachmentSlots` | list of enum | empty   | Which slots this gun accepts: `SCOPE`, `BARREL`, `GRIP`, `MAGAZINE`. See [attachments.md](attachments.md). |
