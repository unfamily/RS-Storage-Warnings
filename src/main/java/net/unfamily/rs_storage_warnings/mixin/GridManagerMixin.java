package net.unfamily.rs_storage_warnings.mixin;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import com.refinedmods.refinedstorage.api.network.grid.IGrid;
import com.refinedmods.refinedstorage.apiimpl.network.grid.GridManager;
import com.refinedmods.refinedstorage.inventory.player.PlayerSlot;

import net.unfamily.rs_storage_warnings.Config;
import net.unfamily.rs_storage_warnings.RsStorageWarnings;
import net.unfamily.rs_storage_warnings.capacity.CapacityWarning;
import net.unfamily.rs_storage_warnings.capacity.DiskCapacityScanner;
import net.unfamily.rs_storage_warnings.intercept.GridOpenGuard;
import net.unfamily.rs_storage_warnings.menu.StorageWarningMenu;
import net.unfamily.rs_storage_warnings.settings.PlayerWarningPreferences;

@Mixin(GridManager.class)
public class GridManagerMixin {
    @Inject(method = "openGrid(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/core/BlockPos;Lcom/refinedmods/refinedstorage/inventory/player/PlayerSlot;)V", at = @At("HEAD"), cancellable = true, remap = false)
    private void rsStorageWarnings$interceptOpen(ResourceLocation id, ServerPlayer player, ItemStack stack,
            BlockPos pos, PlayerSlot slot, CallbackInfo ci) {
        if (GridOpenGuard.consumeSkip(player)) {
            if (Config.loggingEnabled()) {
                RsStorageWarnings.LOGGER.debug("Skipping warning for {}", player.getGameProfile().getName());
            }
            return;
        }

        Pair<IGrid, BlockEntity> created = ((GridManager) (Object) this).createGrid(id, player, stack, pos, slot);
        if (created == null || created.getLeft() == null) {
            return;
        }

        List<CapacityWarning> warnings = DiskCapacityScanner.scan(created.getLeft());
        warnings = warnings.stream()
                .filter(warning -> PlayerWarningPreferences.isWarningEnabled(player, warning.storageTypeId()))
                .toList();
        if (warnings.isEmpty()) {
            return;
        }

        if (Config.loggingEnabled()) {
            RsStorageWarnings.LOGGER.debug("Intercepting grid open for {} with {} warning(s)",
                    player.getGameProfile().getName(), warnings.size());
        }

        ItemStack stackCopy = stack == null ? null : stack.copy();
        GridOpenGuard.storePending(player, new GridOpenGuard.PendingGridOpen(id, stackCopy, pos, slot));
        StorageWarningMenu.open(player, warnings);
        ci.cancel();
    }
}
