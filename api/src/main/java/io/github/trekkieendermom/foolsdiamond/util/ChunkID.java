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

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.jspecify.annotations.NullMarked;

import java.util.Objects;
import java.util.UUID;

@NullMarked
public record ChunkID(UUID worldUID, String worldName, int x, int z) implements Comparable<ChunkID> {
    public static ChunkID of(Chunk chunk) {
        return new ChunkID(chunk.getWorld().getUID(), chunk.getWorld().getName() , chunk.getX(), chunk.getZ());
    }

    public static ChunkID of(World world, int chunkX, int chunkZ) {
        return new ChunkID(world.getUID(), world.getName(), chunkX,  chunkZ);
    }

    public static ChunkID of(Location location) {
        if (location.getWorld() == null) {
            throw new IllegalArgumentException("Location's world is null");
        }
        return new ChunkID(location.getWorld().getUID(), location.getWorld().getName(), location.getChunk().getX(), location.getChunk().getZ());
    }

    @Override
    public int compareTo(ChunkID other) {
        int compare = this.worldUID.compareTo(other.worldUID);
        if (compare == 0) {
            compare = Integer.compare(this.x, other.x);
        }
        if (compare == 0) {
            compare = Integer.compare(this.z, other.z);
        }
        return compare;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof ChunkID chunkID)) return false;
        return x == chunkID.x && z == chunkID.z && Objects.equals(worldUID, chunkID.worldUID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x * 57 << 4, z * 57 << 4, worldUID);
    }
}
