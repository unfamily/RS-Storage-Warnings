package net.unfamily.rs_storage_warnings.network;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import net.unfamily.rs_storage_warnings.settings.PlayerWarningPreferences;

/** C2S: enable/disable capacity warnings for one storage type. */
public class SetWarningTypeEnabledPayload {
    private final ResourceLocation storageTypeId;
    private final boolean enabled;

    public SetWarningTypeEnabledPayload(ResourceLocation storageTypeId, boolean enabled) {
        this.storageTypeId = storageTypeId;
        this.enabled = enabled;
    }

    public static void encode(SetWarningTypeEnabledPayload msg, FriendlyByteBuf buf) {
        buf.writeResourceLocation(msg.storageTypeId);
        buf.writeBoolean(msg.enabled);
    }

    public static SetWarningTypeEnabledPayload decode(FriendlyByteBuf buf) {
        return new SetWarningTypeEnabledPayload(buf.readResourceLocation(), buf.readBoolean());
    }

    public static void handle(SetWarningTypeEnabledPayload msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context ctx = contextSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer serverPlayer = ctx.getSender();
            if (serverPlayer == null) {
                return;
            }
            PlayerWarningPreferences.setWarningEnabled(serverPlayer, msg.storageTypeId, msg.enabled);
            ModNetwork.sendToPlayer(serverPlayer,
                    new SyncWarningSettingsPayload(PlayerWarningPreferences.getDisabledIds(serverPlayer)));
        });
        ctx.setPacketHandled(true);
    }
}
