package net.unfamily.rs_storage_warnings;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;

import net.unfamily.rs_storage_warnings.client.ClientSetup;
import net.unfamily.rs_storage_warnings.menu.ModMenus;
import net.unfamily.rs_storage_warnings.network.ModNetwork;

@Mod(RsStorageWarnings.MODID)
public class RsStorageWarnings {
    public static final String MODID = "rs_storage_warnings";
    public static final Logger LOGGER = LogUtils.getLogger();

    public RsStorageWarnings(IEventBus modEventBus, ModContainer modContainer) {
        ModMenus.MENU_TYPES.register(modEventBus);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        modEventBus.addListener(ModNetwork::register);

        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            ClientSetup.init(modEventBus);
        }
    }
}
