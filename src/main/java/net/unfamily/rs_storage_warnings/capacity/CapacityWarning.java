package net.unfamily.rs_storage_warnings.capacity;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public record CapacityWarning(ResourceLocation storageTypeId, Kind kind, int remainingPercent) {
    public enum Kind {
        TYPES,
        BYTES
    }

    public Component storageTypeName() {
        if (storageTypeId.getPath().equals("fluid")) {
            return Component.translatable("sidebutton.refinedstorage.type.1");
        }
        return Component.translatable("sidebutton.refinedstorage.type.0");
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
