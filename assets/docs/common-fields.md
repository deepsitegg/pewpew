# Common Fields

These fields apply to every item type (`GUN`, `AMMO`, `ATTACHMENT`, `THROWABLE`).

| Field | Type | Default | Required | Description |
|-------|------|---------|----------|-------------|
| `type` | enum | — | yes | One of `GUN`, `AMMO`, `ATTACHMENT`, `THROWABLE`. Selects how the rest of the entry is read. |
| `name` | text | — | yes | Display name (MiniMessage). |
| `itemModel` | key | — | yes | The `minecraft:` (or resource-pack) item model, format `namespace:key`. The base item is always paper; the model controls how it looks. |
| `lore` | list of text | empty | no | Flavor lore lines (MiniMessage). Stat lines are generated automatically for guns and attachments and appended after this. |
| `customModelData` | int | `0` | no | Custom model data value; `0` leaves it unset. |
| `hideItemFlags` | bool | `false` | no | When true, hides all vanilla item flags (attributes, enchants, etc.). |
| `maxStack` | int | `0` | no | Override the max stack size (1–99). `0` keeps the material default (64). Ammo-using guns only merge into a stack when their ammo count is identical. |

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
