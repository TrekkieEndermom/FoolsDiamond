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

import lombok.Getter;
import org.bukkit.World;
import org.bukkit.configuration.Configuration;

import java.time.DateTimeException;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;

public class FoolConfig {
    private static final DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("MMM d");
    private static final FoolConfig DEFAULT = new FoolConfig();
    @Getter
    private int version;
    @Getter
    private boolean alwaysEnabled;
    @Getter
    private boolean morphingDiamondOreEnabled;
    @Getter
    private boolean morphingGlowLichenEnabled;
    @Getter
    private double chanceToMorph;
    @Getter
    private long ttlAfterUnload;
    @Getter
    private long ttlAfterRefresh;
    private List<String> whitelistedWorlds;
    @Getter
    private boolean refreshOnBlockChange;
    @Getter
    private boolean metricsEnabled;
    @Getter
    private MonthDay startDate;
    @Getter
    private boolean handleLightChange;

    public FoolConfig() {
        this.version = 1;
        this.alwaysEnabled = false;
        this.morphingDiamondOreEnabled = true;
        this.morphingGlowLichenEnabled = true;
        this.chanceToMorph = 0.3;
        this.ttlAfterUnload = 120000;
        this.ttlAfterRefresh = 30000;
        this.whitelistedWorlds = List.of("world");
        this.refreshOnBlockChange = true;
        this.metricsEnabled = true;
        this.startDate = MonthDay.of(4, 1);
        this.handleLightChange = false;
    }

    void load(Configuration configuration) {
        this.version = configuration.getInt("version");
        this.metricsEnabled = configuration.getBoolean("enable-metrics", DEFAULT.metricsEnabled);
        this.alwaysEnabled = configuration.getBoolean("enable.always", DEFAULT.alwaysEnabled);
        try {
            this.startDate = MonthDay.parse(configuration.getString("enable.on-date", "Apr 1"), inputFormatter);
        } catch (DateTimeException e) {
            FoolsDiamondPlugin.getInstance().getLogger().log(Level.WARNING, "Couldn't parse date", e);
            this.startDate = DEFAULT.startDate;
        }
        this.morphingDiamondOreEnabled = configuration.getBoolean("morphing-block.enable.diamond-ore", DEFAULT.morphingDiamondOreEnabled);
        this.morphingGlowLichenEnabled = configuration.getBoolean("morphing-block.enable.glow-lichen", DEFAULT.morphingGlowLichenEnabled);
        this.handleLightChange = configuration.getBoolean("morphing-block.handle-light-change", DEFAULT.handleLightChange);
        double chance = configuration.getDouble("morphing-block.chance", DEFAULT.chanceToMorph);
        if (chance < 0.0 || chance > 1.0) {
            chance = DEFAULT.chanceToMorph;
        }
        this.chanceToMorph = chance;
        long unload = configuration.getLong("cache.ttl-after-unload", DEFAULT.ttlAfterUnload) * 1000;
        if (unload < 0) {
            unload = DEFAULT.ttlAfterUnload;
        }
        this.ttlAfterUnload = unload;
        long refresh = configuration.getLong("cache.ttl-after-refresh", DEFAULT.ttlAfterRefresh) * 1000;
        if (refresh < 0) {
            refresh = DEFAULT.ttlAfterRefresh;
        }
        this.ttlAfterRefresh = refresh;
        this.whitelistedWorlds = List.copyOf(configuration.getStringList("morphing-block.appear-in"));
        this.refreshOnBlockChange = configuration.getBoolean("cache.refresh-on-block-change", DEFAULT.refreshOnBlockChange);
    }

    public boolean isOutdated() {
        return version != DEFAULT.version;
    }

    public boolean isWhitelisted(World world) {
        return whitelistedWorlds.contains(world.getName());
    }
}
