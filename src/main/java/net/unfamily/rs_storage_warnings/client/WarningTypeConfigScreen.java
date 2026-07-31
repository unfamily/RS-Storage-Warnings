package net.unfamily.rs_storage_warnings.client;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import com.refinedmods.refinedstorage.screen.grid.GridScreen;

import net.unfamily.rs_storage_warnings.network.ModNetwork;
import net.unfamily.rs_storage_warnings.network.SetWarningTypeEnabledPayload;
import net.unfamily.rs_storage_warnings.settings.PlayerWarningPreferences;

/**
 * Sub-screen opened from the grid toolbar: toggles capacity warnings per resource type.
 */
public class WarningTypeConfigScreen extends Screen {
    private static final int PANEL_WIDTH = 220;
    private static final int PADDING = 12;
    private static final int CHECKBOX_SPACING = 22;
    private static final int BUTTON_HEIGHT = 20;

    private static final List<ResourceLocation> CHANNELS = List.of(
            new ResourceLocation("refinedstorage", "item"),
            new ResourceLocation("refinedstorage", "fluid"));

    private final GridScreen parent;
    private final Map<ResourceLocation, Checkbox> checkboxes = new LinkedHashMap<>();
    private int panelLeft;
    private int panelTop;
    private int panelHeight;

    public WarningTypeConfigScreen(GridScreen parent) {
        super(Component.translatable("gui.rs_storage_warnings.warning_types.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        checkboxes.clear();
        clearWidgets();

        panelHeight = PADDING
                + 12
                + 8
                + CHANNELS.size() * CHECKBOX_SPACING
                + 8
                + BUTTON_HEIGHT
                + PADDING;
        panelLeft = (width - PANEL_WIDTH) / 2;
        panelTop = (height - panelHeight) / 2;

        int y = panelTop + PADDING + 12 + 8;
        for (ResourceLocation id : CHANNELS) {
            boolean enabled = minecraft != null && minecraft.player != null
                    && PlayerWarningPreferences.isWarningEnabled(minecraft.player, id);
            Component label = id.getPath().equals("fluid")
                    ? Component.translatable("sidebutton.refinedstorage.type.1")
                    : Component.translatable("sidebutton.refinedstorage.type.0");
            final ResourceLocation typeId = id;
            Checkbox checkbox = new Checkbox(panelLeft + PADDING, y, PANEL_WIDTH - PADDING * 2, 20, label, enabled) {
                @Override
                public void onPress() {
                    super.onPress();
                    ModNetwork.sendToServer(new SetWarningTypeEnabledPayload(typeId, selected()));
                }
            };
            addRenderableWidget(checkbox);
            checkboxes.put(id, checkbox);
            y += CHECKBOX_SPACING;
        }

        addRenderableWidget(Button.builder(Component.translatable("gui.rs_storage_warnings.warning_types.back"),
                btn -> onClose()).bounds(panelLeft + (PANEL_WIDTH - 100) / 2,
                        panelTop + panelHeight - PADDING - BUTTON_HEIGHT, 100, BUTTON_HEIGHT).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);

        guiGraphics.fill(panelLeft, panelTop, panelLeft + PANEL_WIDTH, panelTop + panelHeight, 0xFFC6C6C6);
        guiGraphics.fill(panelLeft + 1, panelTop + 1, panelLeft + PANEL_WIDTH - 1, panelTop + panelHeight - 1,
                0xFF8B8B8B);
        guiGraphics.fill(panelLeft + 2, panelTop + 2, panelLeft + PANEL_WIDTH - 2, panelTop + panelHeight - 2,
                0xFFC6C6C6);

        int titleWidth = font.width(title);
        guiGraphics.drawString(font, title, panelLeft + (PANEL_WIDTH - titleWidth) / 2, panelTop + PADDING, 0x404040,
                false);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreen(parent);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
