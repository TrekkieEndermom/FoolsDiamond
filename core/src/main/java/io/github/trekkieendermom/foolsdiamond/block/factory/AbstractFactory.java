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

package io.github.trekkieendermom.foolsdiamond.block.factory;

import io.github.trekkieendermom.foolsdiamond.FoolsDiamondPlugin;
import io.github.trekkieendermom.foolsdiamond.util.BlockFaceUtils;
import io.github.trekkieendermom.foolsdiamond.util.ChunkID;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@NullMarked
public abstract class AbstractFactory implements MorphingBlockFactory {

    protected @Nullable BlockData getBlockDataAt(ChunkSnapshot chunk, int x, int y, int z) {
        if (isOutOfBound(x) || isOutOfBound(z)) {
            return null;
        }
        return (chunk.getBlockData(x, y, z));
    }

    protected boolean isOutOfBound(int i) {
        return 0 > i || i > 15;
    }

    protected BlockState getBlockState(final Location location) {
        final ChunkID id = ChunkID.of(location);
        // Getting a copy of block state in an unloaded chunk would trigger a chunk load event, so I need to
        // tell the chunk listener to ignore the event for this specific chunk.
        // Otherwise, we could easily get stuck in an endless loop of two chunks loading each other.
        if (!location.isChunkLoaded()) {
            FoolsDiamondPlugin.getInstance().getGaslightManager().ignoreChunk(id);
        }
        final BlockState state = location.getBlock().getState();
        FoolsDiamondPlugin.getInstance().getGaslightManager().unignoreChunk(id);
        return state;
    }

    protected Map<BlockFace, Block> getAdjacentBlocks(final Location location) {
        final Map<BlockFace, Block> map = new HashMap<>();
        for (BlockFace face : BlockFaceUtils.getCartesianFaces()) {
            map.put(face, location.clone().add(face.getModX(), face.getModY(), face.getModZ()).getBlock());
        }
        return map;
    }
}
