# Skript

Pewpew registers Skript syntax automatically when Skript is installed. Nothing to enable.

## Events

| Event                            | When                                                                     | Cancellable |
|----------------------------------|--------------------------------------------------------------------------|-------------|
| `on pewpew shoot`                | A player fires a gun, once per shot                                      | yes         |
| `on pewpew hit`                  | A shot lands on a living entity                                          | yes         |
| `on pewpew hit block`            | A shot stops on a block instead of an entity                             | no          |
| `on pewpew kill`                 | A player dies to a gun                                                   | no          |
| `on pewpew reload`               | A reload starts                                                          | yes         |
| `on pewpew reload complete`      | A reload finishes and rounds are in the magazine                         | no          |
| `on pewpew scope`                | A player scopes in or out                                                | yes         |
| `on pewpew attachment`           | An attachment is fitted or removed in the bench                          | yes         |
| `on pewpew throw`                | A player throws a throwable                                              | yes         |
| `on pewpew detonate`             | A throwable or launcher payload detonates                                | yes         |
| `on pewpew gun explode`          | An explosive gun projectile detonates                                    | yes         |

Standard event values work where they make sense: `event-player` is the shooter (the killer in a kill event),
`event-entity` the target, `event-block` the block hit, `event-location` the impact or blast point.

## Expressions

| Expression                   | Type   | Where                                 | Settable                       |
|------------------------------|--------|---------------------------------------|--------------------------------|
| `pewpew damage`              | number | hit, gun explode                      | set / add / remove / delete    |
| `pewpew victim`              | entity | hit, kill                             | no                             |
| `pewpew item id`             | text   | every pewpew event                    | no                             |
| `pewpew distance`            | number | hit, hit block                        | no                             |
| `pewpew death message`       | text   | kill                                  | set / delete                   |
| `pewpew attachment id`       | text   | attachment                            | no                             |
| `pewpew attachment slot`     | text   | attachment                            | no                             |
| `pewpew ammo`                | number | reload complete, or `of %players%`    | set / add / remove / reset     |
| `pewpew bloom of %players%`  | number | anywhere                              | delete / reset                 |
| `pewpew magazine of %players%` | text | anywhere (needs `advanced.magazines`) | no                           |
| `pewpew magazine rounds of %players%` | number | anywhere                     | no                             |
| `pewpew chamber of %players%` | number | anywhere                              | no                             |

`pewpew ammo of %players%` reads and writes the magazine of the gun in a player's main hand, clamped to the weapon's
effective capacity, and refreshes the item's lore. `reset pewpew ammo` fills the magazine.

`pewpew bloom` is the accumulated spread from sustained fire. Deleting it instantly restores full accuracy.

`pewpew magazine` is the id of the magazine item inserted in the gun in the player's main hand, or nothing when the gun
is empty or [magazines](magazines.md) are off. `pewpew magazine rounds` is what that magazine still holds and
`pewpew chamber` is `1` or `0` for the round on top of it; `pewpew ammo` is the two added together.

## Conditions

```
%players% is scoped in
%players% is reloading
%itemstacks% is a pewpew item        # or gun, throwable, attachment, ammo
pewpew is scoping in                 # inside a pewpew scope event
the pewpew hit was a headshot        # inside a pewpew hit event
the pewpew hit was a critical
```

## Effects

```
give 2 pewpew items "ak47" to player
force player to reload their pewpew gun
eject the pewpew magazine from player
play pewpew animation "reload" for player
play pewpew animation "reload" for player over 40 ticks
play pewpew sound "magazine.swap-start" to player
play pewpew sound "explosion.blast" at location of player
```

`eject the pewpew magazine` drops the inserted magazine back into the inventory with its rounds, exactly like sneak +
the reload key. It does nothing while the player is reloading, holds no gun, or has no magazine inserted.

Animation names are the [animation](guns.md#animations) events: `fire`, `reload`, `reload-round`, `scope-in`,
`scope-out`. The gun in the main hand supplies the frames (or its [rig](guns.md#rig)), so a gun without that animation
plays nothing. `over %number% ticks` stretches the animation to that length, the way a reload animation is stretched to
the weapon's real reload time.

Sound names are the keys in `sounds.yml` (`gun.fire`, `reload.magazine-start`, `magazine.swap-finish`, `hit.marker`,
`explosion.blast`, ...), so scripts play whatever the server configured for that event rather than a hardcoded sound.
See [integrations.md](integrations.md#sounds).

## Examples

Double damage on headshots, and announce long-range kills:

```
on pewpew hit:
    if the pewpew hit was a headshot:
        set pewpew damage to pewpew damage * 2

on pewpew kill:
    set pewpew death message to "<red>%event-player% sniped %pewpew victim%"
```

Punish hip-firing by clearing the magazine of anyone who sprays without scoping:

```
on pewpew shoot:
    if player is not scoped in:
        if pewpew bloom of player > 2:
            send action bar "<red>steady your aim" to player
```

Block a weapon behind a permission and refill on reload:

```
on pewpew shoot:
    if pewpew item id is "awm":
        if player does not have permission "guns.sniper":
            cancel event

on pewpew reload complete:
    send action bar "%pewpew ammo% rounds" to player
```

Stop players removing a scope once it is fitted:

```
on pewpew attachment:
    if pewpew attachment slot is "SCOPE":
        cancel event
```

Take a player's magazine away when they leave a safezone, with the sound the server configured for it:

```
on region leave:
    if pewpew magazine of player is set:
        eject the pewpew magazine from player
        play pewpew sound "magazine.swap-start" to player
        send action bar "<red>magazine confiscated" to player
```

Warn on a nearly empty magazine, and stretch the gun's own reload animation from a script:

```
on pewpew shoot:
    if pewpew magazine rounds of player <= 3:
        send action bar "<gold>%pewpew magazine rounds of player% left" to player

on pewpew reload:
    play pewpew animation "reload" for player over 40 ticks
```
