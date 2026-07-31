package net.unfamily.rs_storage_warnings.settings;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import net.unfamily.rs_storage_warnings.RsStorageWarnings;

/**
 * Per-player toggles for which storage-type capacity warnings are shown.
 * Checked = warnings enabled (default). Stored values are the disabled type ids.
 */
public final class PlayerWarningPreferences {
    private static final String ROOT_KEY = RsStorageWarnings.MODID;
    private static final String DISABLED_KEY = "disabled_warning_types";

    private static final Set<String> CLIENT_DISABLED = ConcurrentHashMap.newKeySet();

    private PlayerWarningPreferences() {
    }

    public static boolean isWarningEnabled(Player player, ResourceLocation storageTypeId) {
        if (player.level().isClientSide()) {
            return !CLIENT_DISABLED.contains(storageTypeId.toString());
        }
        return !readDisabled(player).contains(storageTypeId.toString());
    }

    public static Set<String> getDisabledIds(Player player) {
        if (player.level().isClientSide()) {
            return Set.copyOf(CLIENT_DISABLED);
        }
        return Set.copyOf(readDisabled(player));
    }

    public static void setWarningEnabled(ServerPlayer player, ResourceLocation storageTypeId, boolean enabled) {
        Set<String> disabled = readDisabled(player);
        String id = storageTypeId.toString();
        if (enabled) {
            disabled.remove(id);
        } else {
            disabled.add(id);
        }
        writeDisabled(player, disabled);
    }

    public static void replaceClientCache(Set<String> disabledIds) {
        CLIENT_DISABLED.clear();
        CLIENT_DISABLED.addAll(disabledIds);
    }

    private static Set<String> readDisabled(Player player) {
        var root = player.getPersistentData().getCompound(ROOT_KEY);
        Set<String> disabled = new HashSet<>();
        ListTag list = root.getList(DISABLED_KEY, Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) {
            disabled.add(list.getString(i));
        }
        return disabled;
    }

    private static void writeDisabled(Player player, Set<String> disabled) {
        ListTag list = new ListTag();
        for (String id : disabled) {
            list.add(StringTag.valueOf(id));
        }
        var root = player.getPersistentData().getCompound(ROOT_KEY);
        root.put(DISABLED_KEY, list);
        player.getPersistentData().put(ROOT_KEY, root);
    }
}
