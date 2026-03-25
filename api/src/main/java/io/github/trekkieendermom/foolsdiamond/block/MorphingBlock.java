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
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

import java.util.Collection;

/**
Represents a block that can disguise itself as another block when a player isn't looking.
 */
@NullMarked
public interface MorphingBlock {
    Material getType();

    BlockLocation getLocation();

    Collection<BlockState> getRealBlocks();

    Collection<BlockState> getFakeBlocks();

    boolean isPartOfBlock(BlockLocation location);

    void onEnterView(Player player);

    void onExitView(Player player);

    boolean validate();

    boolean isLoaded();

    void load();

    void unload();
}
