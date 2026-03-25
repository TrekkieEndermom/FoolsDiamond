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
import io.github.trekkieendermom.foolsdiamond.util.BlockFaceUtils;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.type.GlowLichen;
import org.bukkit.block.data.type.Light;

import java.util.Map;
import java.util.Set;

public class DiamondOreFactory extends AbstractFactory {
    private final Set<Material> validBodyMaterials = Set.of(Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE);
    private final Set<Material> validNeighborMaterials = Set.of(Material.AIR, Material.CAVE_AIR, Material.WATER);
    private final boolean handleLightChange;

    public DiamondOreFactory() {
        super();
        handleLightChange = FoolsDiamondPlugin.getInstance().getConfiguration().isHandleLightChange();
    }

    @Override
    public MorphingBlock build(Block block) {
        BlockState main = block.getState();
        final SimpleMorphingBlock.Builder builder = SimpleMorphingBlock.Builder.startNew(main);
        final BlockState fakeMainBlock = main.copy();
        fakeMainBlock.setType(fakeMainBlock.getType() == Material.DEEPSLATE_DIAMOND_ORE ? Material.DEEPSLATE : Material.STONE);
        builder.addFakeBlock(fakeMainBlock);
        Map<BlockFace, Block> map = getAdjacentBlocks(block.getLocation());

        // Check if the block is exposed on any side
        map.values().removeIf(neighbor -> !validNeighborMaterials.contains(neighbor.getType()));

        if (map.isEmpty()) {
            return null;
        }

        map.forEach((face, adjacentBlock) -> {
            final BlockState state = getBlockState(adjacentBlock.getLocation());
            final Material prevMaterial = state.getType();
            // Block is a flowing water
            if (prevMaterial == Material.WATER && ((Levelled) state.getBlockData()).getLevel() != 0) return;
            if (handleLightChange) {
                state.setType(Material.LIGHT);
                Light light = (Light) state.getBlockData();
                light.setLevel(7);
                light.setWaterlogged(prevMaterial == Material.WATER);
                state.setBlockData(light);
            }
            builder.addRealBlock(state.copy());
            state.setType(Material.GLOW_LICHEN);
            final GlowLichen lichen = (GlowLichen) state.getBlockData();
            lichen.setFace(face.getOppositeFace(), true);
            lichen.setWaterlogged(prevMaterial == Material.WATER);
            state.setBlockData(lichen);
            builder.addFakeBlock(state);
        });
        return builder.build();
    }

    @Override
    public Set<Material> getCandidateMaterials() {
        return validBodyMaterials;
    }

    @Override
    public boolean isValid(ChunkSnapshot chunk, int x, int y, int z) {
        if (chunk.getBlockType(x, y, z).isAir()) return false;
        for (BlockFace face : BlockFaceUtils.getCartesianFaces()) {
            BlockData neighbor = getBlockDataAt(chunk, x + face.getModX(), y + face.getModY(), z + face.getModZ());
            if (neighbor == null) continue;
            Material material = neighbor.getMaterial();
            if (validNeighborMaterials.contains(material)) {
                // Is not a water source block
                if (material == Material.WATER && ((Levelled) neighbor).getLevel() != 0) continue;
                return true;
            }
        }
        return false;
    }
}
