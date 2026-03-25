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

import io.github.trekkieendermom.foolsdiamond.block.MorphingBlock;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BlockFactoryManager {
    private final Map<Material, AbstractFactory> factoryMap = new ConcurrentHashMap<>();

    public void register(AbstractFactory factory) {
        if (factory == null) return;
        for (Material material : factory.getCandidateMaterials()) {
            if (material.isAir()) continue;
            factoryMap.put(material, factory);
        }
    }

    public @Nullable MorphingBlock createBlock(final Block block) {
        if (block == null || block.getType().isAir()) {
            return null;
        }
        final AbstractFactory factory = factoryMap.get(block.getType());
        if (factory != null) {
            return factory.build(block);
        }
        return null;
    }

    public @Nullable MorphingBlockFactory getFactory(Material material) {
        return factoryMap.get(material);
    }

    /**
     * Gets a set of Materials that can be used for gaslighting players. The returned set is immutable and thread-safe.
     * @return material set.
     */
    public Set<Material> getCandidateMaterials() {
        return Set.copyOf(factoryMap.keySet());
    }
}
