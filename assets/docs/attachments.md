# Attachments (`type: ATTACHMENT`)

Plus the [common fields](common-fields.md). An attachment fits a gun slot listed in the gun's `allowedAttachmentSlots`. Players fit them in the attachment bench GUI (swap-hands on a gun). Stat lines render automatically on the item.

Every attachment needs `attachmentType`, which selects the slot and the available modifier fields.

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `attachmentType` | enum | yes | `SCOPE`, `BARREL`, `GRIP`, or `MAGAZINE`. |

Modifiers are **multipliers** unless stated otherwise: `1.0` = no change, `1.25` = +25%, `0.75` = −25%.

## SCOPE

Aim-down-sights optics. ADS is triggered by holding sneak.

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `zoom` | double | `1.0` | Zoom strength while aiming (FOV pinch). `1.0` = none. |
| `adsSpeedModifier` | double | `1.0` | Reserved for aim-in speed tuning. |
| `aimSpreadMultiplier` | double | `0.0` | Spread multiplier while scoped. `0.0` = perfectly accurate, `1.0` = unchanged. |
| `aimRecoilMultiplier` | double | `0.0` | Recoil multiplier while scoped. `0.0` = no kick. |

## BARREL

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `damageModifier` | double | `1.0` | Multiplies base damage. |
| `rangeModifier` | double | `1.0` | Multiplies range. |

## GRIP

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `recoilModifier` | double | `1.0` | Multiplies both recoil kick and spread. `0.5` = half. |

## MAGAZINE

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `ammoBonus` | int | `0` | Rounds added to the gun's `maxAmmo`. Can be negative. |
| `reloadModifier` | double | `1.0` | Multiplies reload time. `0.6` = 40% faster, `1.4` = 40% slower. |

## Example

```yaml
extended_mag:
  type: ATTACHMENT
  name: "<gray>Extended Magazine"
  itemModel: "minecraft:iron_ingot"
  attachmentType: MAGAZINE
  ammoBonus: 10
  reloadModifier: 1.15
```
