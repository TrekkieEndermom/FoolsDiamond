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
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public abstract class BrigadierCommand {
    @Getter
    protected final String name;
    protected final @Nullable String permission;

    public BrigadierCommand(String name) {
        this(name, null);
    }

    public BrigadierCommand(String name, @Nullable String permission) {
        if (name.isBlank()) {
            throw new IllegalArgumentException("Command name couldn't be blank.");
        }
        this.name = name;
        if (permission != null && permission.isBlank()) {
            permission = null;
        }
        this.permission = permission;
    }

    public LiteralArgumentBuilder<CommandSourceStack> getCommand() {
        return Commands.literal(name)
                .requires(stack -> isAuthorized(stack.getSender()))
                .executes(context -> {
                    showUsage(context.getSource().getSender());
                    return Command.SINGLE_SUCCESS;
                });
    }

    public boolean isAuthorized(final CommandSender sender) {
        if (permission == null) return true;
        return sender.hasPermission(permission);
    }

    public void showUsage(CommandSender sender) {
        sender.sendMessage("This command isn't implemented yet.");
    }
}