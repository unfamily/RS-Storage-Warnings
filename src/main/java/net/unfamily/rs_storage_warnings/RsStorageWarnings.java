package net.unfamily.rs_storage_warnings;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import net.unfamily.rs_storage_warnings.client.ClientSetup;
import net.unfamily.rs_storage_warnings.menu.ModMenus;
import net.unfamily.rs_storage_warnings.network.ModNetwork;

@Mod(RsStorageWarnings.MODID)
public class RsStorageWarnings {
    public static final String MODID = "rs_storage_warnings";
    public static final Logger LOGGER = LogUtils.getLogger();

    public RsStorageWarnings() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModMenus.MENU_TYPES.register(modEventBus);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        ModNetwork.register();

        MinecraftForge.EVENT_BUS.register(PlayerEvents.class);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientSetup.init(modEventBus));
    }
}
