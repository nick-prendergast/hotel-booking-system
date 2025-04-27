package com.hotelmanager.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public record DateRangeAvailability(
        LocalDate startDate,
        LocalDate endDate,
        int availability
) {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public String toString() {
        String start = startDate.format(DATE_FORMATTER);
        String end = endDate.format(DATE_FORMATTER);
        return String.format("(%s-%s, %d)", start, end, availability);
    }
}