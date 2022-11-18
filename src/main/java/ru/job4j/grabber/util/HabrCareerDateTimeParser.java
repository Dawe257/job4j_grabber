package ru.job4j.grabber.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HabrCareerDateTimeParser implements DateTimeParser {

    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    @Override
    public LocalDateTime parse(String parse) {
        return LocalDateTime.parse(parse, formatter);
    }
}
