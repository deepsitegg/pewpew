package gg.deepsite.pewpew.api.objects;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SpreadModifiers {

	@Builder.Default
	private final double sprinting = 1.0;
	@Builder.Default
	private final double walking = 1.0;
	@Builder.Default
	private final double sneaking = 1.0;
	@Builder.Default
	private final double standing = 1.0;
	@Builder.Default
	private final double midair = 1.0;
	@Builder.Default
	private final double inWater = 1.0;

	public static final SpreadModifiers DEFAULT = SpreadModifiers.builder().build();
}
