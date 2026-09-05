# Magazines (`type: MAGAZINE`)

Plus the [common fields](common-fields.md).

Magazines are off by default. Turn them on first:

```yaml
advanced:
  magazines: true
```

While off, `MAGAZINE` items are still loaded and can be given, but nothing uses them: ammo loads straight into the gun
exactly as described in [ammo.md](ammo.md).

## What changes when it is on

A magazine is a real item that holds rounds of its own. For guns with `consumesAmmo: true`:

- **`capacity` replaces the gun's `maxAmmo`** while a magazine is inserted. A gun with no magazine in it holds nothing.
- **Reloading swaps magazines.** Pewpew takes the fullest matching magazine from the inventory, inserts it, and ejects
  the one that was in the gun back into the inventory with its remaining rounds intact. Nothing is thrown away, and a
  half-spent magazine can be topped up and used again later.
- **Every gun gains a chamber** holding one round on top of the magazine, so a 30-round magazine gives 31 shots. The
  chambered round stays with the gun across a magazine swap: reload with a round chambered and you keep it.
- **`reloadType` is ignored** while a swap is possible, because swapping a magazine is one action. A gun with no
  magazine available falls back to its `reloadType` and loads loose ammo into the chamber.

The `MAGAZINE` **attachment** (`ammoBonus`, `reloadModifier`, see [attachments.md](attachments.md)) still applies on
top: `ammoBonus` adds to the inserted magazine's capacity and both `reloadModifier`s multiply together.

## Fields

| Field            | Type   | Default | Required | Description                                                                            |
|------------------|--------|---------|----------|----------------------------------------------------------------------------------------|
| `ammoType`       | string | -       | yes      | The type tag this magazine holds. Must match the gun's `ammoType` and the ammo's.       |
| `capacity`       | int    | -       | yes      | How many rounds it holds. Replaces the gun's `maxAmmo` while inserted. Minimum `1`.    |
| `reloadModifier` | double | `1.0`   | no       | Multiplies reload time while this magazine is being swapped in. `1.4` = 40% slower.    |

## Filling a magazine

Pick up an ammo item in your inventory and click it onto a magazine. The magazine takes as many items as it has room
for; a box of ammo (`roundsPerItem: N`) is consumed whole, so topping up a nearly-full magazine wastes the remainder of
the last box, the same as a tactical reload.

A magazine holds one kind of ammo at a time. Loading different ammo into a magazine that still has rounds in it is
refused; empty it first by firing it dry.

Magazines given with `/pewpew give` come out **empty**. Set `maxStack: 1` so partially-filled magazines stay distinct
items rather than trying to stack.

## Ammo stats

With [ammo stats](ammo.md#ammo-stats) on, a magazine remembers which ammo went into it and hands that to the gun when
it is inserted. Two magazines loaded with different rounds therefore behave differently in the same weapon, and
swapping magazines swaps the gun's ballistics with them.

## Example

```yaml
rifle_556_mag:
  type: MAGAZINE
  name: "<gray>STANAG Magazine"
  itemModel: "minecraft:iron_ingot"
  maxStack: 1
  ammoType: rifle_556
  capacity: 30

rifle_556_drum:
  type: MAGAZINE
  name: "<gray>5.56 Drum"
  itemModel: "minecraft:iron_block"
  maxStack: 1
  ammoType: rifle_556
  capacity: 60
  reloadModifier: 1.4
```
