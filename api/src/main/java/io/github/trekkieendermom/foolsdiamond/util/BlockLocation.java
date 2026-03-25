/*
 * Copyright (c) 2026 TrekkieEndermom
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.trekkieendermom.foolsdiamond.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.jspecify.annotations.NullMarked;

import java.util.Objects;
import java.util.UUID;

/**
 * Similar to {@link Location} but immutable and thread safe. The world is stored by its UID rather than a weak reference to it.
 */
@NullMarked
public final class BlockLocation implements Comparable<BlockLocation> {
    private final UUID worldUID;
    private final String worldName;
    private final int x;
    private final int y;
    private final int z;
    // For convenience
    private final ChunkID chunkID;

    public BlockLocation(UUID worldUID, String worldName, int x, int y, int z) {
        this.worldUID = worldUID;
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.chunkID = new ChunkID(worldUID, worldName, x >> 4, z >> 4);
    }


    public static BlockLocation of(Location location) {
        return new BlockLocation(location.getWorld().getUID(), location.getWorld().getName(),
                location.blockX(), location.blockY(), location.blockZ());
    }

    public static BlockLocation of(ChunkID chunkID, int x, int y, int z) {
        return new BlockLocation(chunkID.worldUID(), chunkID.worldName(), x, y, z);
    }

    public UUID worldUID() {
        return worldUID;
    }

    public String worldName() {
        return worldName;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int z() {
        return z;
    }

    public ChunkID chunkID() {
        return chunkID;
    }

    public Vector toVector() {
        return new Vector(x, y, z);
    }

    /**
     * Coverts the BlockLocation to {@link Location}. The world field
     * might be null if there is no world associated with the stored UID.
     *
     * @return a new Location instance.
     */
    public Location toBukkitLocation() {
        return new Location(Bukkit.getWorld(worldUID), x, y, z);
    }

    public Vector getBlockCenter() {
        return new Vector(x + 0.5d, y + 0.5d, z + 0.5d);
    }

    public BlockLocation add(int x, int y, int z) {
        return new BlockLocation(worldUID, worldName, this.x + x, this.y + y, this.z + z);
    }

    public BlockLocation add(BlockLocation other) {
        if (!this.worldUID.equals(other.worldUID)) {
            throw new IllegalStateException("Cannot add locations from two different worlds");
        }
        return add(other.x, other.y, other.z);
    }

    public BlockLocation subtract(int x, int y, int z) {
        return new BlockLocation(worldUID, worldName, this.x - x, this.y - y, this.z - z);
    }

    public BlockLocation subtract(BlockLocation other) {
        if (!this.worldUID.equals(other.worldUID)) {
            throw new IllegalStateException("Cannot subtract locations from two different worlds");
        }
        return subtract(other.x, other.y, other.z);
    }

    public BlockLocation offset(BlockFace face) {
        return add(face.getModX(), face.getModY(), face.getModZ());
    }

    public BlockLocation setWorld(World world) {
        return new BlockLocation(world.getUID(), world.getName(), x, y, z);
    }

    @Override
    public int compareTo(BlockLocation other) {
        int compare = this.worldUID.compareTo(other.worldUID);
        if (compare == 0) {
            compare = Integer.compare(this.x, other.x);
        }
        if (compare == 0) {
            compare = Integer.compare(this.y, other.y);
        }
        if (compare == 0) {
            compare = Integer.compare(this.z, other.z);
        }
        return compare;
    }

    public String asReadableString() {
        return worldName + " x" + x + " y" + y + " z" + z;
    }

    @Override
    public String toString() {
        return "BlockLocation{" +
                "worldName=" + worldName +
                ", worldUID=" + worldUID +
                ", x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof BlockLocation other)) return false;
        return x == other.x && y == other.y && z == other.z && this.worldUID.equals(other.worldUID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x * 31, y * 31, z * 31, worldUID);
    }

}
