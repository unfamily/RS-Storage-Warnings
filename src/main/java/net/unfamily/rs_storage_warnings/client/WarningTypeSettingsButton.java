package net.unfamily.rs_storage_warnings.client;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import com.refinedmods.refinedstorage.common.grid.screen.AbstractGridScreen;
import com.refinedmods.refinedstorage.common.support.widget.AbstractSideButtonWidget;

import net.unfamily.rs_storage_warnings.RsStorageWarnings;

public class WarningTypeSettingsButton extends AbstractSideButtonWidget {
    private static final ResourceLocation SPRITE = ResourceLocation.fromNamespaceAndPath(
            RsStorageWarnings.MODID, "widget/warning_settings");

    public WarningTypeSettingsButton(AbstractGridScreen<?> parent) {
        super(btn -> Minecraft.getInstance().setScreen(new WarningTypeConfigScreen(parent)));
    }

    @Override
    protected ResourceLocation getSprite() {
        return SPRITE;
    }

    @Override
    protected MutableComponent getTitle() {
        return Component.translatable("gui.rs_storage_warnings.warning_types.button");
    }

    @Override
    protected List<MutableComponent> getSubText() {
        return List.of(Component.translatable("gui.rs_storage_warnings.warning_types.button.detail")
                .withStyle(ChatFormatting.GRAY));
    }
}
