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
import io.github.trekkieendermom.foolsdiamond.block.MorphingBlock;
import io.github.trekkieendermom.foolsdiamond.block.SimpleMorphingBlock;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.GlowLichen;
import org.bukkit.block.data.type.Light;

import java.util.Collection;
import java.util.Set;

public class GlowLichenFactory extends AbstractFactory {
    private final Set<Material> materials = Set.of(Material.GLOW_LICHEN);
    private final Set<Material> neighboringMaterials = Set.of(Material.STONE, Material.DEEPSLATE);
    private final boolean handleLightChange;

    public GlowLichenFactory() {
        super();
        handleLightChange = FoolsDiamondPlugin.getInstance().getConfiguration().isHandleLightChange();
    }

    @Override
    public MorphingBlock build(Block block) {
        final BlockState main = block.getState();
        final SimpleMorphingBlock.Builder builder = SimpleMorphingBlock.Builder.startNew(main);
        final BlockState fakeMain = main.copy();
        final GlowLichen lichen = (GlowLichen) main.getBlockData();
        if (handleLightChange) {
            fakeMain.setType(Material.LIGHT);
            Light light = (Light) fakeMain.getBlockData();
            light.setLevel(7);
            light.setWaterlogged(lichen.isWaterlogged());
            fakeMain.setBlockData(light);
        } else {
            fakeMain.setType(lichen.isWaterlogged() ? Material.WATER : Material.CAVE_AIR);
        }
        builder.addFakeBlock(fakeMain);

        for (BlockFace face : lichen.getFaces()) {
            final Location location = block.getLocation().clone().add(face.getModX(), face.getModY(), face.getModZ());
            final Block neighbor = location.getBlock();
            if (!neighboringMaterials.contains(neighbor.getType())) continue;
            final BlockState neighborState = getBlockState(location);
            builder.addRealBlock(neighborState.copy());
            neighborState.setType(neighborState.getType() == Material.DEEPSLATE ? Material.DEEPSLATE_DIAMOND_ORE : Material.DIAMOND_ORE);
            builder.addFakeBlock(neighborState);
        }

        return builder.build();
    }

    @Override
    public Collection<Material> getCandidateMaterials() {
        return materials;
    }

    @Override
    public boolean isValid(ChunkSnapshot chunk, int x, int y, int z) {
        if (chunk.getBlockType(x, y, z).isAir()) return false;
        if (!(chunk.getBlockData(x, y, z) instanceof GlowLichen lichen)) return false;
        for (BlockFace face : lichen.getFaces()) {
            BlockData data = getBlockDataAt(chunk, x + face.getModX(), y + face.getModY(), z + face.getModZ());
            if (data == null) continue;
            Material neighbor = data.getMaterial();
            if (neighboringMaterials.contains(neighbor)) return true;
        }
        return false;
    }
}
