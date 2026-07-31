# RS Storage Warnings

Warns you when Refined Storage disk capacity is critically low **before** a grid opens.

When you open a grid (wired, wireless, crafting, or portable), the mod scans storage **disks** and **storage blocks** on that network. If remaining capacity for a resource type is critically low, a **WARNING!** dialog appears instead of opening the grid right away.

You can ignore the warning once and continue into the grid, or close it and free space first.

## What is checked

- Disks in disk drives and placed storage blocks (not external storage)
- Portable grid disk (when opening a portable grid)
- Remaining capacity aggregated per resource channel (item, fluid, and addon types)

## Per-type toggles

From any grid, use the warning triangle side button to enable or disable capacity warnings per resource type. Choices are saved per player.
