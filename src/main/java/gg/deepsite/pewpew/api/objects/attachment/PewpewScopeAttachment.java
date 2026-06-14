package gg.deepsite.pewpew.api.objects.attachment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PewpewScopeAttachment extends PewpewAttachment {

    private double zoom;
    private double adsSpeedModifier;
    private double aimSpreadMultiplier;
    private double aimRecoilMultiplier;
}

