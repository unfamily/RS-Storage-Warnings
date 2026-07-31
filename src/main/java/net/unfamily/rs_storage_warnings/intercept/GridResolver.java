package net.unfamily.rs_storage_warnings.intercept;

import java.lang.reflect.Field;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.MenuProvider;

import com.refinedmods.refinedstorage.common.api.grid.Grid;

/**
 * Resolves a {@link Grid} from a menu provider.
 * Wired grids implement {@link Grid} directly; wireless/portable wrap a Grid field.
 */
public final class GridResolver {
    private GridResolver() {
    }

    @Nullable
    public static Grid resolve(MenuProvider menuProvider) {
        if (menuProvider instanceof Grid grid) {
            return grid;
        }

        Class<?> current = menuProvider.getClass();
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                if (Grid.class.isAssignableFrom(field.getType())) {
                    try {
                        field.setAccessible(true);
                        Object value = field.get(menuProvider);
                        if (value instanceof Grid grid) {
                            return grid;
                        }
                    } catch (IllegalAccessException ignored) {
                    }
                }
            }
            current = current.getSuperclass();
        }
        return null;
    }
}
