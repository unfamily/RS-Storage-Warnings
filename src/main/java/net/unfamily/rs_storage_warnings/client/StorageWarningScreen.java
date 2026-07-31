package net.unfamily.rs_storage_warnings.client;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;

import net.unfamily.rs_storage_warnings.capacity.CapacityWarning;
import net.unfamily.rs_storage_warnings.menu.StorageWarningMenu;

public class StorageWarningScreen extends AbstractContainerScreen<StorageWarningMenu> {
    private static final int PADDING = 12;
    private static final int LINE_HEIGHT = 12;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_WIDTH = 140;
    private static final int MIN_WIDTH = 240;
    private static final int MAX_HEIGHT = 300;

    private final List<FormattedCharSequence> warningLines = new ArrayList<>();
    private int panelWidth = MIN_WIDTH;
    private int panelHeight = MAX_HEIGHT;

    public StorageWarningScreen(StorageWarningMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, MIN_WIDTH, MAX_HEIGHT);
    }

    @Override
    protected void init() {
        super.init();
        warningLines.clear();

        warningLines.addAll(font.split(Component.translatable("gui.rs_storage_warnings.warning.intro"),
                MIN_WIDTH - PADDING * 2));
        warningLines.add(FormattedCharSequence.EMPTY);

        for (CapacityWarning warning : menu.getWarnings()) {
            Component line = switch (warning.kind()) {
                case TYPES -> Component.translatable("gui.rs_storage_warnings.warning.types",
                        warning.storageTypeName());
                case BYTES -> Component.translatable("gui.rs_storage_warnings.warning.bytes",
                        warning.storageTypeName(), warning.remainingPercent());
            };
            warningLines.addAll(font.split(line, MIN_WIDTH - PADDING * 2));
        }

        int contentHeight = LINE_HEIGHT
                + 4
                + warningLines.size() * LINE_HEIGHT
                + 10
                + BUTTON_HEIGHT
                + PADDING;

        panelWidth = MIN_WIDTH;
        panelHeight = Math.max(100, contentHeight + PADDING);
        leftPos = (width - panelWidth) / 2;
        topPos = (height - panelHeight) / 2;

        int buttonX = leftPos + (panelWidth - BUTTON_WIDTH) / 2;
        int buttonY = topPos + panelHeight - PADDING - BUTTON_HEIGHT;
        addRenderableWidget(Button.builder(Component.translatable("gui.rs_storage_warnings.warning.ignore"), btn -> {
            if (minecraft != null && minecraft.gameMode != null) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, StorageWarningMenu.BUTTON_IGNORE);
            }
        }).bounds(buttonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT).build());
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        int x = leftPos;
        int y = topPos;
        guiGraphics.fill(x, y, x + panelWidth, y + panelHeight, 0xFFC6C6C6);
        guiGraphics.fill(x + 1, y + 1, x + panelWidth - 1, y + panelHeight - 1, 0xFF8B8B8B);
        guiGraphics.fill(x + 2, y + 2, x + panelWidth - 2, y + panelHeight - 2, 0xFFC6C6C6);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        int y = PADDING;
        int titleWidth = font.width(title);
        guiGraphics.text(font, title, (panelWidth - titleWidth) / 2, y, 0xFFFF5555, false);
        y += LINE_HEIGHT + 4;

        for (FormattedCharSequence line : warningLines) {
            guiGraphics.text(font, line, PADDING, y, 0xFF404040, false);
            y += LINE_HEIGHT;
        }
    }
}
