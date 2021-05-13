package com.github.onedirection.utils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.Locale;
import java.util.Objects;

/**
 * A utility class to provide common operations on time-related objects
 */
public class TimeUtils {

    public static ZonedDateTime truncateTimeToWeeks(ZonedDateTime time) {
        return Objects.requireNonNull(time).truncatedTo(ChronoUnit.DAYS)
                .with(TemporalAdjusters.next(WeekFields.of(Locale.getDefault()).getFirstDayOfWeek())).minusWeeks(1);
    }

    public static ZonedDateTime truncateTimeToDays(ZonedDateTime time) {
        return Objects.requireNonNull(time).truncatedTo(ChronoUnit.DAYS);
    }

    public static ZonedDateTime truncateTimeToMonths(ZonedDateTime time) {
        return Objects.requireNonNull(time).truncatedTo(ChronoUnit.DAYS).withDayOfMonth(1);
    }

    public static ZonedDateTime epochToZonedDateTime(long epoch) {
        return ZonedDateTime.ofInstant(Instant.ofEpochSecond(epoch), ZoneId.systemDefault());
    }
}
