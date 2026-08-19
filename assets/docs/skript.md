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

`pewpew ammo of %players%` reads and writes the magazine of the gun in a player's main hand, clamped to the weapon's
effective capacity, and refreshes the item's lore. `reset pewpew ammo` fills the magazine.

`pewpew bloom` is the accumulated spread from sustained fire. Deleting it instantly restores full accuracy.

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
```

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
