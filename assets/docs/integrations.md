# Integrations & config

Optional hooks configured in `plugins/Pewpew/config.yml`. Each is a **no-op unless the matching plugin is installed**,
so they are safe to leave enabled. Set `enabled: false` to hard-disable a hook.

```yaml
integrations:
  worldguard:
    enabled: true
  combattagplus:
    enabled: true
  openminetopia:
    enabled: true
    block-handcuffed: true
    banned-places: [ ]
```

## WorldGuard

Requires [WorldGuard](https://dev.bukkit.org/projects/worldguard). On startup Pewpew registers a region flag **
`pewpew-guns`** (default: `allow`). Deny it in a region to block guns *and* throwables there:

```
/rg flag <region> pewpew-guns deny
```

Denied players get the `worldguard-deny` action-bar message and cannot fire (enforced per shot, so full-auto stops the
moment they enter a protected region). If WorldGuard is absent the flag is never registered and nothing is checked.

## CombatTagPlus

Requires [CombatTagPlus](https://www.spigotmc.org/resources/combattagplus.4775/). When a player lands a gun hit on
another player, both are combat-tagged. If CombatTagPlus is absent this does nothing (guns already deal real damage, so
most combat-log plugins tag on their own regardless).

## OpenMinetopia

Requires [OpenMinetopia](https://github.com/openminetopia/openminetopia). Blocks firing, throwing and opening the
attachment bench for players who are:

- **handcuffed** - action-bar message `openminetopia-handcuffed-deny`. Turn off with `block-handcuffed: false`.
- **inside a banned place** - action-bar message `openminetopia-place-deny`.

`banned-places` is a list of MTPlace names (the city, or the world name when outside a city), matched
case-insensitively:

```yaml
openminetopia:
  banned-places:
    - "Amsterdam"
    - "Rotterdam"
```

Detection gates (weapon scanners) need no configuration here - list the gun's material and `customModelData` in
OpenMinetopia's own config and it will be flagged like any other item.

If OpenMinetopia is absent nothing is checked.

## Messages

All player-facing strings live in `plugins/Pewpew/messages.yml`
([MiniMessage](https://docs.advntr.dev/minimessage/format.html), same custom tags as item text). Missing keys fall back
to the built-in defaults. Run `/pewpew reload` to apply changes.

## Skript

Pewpew registers its Skript syntax automatically whenever Skript is installed. See [skript.md](skript.md) for the full
list of events, expressions and conditions.

## Permissions

Command permissions are declared in the plugin and default to op; `pewpew.*` grants every one of them.

Each item registers `pewpew.use.<id>` when it loads, defaulting to **true** so every weapon works without setup. Negate
it (`-pewpew.use.awm`) to stop a group from shooting, throwing or attaching to that item; players who are denied get the
`no-permission` message.
