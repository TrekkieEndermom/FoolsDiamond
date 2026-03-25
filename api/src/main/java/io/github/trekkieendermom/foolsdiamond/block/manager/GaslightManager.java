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

package io.github.trekkieendermom.foolsdiamond.block.manager;

import io.github.trekkieendermom.foolsdiamond.block.MorphingBlock;
import io.github.trekkieendermom.foolsdiamond.util.ChunkID;
import org.bukkit.Location;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

@NullMarked
public interface GaslightManager {

    @Nullable MorphingBlock getBlockByLocation(Location location);

    Collection<MorphingBlock> getBlocksByChunk(ChunkID id);

    boolean isCached(ChunkID id);

    void loadCache();

    void clearCache();

    void ignoreChunk(ChunkID id);

    void unignoreChunk(ChunkID id);

    boolean isIgnored(ChunkID id);
}
