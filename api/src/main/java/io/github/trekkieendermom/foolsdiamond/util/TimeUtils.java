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

import lombok.experimental.UtilityClass;

import java.time.Duration;
import java.util.StringJoiner;

@UtilityClass
public class TimeUtils {
    public static String getReadableDuration(Duration duration) {
        long days = duration.toDaysPart();
        long hours = duration.toHoursPart();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        StringJoiner joiner = new StringJoiner(" ");
        if (days > 0) {
            joiner.add(String.valueOf(days)).add(days == 1 ? "day" : "days");
        }
        if (hours > 0) {
            if (joiner.length() != 0 && minutes == 0 && seconds == 0) {
                joiner.add("and");
            }
            joiner.add(String.valueOf(hours)).add(hours == 1 ? "hour" : "hours");
        }
        if (minutes > 0) {
            if (joiner.length() != 0 && seconds == 0) {
                joiner.add("and");
            }
            joiner.add(String.valueOf(minutes)).add(minutes == 1 ? "minute" : "minutes");
        }
        if (seconds > 0) {
            if (joiner.length() != 0) {
                joiner.add("and");
            }
            joiner.add(String.valueOf(seconds)).add(seconds == 1 ? "second" : "seconds");
        }
        return joiner.toString();
    }
}
