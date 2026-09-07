package gg.deepsite.pewpew.api.objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record PewpewRig(@NotNull List<Part> parts, boolean viewmodel, @NotNull String billboard) {

	public record Keyframe(int tick, float[] translation, float[] rotation, float[] scale) {

		public static final float[] ZERO = {0f, 0f, 0f};
		public static final float[] ONE = {1f, 1f, 1f};

		public Keyframe {
			translation = translation == null ? ZERO.clone() : translation;
			rotation = rotation == null ? ZERO.clone() : rotation;
			scale = scale == null ? ONE.clone() : scale;
		}
	}

	public record Part(@NotNull String id, @NotNull String itemModel, @NotNull List<Keyframe> keyframes) {

		public Part {
			keyframes = List.copyOf(keyframes);
		}

		public int length() {
			int last = 0;
			for (Keyframe frame : keyframes) last = Math.max(last, frame.tick());
			return last;
		}

		@Nullable
		public Keyframe at(int tick) {
			for (Keyframe frame : keyframes) {
				if (frame.tick() == tick) return frame;
			}
			return null;
		}

		@Nullable
		public Keyframe after(int tick) {
			Keyframe best = null;
			for (Keyframe frame : keyframes) {
				if (frame.tick() > tick && (best == null || frame.tick() < best.tick())) best = frame;
			}
			return best;
		}

		public int durationAfter(int tick) {
			int next = Integer.MAX_VALUE;
			for (Keyframe frame : keyframes) {
				if (frame.tick() > tick && frame.tick() < next) next = frame.tick();
			}
			return next == Integer.MAX_VALUE ? 0 : next - tick;
		}
	}

	public PewpewRig {
		parts = List.copyOf(parts);
	}

	public int length() {
		int last = 0;
		for (Part part : parts) last = Math.max(last, part.length());
		return last;
	}

	@NotNull
	public PewpewRig scaled(double factor) {
		if (factor <= 0 || factor == 1.0) return this;
		List<Part> out = new ArrayList<>(parts.size());
		for (Part part : parts) {
			List<Keyframe> frames = new ArrayList<>(part.keyframes().size());
			for (Keyframe frame : part.keyframes()) {
				frames.add(new Keyframe((int) Math.round(frame.tick() * factor),
						frame.translation(), frame.rotation(), frame.scale()));
			}
			out.add(new Part(part.id(), part.itemModel(), frames));
		}
		return new PewpewRig(out, viewmodel, billboard);
	}
}
