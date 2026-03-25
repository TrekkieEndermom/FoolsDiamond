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

import io.github.trekkieendermom.foolsdiamond.FoolsDiamondPlugin;
import io.github.trekkieendermom.foolsdiamond.util.BlockLocation;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@NullMarked
public abstract class AbstractBlock implements MorphingBlock {
    private static final Random RANDOM = new Random();
    @Getter
    private final Material type;
    @Getter
    private final BlockLocation location;
    private final Map<UUID, BlockStatus> statusMap = new HashMap<>();
    @Getter
    private boolean loaded = true;

    public AbstractBlock(final Material type, final BlockLocation location) {
        this.type = type;
        this.location = location;
    }

    private BlockStatus getBlockStatus(UUID uuid) {
        return statusMap.computeIfAbsent(uuid, key -> new BlockStatus());
    }

    @Override
    public void onEnterView(final Player player) {
        if (!loaded) {
            throw new IllegalStateException("Block is not loaded");
        }
        BlockStatus status = getBlockStatus(player.getUniqueId());
        if (!status.isSeen()) {
            status.setSeen(true);
        }
    }

    @Override
    public void onExitView(final Player player) {
        if (!loaded) {
            throw new IllegalStateException("Block is not loaded");
        }
        BlockStatus status = getBlockStatus(player.getUniqueId());
        if (!status.isSeen()) return;
        status.setSeen(false);
        if (RANDOM.nextDouble() > FoolsDiamondPlugin.getInstance().getConfiguration().getChanceToMorph()) return;
        if (status.isDisguised()) {
            status.setDisguised(false);
            player.sendBlockChanges(getRealBlocks());
        } else {
            status.setDisguised(true);
            player.sendBlockChanges(getFakeBlocks());
        }
    }

    public void load() {
        loaded = true;
    }

    public void unload() {
        statusMap.clear();
        loaded = false;
    }

    @Override
    public String toString() {
        return "AbstractBlock{" +
                "type=" + type +
                ", location=" + location +
                ", loaded=" + loaded +
                '}';
    }

    @Getter @Setter
    private static class BlockStatus {
        private boolean seen = false;
        private boolean disguised = false;
    }
}
