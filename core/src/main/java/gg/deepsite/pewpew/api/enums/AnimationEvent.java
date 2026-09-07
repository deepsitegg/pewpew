package gg.deepsite.pewpew.api.enums;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public enum AnimationEvent {

	FIRE("fire"),
	RELOAD("reload"),
	RELOAD_ROUND("reload-round"),
	SCOPE_IN("scope-in"),
	SCOPE_OUT("scope-out");

	private final String path;

	AnimationEvent(@NotNull String path) {
		this.path = path;
	}

	@Nullable
	public static AnimationEvent fromPath(@NotNull String path) {
		for (AnimationEvent event : values()) {
			if (event.path.equalsIgnoreCase(path)) return event;
		}
		return null;
	}
}
