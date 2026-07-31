package net.unfamily.rs_storage_warnings.intercept;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.Nullable;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;

import com.refinedmods.refinedstorage.common.Platform;

/**
 * Detects Refined Storage grid opens and manages one-shot ignore skips.
 */
public final class GridOpenGuard {
    private static final Set<UUID> SKIP_ONCE = ConcurrentHashMap.newKeySet();
    private static final ConcurrentHashMap<UUID, MenuProvider> PENDING = new ConcurrentHashMap<>();

    private GridOpenGuard() {
    }

    public static boolean shouldIntercept(MenuProvider menuProvider) {
        return GridResolver.resolve(menuProvider) != null;
    }

    public static void armSkip(Player player) {
        SKIP_ONCE.add(player.getUUID());
    }

    public static boolean consumeSkip(Player player) {
        return SKIP_ONCE.remove(player.getUUID());
    }

    public static void storePending(Player player, MenuProvider menuProvider) {
        PENDING.put(player.getUUID(), menuProvider);
    }

    @Nullable
    public static MenuProvider takePending(Player player) {
        return PENDING.remove(player.getUUID());
    }

    public static void openPending(ServerPlayer player) {
        MenuProvider pending = takePending(player);
        if (pending == null) {
            return;
        }
        armSkip(player);
        Platform.INSTANCE.getMenuOpener().openMenu(player, pending);
    }
}
