package net.unfamily.rs_storage_warnings.capacity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.Identifier;
import net.minecraft.world.MenuProvider;

import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.impl.node.AbstractStorageContainerNetworkNode;
import com.refinedmods.refinedstorage.api.network.impl.node.storage.StorageNetworkNode;
import com.refinedmods.refinedstorage.api.network.node.GraphNetworkComponent;
import com.refinedmods.refinedstorage.api.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage.api.storage.StateTrackedStorage;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.api.storage.StorageState;
import com.refinedmods.refinedstorage.api.storage.composite.CompositeStorage;
import com.refinedmods.refinedstorage.api.storage.limited.LimitedStorage;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.grid.Grid;
import com.refinedmods.refinedstorage.common.api.storage.SerializableStorage;
import com.refinedmods.refinedstorage.common.api.storage.StorageType;
import com.refinedmods.refinedstorage.common.api.support.network.item.NetworkItemContext;
import com.refinedmods.refinedstorage.common.api.support.network.item.NetworkItemTargetBlockEntity;
import com.refinedmods.refinedstorage.common.storage.DiskInventory;

import net.unfamily.rs_storage_warnings.Config;
import net.unfamily.rs_storage_warnings.RsStorageWarnings;
import net.unfamily.rs_storage_warnings.intercept.GridResolver;

/**
 * Scans disk drives / storage blocks on the grid's network (and portable disks),
 * aggregating remaining capacity per {@link StorageType}.
 * <p>
 * Refined Storage 2 disks have amount capacity only (no AE2-style type slots).
 */
public final class DiskCapacityScanner {
    private static final Field STORAGES_FIELD = findStoragesField();

    private DiskCapacityScanner() {
    }

    public static List<CapacityWarning> scan(MenuProvider menuProvider) {
        Aggregator aggregator = new Aggregator();
        int disksSeen = 0;

        Grid grid = GridResolver.resolve(menuProvider);
        if (grid == null) {
            return List.of();
        }

        Network network = resolveNetwork(grid, menuProvider);
        if (network != null) {
            GraphNetworkComponent graph = network.getComponent(GraphNetworkComponent.class);
            int driveNodes = 0;
            for (NetworkNodeContainer container : graph.getContainers()) {
                if (!(container.getNode() instanceof StorageNetworkNode storageNode)) {
                    continue;
                }
                driveNodes++;
                disksSeen += addFromStorageNode(aggregator, storageNode);
            }
            if (Config.loggingEnabled()) {
                RsStorageWarnings.LOGGER.debug("Capacity scan: driveNodes={}, disksSeenSoFar={}", driveNodes,
                        disksSeen);
            }
        } else if (Config.loggingEnabled()) {
            RsStorageWarnings.LOGGER.debug("Capacity scan: no network resolved from grid {}",
                    grid.getClass().getName());
        }

        disksSeen += addPortableDisks(aggregator, grid, menuProvider);

        List<CapacityWarning> warnings = aggregator.toWarnings(Config.thresholdPercent());
        if (Config.loggingEnabled()) {
            RsStorageWarnings.LOGGER.debug(
                    "Capacity scan: disksSeen={}, buckets={}, warnings={}, threshold={}%", disksSeen,
                    aggregator.buckets.size(), warnings.size(), Config.thresholdPercent());
            for (Map.Entry<Identifier, Bucket> entry : aggregator.buckets.entrySet()) {
                Bucket bucket = entry.getValue();
                int rem = remainingPercent(bucket.used, bucket.total);
                RsStorageWarnings.LOGGER.debug("  bucket {} used={}/{} ({}% left)", entry.getKey(), bucket.used,
                        bucket.total, rem);
            }
            for (CapacityWarning warning : warnings) {
                RsStorageWarnings.LOGGER.debug("  warning {} {} remaining={}%", warning.storageTypeId(),
                        warning.kind(), warning.remainingPercent());
            }
        }
        return warnings;
    }

    @Nullable
    private static Network resolveNetwork(Grid grid, MenuProvider menuProvider) {
        if (menuProvider instanceof NetworkItemTargetBlockEntity target) {
            return target.getNetworkForItem();
        }
        if (grid instanceof NetworkItemTargetBlockEntity target) {
            return target.getNetworkForItem();
        }

        NetworkItemContext context = findTypedField(grid, NetworkItemContext.class);
        if (context != null) {
            return context.resolveNetwork().orElse(null);
        }
        return null;
    }

    private static int addPortableDisks(Aggregator aggregator, Grid grid, MenuProvider menuProvider) {
        DiskInventory inventory = findTypedField(menuProvider, DiskInventory.class);
        if (inventory == null) {
            inventory = findTypedField(grid, DiskInventory.class);
        }
        if (inventory == null) {
            return 0;
        }

        int added = 0;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            Optional<Storage> resolved = inventory.resolve(i);
            if (resolved.isPresent()) {
                addDiskStorage(aggregator, resolved.get());
                added++;
            }
        }
        return added;
    }

    private static int addFromStorageNode(Aggregator aggregator, StorageNetworkNode node) {
        Storage exposed = node.getStorage();
        if (exposed instanceof CompositeStorage composite && !composite.getSources().isEmpty()) {
            int added = 0;
            for (Storage source : composite.getSources()) {
                addDiskStorage(aggregator, source);
                added++;
            }
            return added;
        }

        StateTrackedStorage[] storages = readStorages(node);
        if (storages == null) {
            // Fall back to node totals without type split.
            long capacity = node.getCapacity();
            if (capacity > 0) {
                aggregator.add(Identifier.fromNamespaceAndPath("refinedstorage", "item"), node.getStored(),
                        capacity);
                return 1;
            }
            return 0;
        }

        int added = 0;
        for (StateTrackedStorage storage : storages) {
            if (storage != null) {
                addDiskStorage(aggregator, storage);
                added++;
            }
        }
        return added;
    }

    private static void addDiskStorage(Aggregator aggregator, Storage storage) {
        Storage delegate = storage instanceof StateTrackedStorage tracked ? tracked.getDelegate() : storage;

        long capacity = 0;
        if (storage instanceof LimitedStorage limited) {
            capacity = limited.getCapacity();
        } else if (delegate instanceof LimitedStorage limited) {
            capacity = limited.getCapacity();
        }
        if (capacity <= 0) {
            return;
        }

        long used = storage.getStored();
        if (storage instanceof StateTrackedStorage tracked && tracked.getState() == StorageState.FULL) {
            used = capacity;
        } else if (used > capacity) {
            used = capacity;
        }

        Identifier typeId = resolveStorageTypeId(delegate);
        aggregator.add(typeId, used, capacity);
    }

    private static Identifier resolveStorageTypeId(Storage storage) {
        if (storage instanceof SerializableStorage serializable) {
            StorageType type = serializable.getType();
            return RefinedStorageApi.INSTANCE.getStorageTypeRegistry()
                    .getId(type)
                    .orElseGet(() -> Identifier.fromNamespaceAndPath("refinedstorage", "unknown"));
        }
        return Identifier.fromNamespaceAndPath("refinedstorage", "item");
    }

    @Nullable
    private static StateTrackedStorage[] readStorages(AbstractStorageContainerNetworkNode node) {
        if (STORAGES_FIELD == null) {
            return null;
        }
        try {
            return (StateTrackedStorage[]) STORAGES_FIELD.get(node);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    @Nullable
    private static Field findStoragesField() {
        try {
            Field field = AbstractStorageContainerNetworkNode.class.getDeclaredField("storages");
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            RsStorageWarnings.LOGGER.error("Failed to access AbstractStorageContainerNetworkNode.storages", e);
            return null;
        }
    }

    @Nullable
    private static <T> T findTypedField(Object target, Class<T> type) {
        Class<?> current = target.getClass();
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                if (type.isAssignableFrom(field.getType())) {
                    try {
                        field.setAccessible(true);
                        Object value = field.get(target);
                        if (type.isInstance(value)) {
                            return type.cast(value);
                        }
                    } catch (IllegalAccessException ignored) {
                    }
                }
            }
            current = current.getSuperclass();
        }
        return null;
    }

    private static final class Aggregator {
        private final Map<Identifier, Bucket> buckets = new HashMap<>();

        void add(Identifier typeId, long used, long total) {
            Bucket bucket = buckets.computeIfAbsent(typeId, ignored -> new Bucket());
            bucket.used += used;
            bucket.total += total;
        }

        List<CapacityWarning> toWarnings(int threshold) {
            List<CapacityWarning> warnings = new ArrayList<>();
            for (Map.Entry<Identifier, Bucket> entry : buckets.entrySet()) {
                Bucket bucket = entry.getValue();
                if (bucket.total <= 0) {
                    continue;
                }
                int remainingPct = remainingPercent(bucket.used, bucket.total);
                if (remainingPct <= threshold) {
                    warnings.add(new CapacityWarning(entry.getKey(), CapacityWarning.Kind.BYTES, remainingPct));
                }
            }
            return warnings;
        }
    }

    /**
     * Remaining % complementary to floored used % (same style RS progress uses),
     * so 90% full reports 10% left instead of floor(free) = 9%.
     */
    static int remainingPercent(long used, long total) {
        if (total <= 0) {
            return 100;
        }
        long clampedUsed = Math.min(Math.max(0, used), total);
        int usedPct = (int) Math.floor(100.0 * clampedUsed / total);
        return Math.max(0, 100 - usedPct);
    }

    private static final class Bucket {
        long used;
        long total;
    }
}
