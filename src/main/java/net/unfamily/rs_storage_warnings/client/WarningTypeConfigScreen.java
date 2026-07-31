package net.unfamily.rs_storage_warnings.client;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.grid.view.GridResourceType;
import com.refinedmods.refinedstorage.common.api.support.registry.PlatformRegistry;
import com.refinedmods.refinedstorage.common.grid.screen.AbstractGridScreen;

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

    private final AbstractGridScreen<?> parent;
    private final Map<Identifier, Checkbox> checkboxes = new LinkedHashMap<>();
    private int panelLeft;
    private int panelTop;
    private int panelHeight;

    public WarningTypeConfigScreen(AbstractGridScreen<?> parent) {
        super(Component.translatable("gui.rs_storage_warnings.warning_types.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        checkboxes.clear();
        clearWidgets();

        PlatformRegistry<GridResourceType> registry = RefinedStorageApi.INSTANCE.getGridResourceTypeRegistry();
        List<GridResourceType> types = registry.getAll();

        panelHeight = PADDING
                + 12
                + 8
                + Math.max(1, types.size()) * CHECKBOX_SPACING
                + 8
                + BUTTON_HEIGHT
                + PADDING;
        panelLeft = (width - PANEL_WIDTH) / 2;
        panelTop = (height - panelHeight) / 2;

        int y = panelTop + PADDING + 12 + 8;
        for (GridResourceType type : types) {
            Identifier id = registry.getId(type).orElse(null);
            if (id == null) {
                continue;
            }
            boolean enabled = minecraft != null && minecraft.player != null
                    && PlayerWarningPreferences.isWarningEnabled(minecraft.player, id);
            final Identifier typeId = id;
            Checkbox checkbox = Checkbox.builder(type.getTitle(), font)
                    .pos(panelLeft + PADDING, y)
                    .selected(enabled)
                    .onValueChange((box, selected) -> ClientPacketDistributor.sendToServer(
                            new SetWarningTypeEnabledPayload(typeId, selected)))
                    .build();
            addRenderableWidget(checkbox);
            checkboxes.put(id, checkbox);
            y += CHECKBOX_SPACING;
        }

        addRenderableWidget(Button.builder(Component.translatable("gui.rs_storage_warnings.warning_types.back"),
                btn -> onClose()).bounds(panelLeft + (PANEL_WIDTH - 100) / 2,
                        panelTop + panelHeight - PADDING - BUTTON_HEIGHT, 100, BUTTON_HEIGHT).build());
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        extractBlurredBackground(guiGraphics);
        extractTransparentBackground(guiGraphics);
        drawPanel(guiGraphics);
    }

    private void drawPanel(GuiGraphicsExtractor guiGraphics) {
        guiGraphics.fill(panelLeft, panelTop, panelLeft + PANEL_WIDTH, panelTop + panelHeight, 0xFFC6C6C6);
        guiGraphics.fill(panelLeft + 1, panelTop + 1, panelLeft + PANEL_WIDTH - 1, panelTop + panelHeight - 1,
                0xFF8B8B8B);
        guiGraphics.fill(panelLeft + 2, panelTop + 2, panelLeft + PANEL_WIDTH - 2, panelTop + panelHeight - 2,
                0xFFC6C6C6);

        int titleWidth = font.width(title);
        guiGraphics.text(font, title, panelLeft + (PANEL_WIDTH - titleWidth) / 2, panelTop + PADDING, 0xFF404040,
                false);
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
