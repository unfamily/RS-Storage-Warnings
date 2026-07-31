package net.unfamily.rs_storage_warnings.capacity;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;

public record CapacityWarning(ResourceLocation storageTypeId, Kind kind, int remainingPercent) {
    public enum Kind {
        TYPES,
        BYTES
    }

    public Component storageTypeName() {
        return RefinedStorageApi.INSTANCE.getResourceTypeRegistry()
                .get(storageTypeId)
                .map(type -> (Component) type.getTitle())
                .orElseGet(() -> Component.literal(storageTypeId.toString()));
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeResourceLocation(storageTypeId);
        buf.writeEnum(kind);
        buf.writeVarInt(remainingPercent);
    }

    public static CapacityWarning read(FriendlyByteBuf buf) {
        return new CapacityWarning(buf.readResourceLocation(), buf.readEnum(Kind.class), buf.readVarInt());
    }
}
