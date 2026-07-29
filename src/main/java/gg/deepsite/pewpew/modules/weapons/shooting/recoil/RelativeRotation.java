package gg.deepsite.pewpew.modules.weapons.shooting.recoil;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

final class RelativeRotation {

    private static final MethodHandle GET_HANDLE = resolveGetHandle();
    private static final MethodHandle FORCE_SET_ROTATION = resolveForceSetRotation();

    private RelativeRotation() {
    }

    static boolean available() {
        return GET_HANDLE != null && FORCE_SET_ROTATION != null;
    }

    static boolean apply(@NotNull Player player, float deltaYaw, float deltaPitch) {
        if (!available()) return false;
        try {
            Object handle = GET_HANDLE.invoke(player);
            FORCE_SET_ROTATION.invoke(handle, deltaYaw, true, deltaPitch, true);
            return true;
        } catch (Throwable throwable) {
            return false;
        }
    }

    private static MethodHandle resolveGetHandle() {
        try {
            Class<?> craftPlayer = Class.forName("org.bukkit.craftbukkit.entity.CraftPlayer");
            Class<?> serverPlayer = Class.forName("net.minecraft.server.level.ServerPlayer");
            return MethodHandles.lookup().findVirtual(craftPlayer, "getHandle", MethodType.methodType(serverPlayer));
        } catch (ReflectiveOperationException exception) {
            return null;
        }
    }

    private static MethodHandle resolveForceSetRotation() {
        try {
            Class<?> serverPlayer = Class.forName("net.minecraft.server.level.ServerPlayer");
            return MethodHandles.lookup().findVirtual(serverPlayer, "forceSetRotation",
                    MethodType.methodType(void.class, float.class, boolean.class, float.class, boolean.class));
        } catch (ReflectiveOperationException exception) {
            return null;
        }
    }
}
