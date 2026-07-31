package net.unfamily.rs_storage_warnings.capacity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.ResourceLocation;

import com.refinedmods.refinedstorage.api.network.INetwork;
import com.refinedmods.refinedstorage.api.network.INetworkNodeGraphEntry;
import com.refinedmods.refinedstorage.api.network.grid.IGrid;
import com.refinedmods.refinedstorage.api.network.grid.INetworkAwareGrid;
import com.refinedmods.refinedstorage.api.network.node.INetworkNode;
import com.refinedmods.refinedstorage.api.storage.disk.IStorageDisk;
import com.refinedmods.refinedstorage.apiimpl.network.node.diskdrive.DiskDriveNetworkNode;
import com.refinedmods.refinedstorage.apiimpl.network.node.storage.FluidStorageNetworkNode;
import com.refinedmods.refinedstorage.apiimpl.network.node.storage.StorageNetworkNode;
import com.refinedmods.refinedstorage.blockentity.grid.portable.IPortableGrid;

import net.unfamily.rs_storage_warnings.Config;
import net.unfamily.rs_storage_warnings.RsStorageWarnings;

/**
 * Scans disk drives / storage blocks on the grid's network (and portable disks).
 * RS1 disks have amount capacity only (no AE2-style type slots).
 */
public final class DiskCapacityScanner {
    private static final ResourceLocation ITEM = new ResourceLocation("refinedstorage", "item");
    private static final ResourceLocation FLUID = new ResourceLocation("refinedstorage", "fluid");

    private DiskCapacityScanner() {
    }

    public static List<CapacityWarning> scan(@Nullable IGrid grid) {
        Aggregator aggregator = new Aggregator();
        int disksSeen = 0;

        if (grid == null) {
            return List.of();
        }

        if (grid instanceof INetworkAwareGrid networkGrid) {
            INetwork network = networkGrid.getNetwork();
            if (network != null) {
                int driveNodes = 0;
                for (INetworkNodeGraphEntry entry : network.getNodeGraph().all()) {
                    INetworkNode node = entry.getNode();
                    if (node instanceof DiskDriveNetworkNode drive) {
                        driveNodes++;
                        disksSeen += addDisks(aggregator, ITEM, drive.getItemDisks());
                        disksSeen += addDisks(aggregator, FLUID, drive.getFluidDisks());
                    } else if (node instanceof StorageNetworkNode storageNode) {
                        driveNodes++;
                        disksSeen += addDisk(aggregator, ITEM, storageNode.getStorage());
                    } else if (node instanceof FluidStorageNetworkNode fluidNode) {
                        driveNodes++;
                        disksSeen += addDisk(aggregator, FLUID, fluidNode.getStorage());
                    }
                }
                if (Config.loggingEnabled()) {
                    RsStorageWarnings.LOGGER.debug("Capacity scan: driveNodes={}, disksSeenSoFar={}", driveNodes,
                            disksSeen);
                }
            }
        }

        if (grid instanceof IPortableGrid portable) {
            IStorageDisk<?> storage = portable.getStorage();
            if (storage != null) {
                // Portable grids are item storage.
                disksSeen += addDisk(aggregator, ITEM, storage);
            }
        }

        List<CapacityWarning> warnings = aggregator.toWarnings(Config.thresholdPercent());
        if (Config.loggingEnabled()) {
            RsStorageWarnings.LOGGER.debug(
                    "Capacity scan: disksSeen={}, buckets={}, warnings={}, threshold={}%", disksSeen,
                    aggregator.buckets.size(), warnings.size(), Config.thresholdPercent());
            for (Map.Entry<ResourceLocation, Bucket> entry : aggregator.buckets.entrySet()) {
                Bucket bucket = entry.getValue();
                int rem = remainingPercent(bucket.used, bucket.total);
                RsStorageWarnings.LOGGER.debug("  bucket {} used={}/{} ({}% left)", entry.getKey(), bucket.used,
                        bucket.total, rem);
            }
        }
        return warnings;
    }

    private static int addDisks(Aggregator aggregator, ResourceLocation typeId, IStorageDisk<?>[] disks) {
        int added = 0;
        if (disks == null) {
            return 0;
        }
        for (IStorageDisk<?> disk : disks) {
            added += addDisk(aggregator, typeId, disk);
        }
        return added;
    }

    private static int addDisk(Aggregator aggregator, ResourceLocation typeId, @Nullable IStorageDisk<?> disk) {
        if (disk == null) {
            return 0;
        }
        int capacity = disk.getCapacity();
        if (capacity <= 0) {
            // Creative / unlimited.
            return 0;
        }
        long used = Math.min(disk.getStored(), capacity);
        aggregator.add(typeId, used, capacity);
        return 1;
    }

    static int remainingPercent(long used, long total) {
        if (total <= 0) {
            return 100;
        }
        long clampedUsed = Math.min(Math.max(0, used), total);
        int usedPct = (int) Math.floor(100.0 * clampedUsed / total);
        return Math.max(0, 100 - usedPct);
    }

    private static final class Aggregator {
        private final Map<ResourceLocation, Bucket> buckets = new HashMap<>();

        void add(ResourceLocation typeId, long used, long total) {
            Bucket bucket = buckets.computeIfAbsent(typeId, ignored -> new Bucket());
            bucket.used += used;
            bucket.total += total;
        }

        List<CapacityWarning> toWarnings(int threshold) {
            List<CapacityWarning> warnings = new ArrayList<>();
            for (Map.Entry<ResourceLocation, Bucket> entry : buckets.entrySet()) {
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

    private static final class Bucket {
        long used;
        long total;
    }
}
