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
import io.github.trekkieendermom.foolsdiamond.util.ChunkID;
import io.papermc.paper.math.BlockPosition;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ChunkAnalyzer {
    private final Map<ChunkID, WeakReference<CompletableFuture<?>>> workers = new HashMap<>();
    private final Set<ChunkID> ignored = new HashSet<>();

    public ChunkAnalyzer() {
        Bukkit.getScheduler().runTaskTimer(FoolsDiamondPlugin.getInstance(), () -> {
            // Clean up
            workers.values().removeIf(ref -> ref.get() == null);
        }, 200L, 200L);
    }

    void ignore(ChunkID id) {
        ignored.add(id);
    }

    void unignore(ChunkID id) {
        ignored.remove(id);
    }

    boolean isIgnored(ChunkID id) {
        return ignored.contains(id);
    }

    public CompletableFuture<Set<Location>> readChunk(Chunk chunk) {
        final ChunkID id = ChunkID.of(chunk);
        if (isIgnored(id)) {
            return CompletableFuture.completedFuture(Set.of());
        }
        CompletableFuture<Set<BlockPosition>> future = CompletableFuture.supplyAsync(() ->
                new ChunkReader(chunk.getChunkSnapshot(true, true, false),
                        chunk.getWorld().getMinHeight(),
                        chunk.getWorld().getMaxHeight())
                        .readChunk());
        workers.put(id, new WeakReference<>(future));
        return future.thenApply(results -> results.stream()
                        .map(pos -> pos.toLocation(chunk.getWorld()))
                        .collect(Collectors.toSet()));
    }

    public void cancelTask(ChunkID id) {
        WeakReference<CompletableFuture<?>> ref = workers.remove(id);
        if (ref == null) return;
        CompletableFuture<?> worker = ref.get();
        if (worker != null && !worker.isDone()) {
            worker.cancel(true);
        }
    }

    public void cancelAllTasks() {
        Iterator<WeakReference<CompletableFuture<?>>> it = workers.values().iterator();
        while (it.hasNext()) {
            WeakReference<CompletableFuture<?>> ref = it.next();
            it.remove();
            CompletableFuture<?> worker = ref.get();
            if (worker != null && !worker.isDone()) {
                worker.cancel(true);
            }
        }
    }
}
