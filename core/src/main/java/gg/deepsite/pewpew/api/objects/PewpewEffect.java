package gg.deepsite.pewpew.api.objects;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

public record PewpewEffect(@NotNull Key type, int duration, int amplifier) {
}
