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

package io.github.trekkieendermom.foolsdiamond;

import io.github.trekkieendermom.foolsdiamond.block.factory.BlockFactoryManager;
import io.github.trekkieendermom.foolsdiamond.block.factory.DiamondOreFactory;
import io.github.trekkieendermom.foolsdiamond.block.factory.GlowLichenFactory;
import io.github.trekkieendermom.foolsdiamond.block.manager.GaslightManager;
import io.github.trekkieendermom.foolsdiamond.block.manager.TableBasedBlockManager;
import io.github.trekkieendermom.foolsdiamond.command.CommandManager;
import io.github.trekkieendermom.foolsdiamond.listener.PlayerListener;
import io.github.trekkieendermom.foolsdiamond.util.DateRange;
import io.github.trekkieendermom.foolsdiamond.util.TimeUtils;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public final class FoolsDiamondPlugin extends JavaPlugin {
    @Getter
    private static FoolsDiamondPlugin instance;
    @Getter
    private GaslightManager gaslightManager;
    @Getter
    private BlockFactoryManager blockFactoryManager;
    private ConfigManager configManager;
    @Getter @Setter
    private @Nullable Boolean enableOverride = null;
    public final static Component PREFIX = Component.text()
        .append(Component.text("[").color(NamedTextColor.DARK_AQUA))
        .append(Component.text("Fools").color(NamedTextColor.WHITE))
        .append(Component.text("Diamond").color(NamedTextColor.AQUA))
        .append(Component.text("]").color(NamedTextColor.DARK_AQUA))
        .appendSpace().color(NamedTextColor.WHITE).build();
    @Getter
    private DateRange dateTracker;
    private final DateTimeFormatter outputFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withZone(ZoneId.systemDefault());
    private boolean prevStatus = false;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        configManager = new ConfigManager();
        dateTracker = DateRange.of(getConfiguration().getStartDate());
        getLogger().info("Set targeted date to " + dateTracker.getStartDate().format(outputFormatter) + ".");
        if (getConfiguration().isAlwaysEnabled()) {
            getLogger().info("Troll mode set to 'always enabled'.");
            prevStatus = true;
        } else if (dateTracker.isTodayInRange()) {
            getLogger().info("Today's the date. Time for the antics!");
            prevStatus = true;
        } else {
            Duration dur = dateTracker.untilStart();
            long days = dur.toDays();
            if (days > 0) {
                getLogger().info("The plugin will start its antics in " + days + (days == 1 ? " day" : " days") + " from now.");
            } else {
                getLogger().info("The plugin will start its antics in " + TimeUtils.getReadableDuration(dur) + " from now.");
            }
        }
        getLogger().info("If you want to manually override this during runtime, you can use '/fd override true/false'.");
        gaslightManager = new TableBasedBlockManager();
        blockFactoryManager = new BlockFactoryManager();
        if (getConfiguration().isMorphingDiamondOreEnabled()) {
            blockFactoryManager.register(new DiamondOreFactory());
        }
        if (getConfiguration().isMorphingGlowLichenEnabled()) {
            blockFactoryManager.register(new GlowLichenFactory());
        }
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
        CommandManager.registerCommand();
        if (getConfiguration().isMetricsEnabled()) {
            startMetrics();
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public boolean isTrollTime() {
        boolean status = dateTracker.isTodayInRange();
        if (getConfiguration().isAlwaysEnabled()) status = true;
        if (enableOverride != null) {
            status = enableOverride;
        }
        // load / clear cache depending on what the status changed to.
        if (status != prevStatus) {
            if (status) {
                gaslightManager.loadCache();
            } else {
                gaslightManager.clearCache();
            }
        }
        prevStatus = status;
        return status;
    }

    public FoolConfig getConfiguration() {
        return configManager.getConfig();
    }

    public void reloadConfiguration() {
        configManager.reloadConfig();
    }

    private void startMetrics() {
        new Metrics(this, 30336);
    }
}
