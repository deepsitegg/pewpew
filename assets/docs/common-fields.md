# Common Fields

These fields apply to every item type (`GUN`, `AMMO`, `ATTACHMENT`, `THROWABLE`).

| Field             | Type         | Default | Required | Description                                                                                                                             |
|-------------------|--------------|---------|----------|-----------------------------------------------------------------------------------------------------------------------------------------|
| `type`            | enum         | -       | yes      | One of `GUN`, `AMMO`, `ATTACHMENT`, `THROWABLE`. Selects how the rest of the entry is read.                                             |
| `name`            | text         | -       | yes      | Display name (MiniMessage).                                                                                                             |
| `itemModel`       | key          | -       | yes      | The `minecraft:` (or resource-pack) item model, format `namespace:key`. The base item is always paper; the model controls how it looks. |
| `lore`            | list of text | empty   | no       | Flavor lore lines (MiniMessage). Stat lines are generated automatically for guns and attachments and appended after this.               |
| `customModelData` | int          | `0`     | no       | Custom model data value; `0` leaves it unset.                                                                                           |
| `hideItemFlags`   | bool         | `false` | no       | When true, hides all vanilla item flags (attributes, enchants, etc.).                                                                   |
| `maxStack`        | int          | `0`     | no       | Override the max stack size (1–99). `0` keeps the material default (64).                                                                |
| `extends`         | item id      | -       | no       | Copy every field from another item, then apply this entry's own fields on top. Needs `advanced.extends`.                                |
| `abstract`        | bool         | `false` | no       | Make this entry a template only: never registered, never given. Needs `advanced.abstract`.                                             |

## Inheriting from another item

Both fields below are off until you turn them on in `config.yml`:

```yaml
advanced:
  extends: true
  abstract: true
```

While a feature is off, an item using it loads as if the field were not there, and Pewpew logs a warning telling you
which option to enable, it never fails silently.

`extends` lets one entry reuse another instead of repeating it. Anything the child sets wins; anything it leaves out is
taken from the parent. The parent can live in any file in `items/`, so your own file can build on a bundled weapon, and
`extends` chains as deep as you like.

```yaml
base_rifle:
  type: GUN
  maxStack: 1
  hideItemFlags: true
  firingMode: HITSCAN
  reloadType: MAGAZINE
  ammoType: rifle_round
  baseDamage: 7.0
  spread: 2.0

ak47:
  extends: base_rifle
  name: "<red>AK-47"
  itemModel: "minecraft:iron_horse_armor"
  maxAmmo: 30
  fireRate: 2.0

m4:
  extends: base_rifle
  name: "<gray>M4"
  itemModel: "minecraft:iron_horse_armor"
  maxAmmo: 30
  fireRate: 1.5
  spread: 1.5
```

### Template-only entries

By default a template like `base_rifle` is still a real item: it registers, shows up in `/pewpew list`, and warns if it
has no `name` or `itemModel`. Mark it `abstract: true` to make it a template and nothing else:

```yaml
base_rifle:
  abstract: true
  type: GUN
  maxStack: 1
  ammoType: rifle_round
  baseDamage: 7.0
```

An abstract entry is skipped entirely at load, so it needs no `name` and no `itemModel`. `abstract` is never inherited:
guns extending an abstract template are normal, registered items.

If `extends` names an item that does not exist, or two entries extend each other in a loop, Pewpew logs a warning and
loads the item without inheritance rather than failing.

## Example

```yaml
my_item:
  type: AMMO
  name: "<gray>Example"
  itemModel: "minecraft:iron_nugget"
  lore:
    - "<dark_gray>Some flavor text"
  customModelData: 4099
  maxStack: 16
```
