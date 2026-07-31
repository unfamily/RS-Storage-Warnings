package net.unfamily.rs_storage_warnings.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import net.unfamily.rs_storage_warnings.RsStorageWarnings;

public final class ModNetwork {
    private ModNetwork() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(RsStorageWarnings.MODID).versioned("1");
        registrar.playToServer(SetWarningTypeEnabledPayload.TYPE, SetWarningTypeEnabledPayload.STREAM_CODEC,
                SetWarningTypeEnabledPayload::handle);
        registrar.playToClient(SyncWarningSettingsPayload.TYPE, SyncWarningSettingsPayload.STREAM_CODEC,
                SyncWarningSettingsPayload::handle);
    }
}
