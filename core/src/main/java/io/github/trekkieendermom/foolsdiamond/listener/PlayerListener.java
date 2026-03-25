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

package io.github.trekkieendermom.foolsdiamond.listener;

import io.github.trekkieendermom.foolsdiamond.FoolsDiamondPlugin;
import io.github.trekkieendermom.foolsdiamond.block.MorphingBlock;
import io.github.trekkieendermom.foolsdiamond.util.BlockLocation;
import io.github.trekkieendermom.foolsdiamond.util.ChunkID;
import io.github.trekkieendermom.foolsdiamond.util.Vector;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerListener implements Listener {

    /**
     * Listen to player's location and head movement to determine when to start gaslighting them
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        final Player player = event.getPlayer();
        if (!FoolsDiamondPlugin.getInstance().isTrollTime()) {
            return;
        }
        if (player.hasPermission("foolsdiamond.exclude")) {
            return;
        }
        if (!FoolsDiamondPlugin.getInstance().getConfiguration().isWhitelisted(player.getWorld())) {
            return;
        }
        final Location to = event.getTo().clone().add(0, player.getEyeHeight(), 0);
        final Chunk playerChunk = player.getChunk();
        for (Chunk chunk : player.getSentChunks()) {
            final int distance = getChunkDistanceSquared(playerChunk, chunk);
            // More than 4 chunks away
            if (distance > 16) continue;
            for (final MorphingBlock block : FoolsDiamondPlugin.getInstance().getGaslightManager().getBlocksByChunk(ChunkID.of(chunk))) {
                if (isInView(to, block.getLocation())) {
                    block.onEnterView(player);
                } else {
                    block.onExitView(player);
                }
            }
        }
    }

    private int getChunkDistanceSquared(final Chunk first, final Chunk second) {
        return (first.getX() - second.getX())^2 + (first.getZ() - second.getZ())^2;
    }

    private boolean isInView(final Location head, final BlockLocation block) {
        if (!head.getWorld().getUID().equals(block.worldUID())) return false;
        final Vector eyeVec = Vector.of(head);
        final Vector blockRelative = block.getBlockCenter().subtract(eyeVec);
        final double distance = blockRelative.getLength();
        int degrees;
        // Dirty hack to prevent weirdness that occurs when a player's head is inside a non-solid morphing block
        if (distance <= 0.5) {
            return true;
        }
        if (distance < 1.5) { // less than 1.5 blocks away
            degrees = 120;
        }
        else if (distance < 5) { // 1.5 to 5 blocks away
            degrees = 90;
        } else { // Over 6 blocks away
            degrees = 75;
        }

        final Vector headDirection = new Vector(head.getYaw(), head.getPitch());
        return headDirection.angle(blockRelative) < degrees;
    }
}
