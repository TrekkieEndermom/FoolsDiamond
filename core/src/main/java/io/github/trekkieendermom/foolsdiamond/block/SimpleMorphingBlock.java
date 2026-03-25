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

package io.github.trekkieendermom.foolsdiamond.block;

import io.github.trekkieendermom.foolsdiamond.util.BlockLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.jspecify.annotations.NullMarked;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@NullMarked
public class SimpleMorphingBlock extends AbstractBlock {
    private final Set<BlockState> realBlocks;
    private final Set<BlockState> fakeBlocks;

    public SimpleMorphingBlock(Material type, BlockLocation location, Set<BlockState> realBlocks, Set<BlockState> fakeBlocks) {
        super(type, location);
        this.realBlocks = Set.copyOf(realBlocks);
        this.fakeBlocks = Set.copyOf(fakeBlocks);
    }

    @Override
    public Collection<BlockState> getRealBlocks() {
        return realBlocks;
    }

    @Override
    public Collection<BlockState> getFakeBlocks() {
        return fakeBlocks;
    }

    @Override
    public boolean isPartOfBlock(BlockLocation location) {
        if (!location.worldUID().equals(this.getLocation().worldUID())) return false;
        for (BlockState state : realBlocks) {
            Location stateLoc = state.getLocation();
            if (stateLoc.blockX() == location.x() &&
                    stateLoc.blockY() == location.y() &&
                    stateLoc.blockZ() == location.z()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean validate() {
        if (!isLoaded()) {
            // Just return true in case of unloaded chunk. The block could always be checked later if it's still cached.
            return true;
        }
        World world = Bukkit.getWorld(getLocation().worldUID());
        if (world == null) return false;
        for (BlockState state : realBlocks) {
            // Unplaced BlockState may return null for world in the returned location, so I need to set the world manually.
            Location location = state.getLocation();
            location.setWorld(world);
            if (location.getBlock().getType() != state.getType()) return false;
            // I can also check the block data on both, but the chance of the block changing only its data is slim,
            // and getting data on the actual block may cause a chunk load. Overall not worth the effort.
        }
        return true;
    }



    @NullMarked
    public static class Builder {
        private final Material blockType;
        private final BlockLocation origin;
        private final Set<BlockState> realBlocks = new HashSet<>();
        private final Set<BlockState> fakeBlocks = new HashSet<>();

        private Builder(BlockState mainBody) {
            blockType = mainBody.getType();
            origin = BlockLocation.of(mainBody.getLocation());
            realBlocks.add(mainBody);
        }

        public static Builder startNew(BlockState mainBody) {
            return new Builder(mainBody);
        }

        public Builder addRealBlock(BlockState blockState) {
            realBlocks.add(blockState);
            return this;
        }

        public Builder addFakeBlock(BlockState blockState) {
            fakeBlocks.add(blockState);
            return this;
        }

        public SimpleMorphingBlock build() {
            return new SimpleMorphingBlock(blockType, origin, realBlocks, fakeBlocks);
        }
    }
}
