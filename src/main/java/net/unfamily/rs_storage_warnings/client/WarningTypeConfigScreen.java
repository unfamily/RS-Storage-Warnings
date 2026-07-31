package net.unfamily.rs_storage_warnings.client;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.support.registry.PlatformRegistry;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceType;
import com.refinedmods.refinedstorage.common.grid.screen.AbstractGridScreen;

import net.unfamily.rs_storage_warnings.network.SetWarningTypeEnabledPayload;
import net.unfamily.rs_storage_warnings.settings.PlayerWarningPreferences;

/**
 * Sub-screen opened from the grid toolbar: toggles capacity warnings per resource type.
 * <p>
 * Blurs the world once, then draws a sharp panel. The parent grid is not shown underneath.
 * Avoids {@link Screen#renderBackground} after the panel — a second blur would soften it.
 */
public class WarningTypeConfigScreen extends Screen {
    private static final int PANEL_WIDTH = 220;
    private static final int PADDING = 12;
    private static final int CHECKBOX_SPACING = 22;
    private static final int BUTTON_HEIGHT = 20;

    private final AbstractGridScreen<?> parent;
    private final Map<ResourceLocation, Checkbox> checkboxes = new LinkedHashMap<>();
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

        PlatformRegistry<ResourceType> registry = RefinedStorageApi.INSTANCE.getResourceTypeRegistry();
        List<ResourceType> types = registry.getAll();

        panelHeight = PADDING
                + 12 // title
                + 8
                + Math.max(1, types.size()) * CHECKBOX_SPACING
                + 8
                + BUTTON_HEIGHT
                + PADDING;
        panelLeft = (width - PANEL_WIDTH) / 2;
        panelTop = (height - panelHeight) / 2;

        int y = panelTop + PADDING + 12 + 8;
        for (ResourceType type : types) {
            ResourceLocation id = registry.getId(type).orElse(null);
            if (id == null) {
                continue;
            }
            boolean enabled = minecraft != null && minecraft.player != null
                    && PlayerWarningPreferences.isWarningEnabled(minecraft.player, id);
            final ResourceLocation typeId = id;
            Checkbox checkbox = Checkbox.builder(type.getTitle(), font)
                    .pos(panelLeft + PADDING, y)
                    .selected(enabled)
                    .onValueChange((box, selected) -> PacketDistributor.sendToServer(
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
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // World blur + dim only — do not render the parent grid underneath.
        renderBlurredBackground(partialTick);
        renderTransparentBackground(guiGraphics);

        drawPanel(guiGraphics);

        for (Renderable renderable : renderables) {
            renderable.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    private void drawPanel(GuiGraphics guiGraphics) {
        guiGraphics.fill(panelLeft, panelTop, panelLeft + PANEL_WIDTH, panelTop + panelHeight, 0xFFC6C6C6);
        guiGraphics.fill(panelLeft + 1, panelTop + 1, panelLeft + PANEL_WIDTH - 1, panelTop + panelHeight - 1,
                0xFF8B8B8B);
        guiGraphics.fill(panelLeft + 2, panelTop + 2, panelLeft + PANEL_WIDTH - 2, panelTop + panelHeight - 2,
                0xFFC6C6C6);

        int titleWidth = font.width(title);
        guiGraphics.drawString(font, title, panelLeft + (PANEL_WIDTH - titleWidth) / 2, panelTop + PADDING, 0x404040,
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
