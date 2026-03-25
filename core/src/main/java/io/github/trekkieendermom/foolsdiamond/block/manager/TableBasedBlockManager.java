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

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import io.github.trekkieendermom.foolsdiamond.FoolsDiamondPlugin;
import io.github.trekkieendermom.foolsdiamond.block.MorphingBlock;
import io.github.trekkieendermom.foolsdiamond.util.*;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;

@NullMarked
public class TableBasedBlockManager implements GaslightManager {
    private final Table<ChunkID, BlockLocation, MorphingBlock> table = HashBasedTable.create();
    @Getter
    private final NavigableSet<ExpiringChunk> expiringChunks = new ConcurrentSkipListSet<>();
    private final NavigableSet<ExpiringBlock> blocksToRefresh = new ConcurrentSkipListSet<>();
    private final FoolsDiamondPlugin plugin;
    private final ChunkAnalyzer analyzer;

    public TableBasedBlockManager() {
        this.plugin = FoolsDiamondPlugin.getInstance();
        this.analyzer = new ChunkAnalyzer();
        Bukkit.getPluginManager().registerEvents(new BlockListener(), plugin);
        Bukkit.getScheduler().runTaskTimer(plugin, new CleanUpTask(), 20, 20);
    }

    @Override
    public @Nullable MorphingBlock getBlockByLocation(Location location) {
        BlockLocation loc = BlockLocation.of(location);
        return table.get(loc.chunkID(), loc);
    }

    @Override
    public Collection<MorphingBlock> getBlocksByChunk(ChunkID id) {
        /*
         Should this return a copy of set or an unmodifiable collection view?

         Returning an unmodifiable view seems far better for performance than making a full immutable copy
         when called frequently in a short amount of time. However, initiating an iterator for an unmodifiable view
         is noticeably slower than initiating one for a copy. Interesting.
         Another thing to note with unmodifiable collection is if it is iterated over while being modified
         concurrently, ConcurrentModificationException may occur.
         */
        return Collections.unmodifiableCollection(table.row(id).values());
    }

    @Override
    public boolean isCached(ChunkID id) {
        return table.rowKeySet().contains(id);
    }

    private void addMorphingBlocks(Set<Location> locations) {
        if (locations.isEmpty()) {
            return;
        }
        locations.forEach(this::addMorphingBlock);
    }

    private void addMorphingBlock(Location location) {
        ChunkID id = ChunkID.of(location);
        MorphingBlock block = plugin.getBlockFactoryManager().createBlock(location.getBlock());
        if (block == null) return;
        table.put(id, block.getLocation(), block);
        ExpiringBlock expiringBlock = new ExpiringBlock(block, plugin.getConfiguration().getTtlAfterRefresh());
        blocksToRefresh.remove(expiringBlock);
        blocksToRefresh.add(expiringBlock);
    }

    @Override
    public void ignoreChunk(ChunkID id) {
        analyzer.ignore(id);
    }

    @Override
    public void unignoreChunk(ChunkID id) {
        analyzer.unignore(id);
    }

    @Override
    public boolean isIgnored(ChunkID id) {
        return analyzer.isIgnored(id);
    }

    @Override
    public void loadCache() {
        for (World world : Bukkit.getWorlds()) {
            if (plugin.getConfiguration().isWhitelisted(world)) {
                Iterator<Chunk> it = Arrays.stream(world.getLoadedChunks()).filter(chunk -> !isCached(ChunkID.of(chunk))).iterator();
                if (!it.hasNext()) continue;
                FoolsDiamondPlugin.getInstance().getLogger().info("Started caching all loaded chunks.");
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        long target = System.currentTimeMillis() + 3;
                        while (it.hasNext()) {
                            Chunk chunk = it.next();
                            if (!chunk.isLoaded()) continue;
                            readChunk(chunk);
                            // Over the time limit, resume a couple of ticks later.
                            if (System.currentTimeMillis() > target && it.hasNext()) {
                                Bukkit.getScheduler().runTaskLater(FoolsDiamondPlugin.getInstance(), this, 3L);
                                break;
                            }
                        }
                        if (!it.hasNext()) {
                            FoolsDiamondPlugin.getInstance().getLogger().info("Done caching all loaded chunks.");
                        }
                    }
                };
                Bukkit.getScheduler().runTask(FoolsDiamondPlugin.getInstance(), runnable);
//                Arrays.stream(world.getLoadedChunks()).forEach(this::readChunk);
            }
        }
    }

    @Override
    public void clearCache() {
        analyzer.cancelAllTasks();
        expiringChunks.clear();
        blocksToRefresh.clear();
        table.values().forEach(MorphingBlock::unload);
        table.clear();
    }

    private void readChunk(Chunk chunk) {
        if (isCached(ChunkID.of(chunk))) return;
        analyzer.readChunk(chunk)
                .thenAcceptAsync(this::addMorphingBlocks, Bukkit.getScheduler().getMainThreadExecutor(plugin));
    }

    private class CleanUpTask implements Runnable {

        @Override
        public void run() {
            // Clears expired chunks
            Iterator<ExpiringChunk> it = expiringChunks.iterator();
            while (it.hasNext()) {
                ExpiringChunk chunk = it.next();
                if (chunk.getDelay(TimeUnit.MILLISECONDS) > 0) {
                    break;
                }
                it.remove();
                table.row(chunk.id()).clear();
            }

            // Refreshes morphing blocks
            Iterator<ExpiringBlock> iterator = blocksToRefresh.iterator();
            List<ExpiringBlock> toAdd = new ArrayList<>();
            while (iterator.hasNext()) {
                ExpiringBlock expiringBlock = iterator.next();
                if (expiringBlock.getDelay(TimeUnit.MILLISECONDS) > 0) {
                    break;
                }
                iterator.remove();
                final BlockLocation location = expiringBlock.location();
                final ChunkID chunkID = location.chunkID();
                final MorphingBlock block = table.get(chunkID, location);
                if (block == null) continue;
                if (block.isLoaded() && !block.validate()) {
                    table.remove(chunkID, location);
                    addMorphingBlock(location.toBukkitLocation());
                    continue;
                }
                toAdd.add(new ExpiringBlock(block, plugin.getConfiguration().getTtlAfterRefresh()));
            }
            if (!toAdd.isEmpty()) {
                blocksToRefresh.addAll(toAdd);
            }
        }
    }

    private class BlockListener implements Listener {

        @EventHandler
        public void onChunkLoad(ChunkLoadEvent event) {
            if (!plugin.isTrollTime()) return;
            ChunkID id = ChunkID.of(event.getChunk());
            ExpiringChunk chunk = new ExpiringChunk(id, 0);
            expiringChunks.remove(chunk);
            if (isCached(id)) {
                table.row(id).values().forEach(MorphingBlock::load);
            } else if (plugin.getConfiguration().isWhitelisted(event.getWorld())) {
                readChunk(event.getChunk());
            }
        }

        @EventHandler
        public void onChunkUnload(ChunkUnloadEvent event) {
            ChunkID id = ChunkID.of(event.getChunk());
            analyzer.cancelTask(id);
            if (!isCached(id)) return;
            table.row(id).values().forEach(MorphingBlock::unload);
            long afterUnload = plugin.getConfiguration().getTtlAfterUnload();
            if (afterUnload == 0) {
                // Immediately clear the chunk cache.
                table.row(id).clear();
                return;
            }
            ExpiringChunk chunk = new ExpiringChunk(id, afterUnload);
            expiringChunks.remove(chunk); // Remove the previous entry if there is any
            expiringChunks.add(chunk);
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onBlockBreak(BlockBreakEvent event) {
            onBlockChange(event.getBlock().getLocation());
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onBlockPlace(BlockPlaceEvent event) {
            onBlockChange(event.getBlockPlaced().getLocation());
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onBlockExplode(BlockExplodeEvent event) {
            onBlockChange(event.getBlock().getLocation());
        }

        // Piston push/pull events?

        private void onBlockChange(Location loc) {
            if (!plugin.getConfiguration().isRefreshOnBlockChange()) return;
            if (!plugin.getConfiguration().isWhitelisted(loc.getWorld())) return;
            BlockLocation location = BlockLocation.of(loc);
            ChunkID id = location.chunkID();
            MorphingBlock block = table.remove(id, location);
            if (block != null) {
                block.unload();
            }

            // Update adjacent morphing blocks
            List<Location> toUpdate = new ArrayList<>();
            for (BlockFace face : BlockFaceUtils.getCartesianFaces()) {
                BlockLocation adjacentLoc = location.offset(face);
                MorphingBlock neighbor = table.get(adjacentLoc.chunkID(), adjacentLoc);
                if (neighbor == null || neighbor.validate()) continue;
                table.remove(adjacentLoc.chunkID(), adjacentLoc);
                neighbor.unload();
                ExpiringBlock expiringBlock = new ExpiringBlock(neighbor, 0);
                blocksToRefresh.remove(expiringBlock);
                toUpdate.add(adjacentLoc.toBukkitLocation());
            }

            if (!toUpdate.isEmpty()) {
                Bukkit.getScheduler().runTask(plugin, () ->
                        toUpdate.forEach(TableBasedBlockManager.this::addMorphingBlock));
            }
        }
    }
}
