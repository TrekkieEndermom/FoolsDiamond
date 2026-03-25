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
import io.github.trekkieendermom.foolsdiamond.util.TimeUtils;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.time.Duration;

@NullMarked
public class StatusCommand extends BrigadierCommand {
    public StatusCommand() {
        super("status", "foolsdiamond.command.status");
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> getCommand() {
        return super.getCommand()
                .executes(context -> {
                    CommandSender sender = context.getSource().getSender();
                    FoolsDiamondPlugin plugin = FoolsDiamondPlugin.getInstance();
                    if (plugin.getEnableOverride() != null) {
                        sender.sendMessage(getStatusMessage(plugin.getEnableOverride(), "Manually overridden."));
                    } else if (plugin.getConfiguration().isAlwaysEnabled()) {
                        sender.sendMessage(getStatusMessage(true, "Overridden by 'Always Enable' in config."));
                    } else if (plugin.getDateTracker().isTodayInRange()) {
                        sender.sendMessage(getStatusMessage(true, "Today's the date!"));
                        Duration dur = plugin.getDateTracker().untilEnd();
                        sender.sendMessage("Plugin's antics will continue for " + TimeUtils.getReadableDuration(dur) + ".");
                    } else {
                        sender.sendMessage(getStatusMessage(false, null));
                        Duration dur = plugin.getDateTracker().untilStart();
                        sender.sendMessage("Plugin's antics will start in " + TimeUtils.getReadableDuration(dur) + ".");
                    }
                    return Command.SINGLE_SUCCESS;
                });
    }

    private Component getStatusMessage(boolean enabled, @Nullable String reason) {
        ComponentBuilder<TextComponent, TextComponent.Builder> builder = Component.text();
        builder.append(FoolsDiamondPlugin.PREFIX).append(Component.text("Troll mode is "))
                .append(Component.text(enabled ? "enabled" : "disabled")
                        .color(enabled ? NamedTextColor.GREEN : NamedTextColor.RED))
                .append(Component.text("."));
        if (reason != null) {
            builder.appendSpace().append(Component.text("(" + reason + ")"));
        }
        return builder.build();
    }
}
