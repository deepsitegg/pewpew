package gg.deepsite.pewpew.api.objects;

import lombok.Builder;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Getter
@Builder
public class RecoilProfile {

	@Builder.Default
	private final float verticalMean = 1.0f;
	@Builder.Default
	private final float verticalVariance = 0.0f;
	@Builder.Default
	private final float horizontalMean = 0.0f;
	@Builder.Default
	private final float horizontalVariance = 0.3f;
	@Builder.Default
	private final float smoothing = 0.35f;
	@Builder.Default
	private final float damping = 0.15f;
	@Builder.Default
	private final float recovery = 0.6f;
	@Builder.Default
	private final float recoveryPenalty = 0.0f;
	@Builder.Default
	private final float speed = 1.0f;
	@Builder.Default
	private final float maxAccumulation = 12.0f;

	@Nullable
	private final List<float[]> pattern;
	@Builder.Default
	private final boolean patternLoop = false;
	@Builder.Default
	private final int patternReset = 20;

	public boolean hasPattern() {
		return pattern != null && !pattern.isEmpty();
	}

	public static final RecoilProfile DEFAULT = RecoilProfile.builder().build();
}
