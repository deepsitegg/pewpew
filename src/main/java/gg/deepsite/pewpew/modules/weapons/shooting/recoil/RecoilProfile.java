package gg.deepsite.pewpew.modules.weapons.shooting.recoil;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RecoilProfile {

    @Builder.Default
    private final float smoothingFactor = 0.35f;
    @Builder.Default
    private final float damping = 0.15f;
    @Builder.Default
    private final float recovery = 0.6f;
    @Builder.Default
    private final float recoveryPercentage = 1.0f;
    @Builder.Default
    private final float recoilSpeed = 1.0f;
    @Builder.Default
    private final float maxAccum = 12.0f;
    @Builder.Default
    private final float horizontalRatio = 0.3f;

    public static final RecoilProfile DEFAULT = RecoilProfile.builder().build();
}
