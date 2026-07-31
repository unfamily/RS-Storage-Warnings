package net.unfamily.rs_storage_warnings.menu;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import net.unfamily.rs_storage_warnings.RsStorageWarnings;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(
            ForgeRegistries.MENU_TYPES, RsStorageWarnings.MODID);

    public static final RegistryObject<MenuType<StorageWarningMenu>> STORAGE_WARNING = MENU_TYPES
            .register("storage_warning", () -> IForgeMenuType.create(StorageWarningMenu::fromNetwork));

    private ModMenus() {
    }
}
