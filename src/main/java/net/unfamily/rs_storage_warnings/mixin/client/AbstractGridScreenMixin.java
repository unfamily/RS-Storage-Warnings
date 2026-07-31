package net.unfamily.rs_storage_warnings.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.refinedmods.refinedstorage.common.grid.screen.AbstractGridScreen;

import net.unfamily.rs_storage_warnings.client.WarningTypeSettingsButton;

@Mixin(AbstractGridScreen.class)
public abstract class AbstractGridScreenMixin {
    @Inject(method = "init", at = @At("TAIL"))
    private void rsStorageWarnings$addWarningSettingsButton(CallbackInfo ci) {
        AbstractGridScreen<?> self = (AbstractGridScreen<?>) (Object) this;
        self.addSideButton(new WarningTypeSettingsButton(self));
    }
}
