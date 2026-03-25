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

import io.github.trekkieendermom.foolsdiamond.FoolsDiamondPlugin;
import io.github.trekkieendermom.foolsdiamond.block.factory.MorphingBlockFactory;
import io.github.trekkieendermom.foolsdiamond.util.PositionUtils;
import io.papermc.paper.math.BlockPosition;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

@NullMarked
public class ChunkReader {
    private final FoolsDiamondPlugin plugin = FoolsDiamondPlugin.getInstance();
    private final int maxHeight;
    private final int minHeight;
    private final ChunkSnapshot chunk;

    public ChunkReader(ChunkSnapshot chunk, int minHeight, int maxHeight) {
        this.chunk = chunk;
        this.minHeight = minHeight;
        this.maxHeight = maxHeight;
    }

    /*
    For now this works fine, but in future I can look into keeping BlockData from this part of the process to pass on
    so the block factory can save time and resource on creating the morphing block. But one downside is since the
    reader operates on a snapshot of the chunk, the block data could become outdated by the time the reader finishes.
     */

    public Set<BlockPosition> readChunk() {
        final Set<Material> candidateMaterials = plugin.getBlockFactoryManager().getCandidateMaterials();
        final Set<BlockPosition> results = new HashSet<>();
        final BlockPosition chunkPosition = PositionUtils.chunkToWorldPosition(chunk);
        for (int x = 0; x < 16 ; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 17; y >= minHeight; y--) {
                    BlockData data = chunk.getBlockData(x, y, z);
                    Material material = data.getMaterial();
                    if (material.isAir()) {
                        continue;
                    }
                    if (candidateMaterials.contains(material)) {
                        final BlockPosition offset = chunkPosition.offset(x, y, z);
                        MorphingBlockFactory factory = plugin.getBlockFactoryManager().getFactory(material);
                        if (factory == null) continue;
                        try {
                            if (factory.isValid(chunk, x, y, z)) {
                                results.add(offset);
                            }
                        } catch (Exception e) {
                            plugin.getLogger().log(Level.WARNING, "Exception occurred while reading chunk at x" + chunk.getX() + ", z" + chunk.getZ(), e);
                        }
                    }
                }
            }
        }

        return results;
    }

    private @Nullable Material getMaterialAt(int x, int y, int z) {
        if (0 > x || x > 15) {
            return null;
        }
        if (0 > z || z > 15) {
            return null;
        }
        if (minHeight > y || y > maxHeight) {
            return null;
        }
        return chunk.getBlockType(x, y, z);
    }

}
