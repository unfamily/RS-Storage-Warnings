package net.unfamily.rs_storage_warnings.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;

import com.refinedmods.refinedstorage.screen.grid.GridScreen;
import com.refinedmods.refinedstorage.screen.widget.sidebutton.SideButton;

import net.unfamily.rs_storage_warnings.RsStorageWarnings;

public class WarningTypeSettingsButton extends SideButton {
    private static final ResourceLocation TEXTURE = new ResourceLocation(RsStorageWarnings.MODID,
            "textures/gui/sprites/widget/warning_settings.png");

    public WarningTypeSettingsButton(GridScreen screen) {
        super(screen);
    }

    @Override
    protected void renderButtonIcon(GuiGraphics graphics, int x, int y) {
        graphics.blit(TEXTURE, x, y, 0, 0, 16, 16, 16, 16);
    }

    @Override
    protected String getSideButtonTooltip() {
        return I18n.get("gui.rs_storage_warnings.warning_types.button") + "\n"
                + I18n.get("gui.rs_storage_warnings.warning_types.button.detail");
    }

    @Override
    public void onPress() {
        if (screen instanceof GridScreen gridScreen) {
            Minecraft.getInstance().setScreen(new WarningTypeConfigScreen(gridScreen));
        }
    }
}
