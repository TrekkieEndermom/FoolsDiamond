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

import io.github.trekkieendermom.foolsdiamond.block.MorphingBlock;
import org.jspecify.annotations.NullMarked;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

@NullMarked
public class ExpiringBlock implements Delayed {
    private final BlockLocation location;
    private final long expiryTime;

    public ExpiringBlock(BlockLocation location, long expiryTime) {
        this.location = location;
        this.expiryTime = System.currentTimeMillis() + expiryTime;
    }

    public ExpiringBlock(MorphingBlock block, long expiryTime) {
        this(block.getLocation(), expiryTime);
    }

    public BlockLocation location() {
        return location;
    }

    public long expiryTime() {
        return expiryTime;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        long diff = expiryTime - System.currentTimeMillis();
        return unit.convert(diff, TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        ExpiringBlock other = (ExpiringBlock) o;
        int compare = Long.compare(this.expiryTime, other.expiryTime);
        if (compare == 0) {
            compare = this.location.compareTo(other.location);
        }
        return compare;
    }

    @Override
    public int hashCode() {
        return location.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof ExpiringBlock other)) return false;
        return location.equals(other.location);
    }
}
