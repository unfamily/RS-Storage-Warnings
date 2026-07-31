package net.unfamily.rs_storage_warnings.capacity;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.grid.view.GridResourceType;

public record CapacityWarning(Identifier storageTypeId, Kind kind, int remainingPercent) {
    public enum Kind {
        TYPES,
        BYTES
    }

    public Component storageTypeName() {
        return RefinedStorageApi.INSTANCE.getGridResourceTypeRegistry()
                .get(storageTypeId)
                .map(type -> (Component) type.getTitle())
                .orElseGet(() -> Component.literal(storageTypeId.toString()));
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeIdentifier(storageTypeId);
        buf.writeEnum(kind);
        buf.writeVarInt(remainingPercent);
    }

    public static CapacityWarning read(FriendlyByteBuf buf) {
        return new CapacityWarning(buf.readIdentifier(), buf.readEnum(Kind.class), buf.readVarInt());
    }
}
