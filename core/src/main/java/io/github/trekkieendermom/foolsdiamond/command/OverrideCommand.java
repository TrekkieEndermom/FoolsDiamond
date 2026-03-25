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
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.trekkieendermom.foolsdiamond.FoolsDiamondPlugin;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class OverrideCommand extends BrigadierCommand {
    private final static String BOOL_ARG = "enable";

    public OverrideCommand() {
        super("override", "foolsdiamond.command.override");
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> getCommand() {
        return super.getCommand()
                .executes(context -> {
                    Boolean override = FoolsDiamondPlugin.getInstance().getEnableOverride();
                    CommandSender sender = context.getSource().getSender();
                    if (override != null) {
                        sender.sendMessage(Component.text().append(FoolsDiamondPlugin.PREFIX)
                                .append(Component.text("Manual override is currently set to "))
                                .append(Component.text(override).color(override ? NamedTextColor.GREEN : NamedTextColor.RED)));
                        sender.sendMessage("The override may be removed with /fd override clear.");
                    } else {
                        sender.sendMessage(Component.text().append(FoolsDiamondPlugin.PREFIX)
                                .append(Component.text("Manual override is unset.")));
                    }
                    return Command.SINGLE_SUCCESS;
                })
                .then(Commands.literal("clear")
                        .executes(context -> {
                            FoolsDiamondPlugin.getInstance().setEnableOverride(null);
                            context.getSource().getSender().sendMessage(getResultMessage());
                            return Command.SINGLE_SUCCESS;
                        }))
                .then(Commands.argument(BOOL_ARG, BoolArgumentType.bool())
                        .executes(context -> {
                            boolean enable = BoolArgumentType.getBool(context, BOOL_ARG);
                            FoolsDiamondPlugin.getInstance().setEnableOverride(enable);
                            CommandSender sender = context.getSource().getSender();
                            sender.sendMessage(getResultMessage());
                            sender.sendMessage("The override will persist until server restart.");
                            return Command.SINGLE_SUCCESS;
                        }));
    }

    private Component getResultMessage() {
        Boolean enable = FoolsDiamondPlugin.getInstance().getEnableOverride();
        return Component.text().append(FoolsDiamondPlugin.PREFIX)
                .append(Component.text("Troll mode is now "))
                .append(Component.text(enable == null ? "no longer manually overridden" : (enable ? "enabled" : "disabled"))
                        .color(enable == null ? NamedTextColor.GRAY : (enable ? NamedTextColor.GREEN : NamedTextColor.RED)))
                .append(Component.text("."))
                .build();
    }
}
