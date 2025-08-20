package ru.practicum.stats.utils;

import java.time.format.DateTimeFormatter;

public final class DateTimeUtils {

    private DateTimeUtils() {

    }

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

}
