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

package io.github.trekkieendermom.foolsdiamond.util;

import lombok.Getter;

import java.time.*;
import java.time.temporal.TemporalUnit;

public class DateRange {
    @Getter
    private final ZonedDateTime startDate;
    @Getter
    private final ZonedDateTime endDate;
    private final ZoneId zoneId;

    public DateRange(LocalDate startDate, LocalDate endDate, ZoneId zoneId) {
        this.zoneId = zoneId;
        this.startDate = startDate.atStartOfDay(zoneId);
        this.endDate = endDate.atStartOfDay(zoneId);
    }

    public static DateRange of(MonthDay targetDay) {
        return of(targetDay, targetDay.withDayOfMonth(targetDay.getDayOfMonth() + 1));
    }

    public static DateRange of(MonthDay start, MonthDay end) {
        ZoneId zone = ZoneId.systemDefault();
        MonthDay now = MonthDay.now(zone);
        Year year = Year.now(zone);
        if (now.isAfter(end)) {
            year = year.plusYears(1);
        }
        return new DateRange(year.atMonthDay(start), year.atMonthDay(end), zone);
    }

    public boolean isTodayInRange() {
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        return now.isAfter(startDate) && now.isBefore(endDate);
    }

    public Duration getRangeDuration() {
        return Duration.between(startDate, endDate);
    }

    public long untilStart(TemporalUnit unit) {
        return until(startDate, unit);
    }

    public Duration untilStart() {
        return Duration.between(ZonedDateTime.now(zoneId), startDate);
    }

    public long untilEnd(TemporalUnit unit) {
        return until(endDate, unit);
    }

    public Duration untilEnd() {
        return Duration.between(ZonedDateTime.now(zoneId), endDate);
    }

    private long until(ZonedDateTime target, TemporalUnit unit) {
        ZonedDateTime current = ZonedDateTime.now(zoneId);
        return current.until(target, unit);
    }
}
