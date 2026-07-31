package net.unfamily.rs_storage_warnings.menu;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkHooks;

import net.unfamily.rs_storage_warnings.capacity.CapacityWarning;
import net.unfamily.rs_storage_warnings.intercept.GridOpenGuard;

public class StorageWarningMenu extends AbstractContainerMenu {
    public static final int BUTTON_IGNORE = 0;

    private final List<CapacityWarning> warnings;

    public StorageWarningMenu(int containerId, Inventory playerInventory, List<CapacityWarning> warnings) {
        super(ModMenus.STORAGE_WARNING.get(), containerId);
        this.warnings = List.copyOf(warnings);
    }

    public static StorageWarningMenu fromNetwork(int containerId, Inventory inventory, FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        List<CapacityWarning> warnings = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            warnings.add(CapacityWarning.read(buf));
        }
        return new StorageWarningMenu(containerId, inventory, warnings);
    }

    public static void open(ServerPlayer player, List<CapacityWarning> warnings) {
        NetworkHooks.openScreen(player,
                new SimpleMenuProvider((id, inv, p) -> new StorageWarningMenu(id, inv, warnings),
                        Component.translatable("gui.rs_storage_warnings.warning.title")),
                buf -> writeExtraData(buf, warnings));
    }

    private static void writeExtraData(FriendlyByteBuf buf, List<CapacityWarning> warnings) {
        buf.writeVarInt(warnings.size());
        for (CapacityWarning warning : warnings) {
            warning.write(buf);
        }
    }

    public List<CapacityWarning> getWarnings() {
        return warnings;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == BUTTON_IGNORE && !player.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
            GridOpenGuard.openPending(serverPlayer);
            return true;
        }
        return false;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide()) {
            GridOpenGuard.takePending(player);
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
