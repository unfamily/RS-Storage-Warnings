package net.unfamily.rs_storage_warnings.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;

import com.refinedmods.refinedstorage.neoforge.support.containermenu.MenuOpenerImpl;

import net.unfamily.rs_storage_warnings.Config;
import net.unfamily.rs_storage_warnings.RsStorageWarnings;
import net.unfamily.rs_storage_warnings.capacity.CapacityWarning;
import net.unfamily.rs_storage_warnings.capacity.DiskCapacityScanner;
import net.unfamily.rs_storage_warnings.intercept.GridOpenGuard;
import net.unfamily.rs_storage_warnings.menu.StorageWarningMenu;
import net.unfamily.rs_storage_warnings.settings.PlayerWarningPreferences;

@Mixin(MenuOpenerImpl.class)
public class MenuOpenerMixin {
    @Inject(method = "openMenu", at = @At("HEAD"), cancellable = true, remap = false)
    private void rsStorageWarnings$interceptOpen(ServerPlayer player, MenuProvider menuProvider, CallbackInfo ci) {
        if (!GridOpenGuard.shouldIntercept(menuProvider)) {
            return;
        }
        if (GridOpenGuard.consumeSkip(player)) {
            if (Config.loggingEnabled()) {
                RsStorageWarnings.LOGGER.debug("Skipping warning for {}", player.getGameProfile().getName());
            }
            return;
        }

        List<CapacityWarning> warnings = DiskCapacityScanner.scan(menuProvider);
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

        StorageWarningMenu.open(player, warnings, menuProvider);
        ci.cancel();
    }
}
