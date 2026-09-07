package gg.deepsite.pewpew.api.objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record PewpewAnimation(@NotNull List<Frame> frames) {

	public record Frame(@NotNull String model, int ticks) {
	}

	public PewpewAnimation {
		frames = List.copyOf(frames);
	}

	@NotNull
	public static PewpewAnimation baked(@NotNull String base, int frameCount, int ticksPerFrame) {
		List<Frame> frames = new ArrayList<>(frameCount);
		for (int i = 1; i <= frameCount; i++) frames.add(new Frame(base + i, ticksPerFrame));
		return new PewpewAnimation(frames);
	}

	public int length() {
		int total = 0;
		for (Frame frame : frames) total += frame.ticks();
		return total;
	}

	@Nullable
	public String modelAt(int tick) {
		if (tick < 0) return null;
		int elapsed = 0;
		for (Frame frame : frames) {
			elapsed += frame.ticks();
			if (tick < elapsed) return frame.model();
		}
		return null;
	}

	@NotNull
	public PewpewAnimation scaled(double factor) {
		if (factor <= 0 || factor == 1.0) return this;
		List<Frame> scaled = new ArrayList<>(frames.size());
		for (Frame frame : frames) {
			scaled.add(new Frame(frame.model(), Math.max(1, (int) Math.round(frame.ticks() * factor))));
		}
		return new PewpewAnimation(scaled);
	}
}
