package gg.deepsite.pewpew.api.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import gg.deepsite.pewpew.api.enums.AttachmentType;

import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PewpewWeaponItem extends PewPewItem {

    private double baseDamage;
    private int fireRate;
    private int reloadTime;
    private String ammoType;
    private int maxAmmo;
    private boolean consumesAmmo;
    private List<AttachmentType> allowedAttachmentSlots;
}

