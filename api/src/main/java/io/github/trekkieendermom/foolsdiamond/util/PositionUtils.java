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

import io.papermc.paper.math.BlockPosition;
import io.papermc.paper.math.Position;
import lombok.experimental.UtilityClass;
import org.bukkit.ChunkSnapshot;
import org.jspecify.annotations.NullMarked;

@NullMarked
@UtilityClass
public class PositionUtils {
    public static BlockPosition chunkToWorldPosition(ChunkSnapshot chunk) {
        return Position.block(chunk.getX() * 16, 0 , chunk.getZ() * 16);
    }
}
