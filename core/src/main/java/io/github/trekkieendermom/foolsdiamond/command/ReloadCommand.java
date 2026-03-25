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
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class ReloadCommand extends BrigadierCommand {
    public ReloadCommand() {
        super("reload", "foolsdiamond.command.reload");
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> getCommand() {
        return super.getCommand()
                .executes(context -> {
                    FoolsDiamondPlugin.getInstance().reloadConfiguration();
                    context.getSource().getSender().sendMessage(FoolsDiamondPlugin.PREFIX.append(Component.text("Config reloaded!")));
                    return Command.SINGLE_SUCCESS;
                });
    }
}
