package net.unfamily.rs_storage_warnings.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.refinedmods.refinedstorage.screen.grid.GridScreen;

import net.unfamily.rs_storage_warnings.client.WarningTypeSettingsButton;

@Mixin(GridScreen.class)
public abstract class GridScreenMixin {
    @Inject(method = "onPostInit", at = @At("TAIL"), remap = false)
    private void rsStorageWarnings$addWarningSettingsButton(int x, int y, CallbackInfo ci) {
        GridScreen self = (GridScreen) (Object) this;
        self.addSideButton(new WarningTypeSettingsButton(self));
    }
}
