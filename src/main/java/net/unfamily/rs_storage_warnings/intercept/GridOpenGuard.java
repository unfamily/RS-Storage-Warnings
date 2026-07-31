package net.unfamily.rs_storage_warnings.intercept;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import com.refinedmods.refinedstorage.apiimpl.API;
import com.refinedmods.refinedstorage.inventory.player.PlayerSlot;

/**
 * One-shot ignore skips and deferred grid reopen after the warning dialog.
 */
public final class GridOpenGuard {
    private static final Set<UUID> SKIP_ONCE = ConcurrentHashMap.newKeySet();
    private static final ConcurrentHashMap<UUID, PendingGridOpen> PENDING = new ConcurrentHashMap<>();

    private GridOpenGuard() {
    }

    public static void armSkip(Player player) {
        SKIP_ONCE.add(player.getUUID());
    }

    public static boolean consumeSkip(Player player) {
        return SKIP_ONCE.remove(player.getUUID());
    }

    public static void storePending(Player player, PendingGridOpen pending) {
        PENDING.put(player.getUUID(), pending);
    }

    @Nullable
    public static PendingGridOpen takePending(Player player) {
        return PENDING.remove(player.getUUID());
    }

    public static void openPending(ServerPlayer player) {
        PendingGridOpen pending = takePending(player);
        if (pending == null) {
            return;
        }
        armSkip(player);
        if (pending.pos() != null) {
            API.instance().getGridManager().openGrid(pending.factoryId(), player, pending.pos());
        } else if (pending.stack() != null) {
            API.instance().getGridManager().openGrid(pending.factoryId(), player, pending.stack(), pending.slot());
        }
    }

    public record PendingGridOpen(ResourceLocation factoryId, @Nullable ItemStack stack, @Nullable BlockPos pos,
            PlayerSlot slot) {
    }
}
