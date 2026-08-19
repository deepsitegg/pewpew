# Pewpew Documentation

Every item is defined in YAML under `plugins/Pewpew/items/`. Each file is a map of `id: { fields }`. After editing, run
`/pewpew reload`.

There are four item types, set by the `type` field:

| Type         | File              | Docs                             |
|--------------|-------------------|----------------------------------|
| `GUN`        | `guns.yml`        | [guns.md](guns.md)               |
| `AMMO`       | `ammo.yml`        | [ammo.md](ammo.md)               |
| `ATTACHMENT` | `attachments.yml` | [attachments.md](attachments.md) |
| `THROWABLE`  | `throwables.yml`  | [throwables.md](throwables.md)   |

All types share a set of [common fields](common-fields.md).

Optional plugin hooks (WorldGuard, CombatTagPlus, OpenMinetopia) and the `messages.yml` file are documented
in [integrations.md](integrations.md). Skript events, expressions and conditions are in [skript.md](skript.md).

## Bundled files

The four bundled item files are written only when `plugins/Pewpew/items/` does not exist, so updates never overwrite or
add to your own files. Add as many extra `.yml` files as you like: every `.yml` in the folder is loaded. To get the
defaults back, delete the whole `items/` folder.

## Notes

- **Unknown fields are reported.** Anything Pewpew does not recognise at the top level of an item is logged as a warning
  naming the item, the file and the field, so a typo like `spred` or `bulletcount` shows up in console instead of
  silently doing nothing. The item still loads; only the misspelled field is ignored.

- **Text** (`name`, `lore`, messages) uses [MiniMessage](https://docs.advntr.dev/minimessage/format.html). Pewpew adds
  the tags `<color>`, `<primary>`, `<success>`, `<warning>`, `<error>`.
- **Item id** is the YAML key; it is what `/pewpew give <id>` expects and must be unique across all files.
- Invalid or unknown values are skipped with a console warning rather than crashing the load.
