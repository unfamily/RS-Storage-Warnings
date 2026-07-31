package net.unfamily.rs_storage_warnings.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import net.unfamily.rs_storage_warnings.RsStorageWarnings;
import net.unfamily.rs_storage_warnings.settings.PlayerWarningPreferences;

/** C2S: enable/disable capacity warnings for one storage/resource type. */
public record SetWarningTypeEnabledPayload(ResourceLocation storageTypeId, boolean enabled)
        implements CustomPacketPayload {

    public static final Type<SetWarningTypeEnabledPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(RsStorageWarnings.MODID, "set_warning_type"));

    public static final StreamCodec<FriendlyByteBuf, SetWarningTypeEnabledPayload> STREAM_CODEC = StreamCodec
            .composite(
                    ResourceLocation.STREAM_CODEC,
                    SetWarningTypeEnabledPayload::storageTypeId,
                    ByteBufCodecs.BOOL,
                    SetWarningTypeEnabledPayload::enabled,
                    SetWarningTypeEnabledPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SetWarningTypeEnabledPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }
            PlayerWarningPreferences.setWarningEnabled(serverPlayer, payload.storageTypeId(), payload.enabled());
            PacketDistributor.sendToPlayer(serverPlayer,
                    new SyncWarningSettingsPayload(PlayerWarningPreferences.getDisabledIds(serverPlayer)));
        });
    }
}
