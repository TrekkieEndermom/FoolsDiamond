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

package io.github.trekkieendermom.foolsdiamond.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.trekkieendermom.foolsdiamond.FoolsDiamondPlugin;
import io.github.trekkieendermom.foolsdiamond.block.MorphingBlock;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.BlockPositionResolver;
import io.papermc.paper.math.BlockPosition;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

import java.util.Locale;

@NullMarked
public class BlockDataCommand extends  BrigadierCommand {
    private final static String POSITION_ARG = "pos";

    public BlockDataCommand() {
        super("getData", "foolsdiamond.command.getdata");
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> getCommand() {
        return super.getCommand()
                .executes(context -> {
                    // get block the player is looking at
                    Player player = (Player) context.getSource().getSender();
                    final Block block = player.getTargetBlockExact(20);
                    if (block == null) {
                        player.sendMessage(FoolsDiamondPlugin.PREFIX
                                .append(Component.text("You aren't looking at a block within a distance of 20 blocks.")));
                        return Command.SINGLE_SUCCESS;
                    }
                    run(player, block.getLocation());
                    return Command.SINGLE_SUCCESS;
                })
                .then(Commands.argument(POSITION_ARG, ArgumentTypes.blockPosition())
                        .executes(context -> {
                            final BlockPositionResolver resolver = context.getArgument(POSITION_ARG, BlockPositionResolver.class);
                            BlockPosition position = resolver.resolve(context.getSource());
                            Player player = (Player) context.getSource().getSender();
                            run(player, position.toLocation(player.getWorld()));
                            return Command.SINGLE_SUCCESS;
                        }));
    }

    @Override
    public boolean isAuthorized(final CommandSender sender){
        if (!(sender instanceof Player)) return false;
        return super.isAuthorized(sender);
    }

    private void run(Player player, Location location) {
        final MorphingBlock block = FoolsDiamondPlugin.getInstance().getGaslightManager().getBlockByLocation(location);
        if (block == null) {
            player.sendMessage(FoolsDiamondPlugin.PREFIX.append(Component.text("There is no morphing block at this location.")));
            return;
        }
        player.sendMessage(FoolsDiamondPlugin.PREFIX
                .append(Component.text("Properties of morphing block at " + location.toVector())));
        player.sendMessage(Component.text().append(Component.text("Type: ").color(NamedTextColor.DARK_AQUA))
                .append(translateMaterial(block.getType())));
        player.sendMessage(Component.text().append(Component.text("Marked as loaded: ").color(NamedTextColor.DARK_AQUA))
                .append(Component.text(block.isLoaded())));
        player.sendMessage(Component.text().append(Component.text("Is valid: ").color(NamedTextColor.DARK_AQUA))
                .append(Component.text(block.validate())));
        player.sendMessage(Component.text("List of real blocks: ").color(NamedTextColor.DARK_AQUA));
        block.getRealBlocks().forEach(state -> player.sendMessage(outputBlockState(state)));
        player.sendMessage(Component.text("List of fake blocks: ").color(NamedTextColor.DARK_AQUA));
        block.getFakeBlocks().forEach(state -> player.sendMessage(outputBlockState(state)));
    }

    private Component outputBlockState(BlockState state) {
        return Component.text("- ")
                .append(translateMaterial(state.getType()))
                .appendSpace()
                .append(Component.text("at"))
                .appendSpace()
                .append(Component.text(state.getLocation().toVector().toString()));
    }

    private Component translateMaterial(Material material) {
        return GlobalTranslator.render(Component.translatable(material.translationKey()), Locale.ENGLISH);
    }
}
