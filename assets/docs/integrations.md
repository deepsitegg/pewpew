# Integrations & config

Optional hooks configured in `plugins/Pewpew/config.yml`. Each is a **no-op unless the matching plugin is installed**, so they are safe to leave enabled. Set `enabled: false` to hard-disable a hook.

```yaml
integrations:
  worldguard:
    enabled: true
  combattagplus:
    enabled: true
```

## WorldGuard

Requires [WorldGuard](https://dev.bukkit.org/projects/worldguard). On startup Pewpew registers a region flag **`pewpew-guns`** (default: `allow`). Deny it in a region to block guns *and* throwables there:

```
/rg flag <region> pewpew-guns deny
```

Denied players get the `worldguard-deny` action-bar message and cannot fire (enforced per shot, so full-auto stops the moment they enter a protected region). If WorldGuard is absent the flag is never registered and nothing is checked.

## CombatTagPlus

Requires [CombatTagPlus](https://www.spigotmc.org/resources/combattagplus.4775/). When a player lands a gun hit on another player, both are combat-tagged. If CombatTagPlus is absent this does nothing (guns already deal real damage, so most combat-log plugins tag on their own regardless).

## Messages

All player-facing strings live in `plugins/Pewpew/messages.yml` ([MiniMessage](https://docs.advntr.dev/minimessage/format.html), same custom tags as item text). Missing keys fall back to the built-in defaults. Run `/pewpew reload` to apply changes.
