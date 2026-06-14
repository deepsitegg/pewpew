# Pewpew Documentation

Every item is defined in YAML under `plugins/Pewpew/items/`. Each file is a map of `id: { fields }`.
After editing, run `/pewpew reload`.

There are four item types, set by the `type` field:

| Type | File | Docs |
|------|------|------|
| `GUN` | `guns.yml` | [guns.md](guns.md) |
| `AMMO` | `ammo.yml` | [ammo.md](ammo.md) |
| `ATTACHMENT` | `attachments.yml` | [attachments.md](attachments.md) |
| `THROWABLE` | `throwables.yml` | [throwables.md](throwables.md) |

All types share a set of [common fields](common-fields.md).

## Notes

- **Text** (`name`, `lore`, messages) uses [MiniMessage](https://docs.advntr.dev/minimessage/format.html). Pewpew adds the tags `<color>`, `<primary>`, `<success>`, `<warning>`, `<error>`.
- **Item id** is the YAML key; it is what `/pewpew give <id>` expects and must be unique across all files.
- Invalid or unknown values are skipped with a console warning rather than crashing the load.
