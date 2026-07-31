package net.unfamily.rs_storage_warnings.menu;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;

import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import net.unfamily.rs_storage_warnings.RsStorageWarnings;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU,
            RsStorageWarnings.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<StorageWarningMenu>> STORAGE_WARNING = MENU_TYPES
            .register("storage_warning", () -> IMenuTypeExtension.create(StorageWarningMenu.factory()));

    private ModMenus() {
    }
}
