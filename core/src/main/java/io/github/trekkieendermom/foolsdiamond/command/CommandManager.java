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

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.trekkieendermom.foolsdiamond.FoolsDiamondPlugin;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CommandManager {
    private CommandManager() {
    }

    public static void registerCommand() {
        final Set<BrigadierCommand> children = new HashSet<>();
        children.add(new ReloadCommand());
        children.add(new OverrideCommand());
        children.add(new BlockDataCommand());
        children.add(new StatusCommand());

        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("foolsdiamond");
        root.requires(stack -> children.stream().anyMatch(comm -> comm.isAuthorized(stack.getSender())));
        for (BrigadierCommand child : children) {
            root.then(child.getCommand());
        }
        FoolsDiamondPlugin.getInstance().getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(root.build(), List.of("fd"));
        });
    }
}
