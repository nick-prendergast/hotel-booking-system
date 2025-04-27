package com.hotelmanager.util;

import com.hotelmanager.model.DailyAvailability;
import com.hotelmanager.model.DateRangeAvailability;
import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@UtilityClass
public class DateRangeUtil {

    public List<DateRangeAvailability> consolidateDateRanges(List<DailyAvailability> dailyAvailabilities) {
        if (isEmptyOrNull(dailyAvailabilities)) {
            return Collections.emptyList();
        }

        List<DateRangeAvailability> consolidatedRanges = new ArrayList<>();

        DailyAvailability initialDay = dailyAvailabilities.getFirst();
        LocalDate currentRangeStart = initialDay.date();
        LocalDate lastProcessedDate = currentRangeStart;
        int currentRangeAvailability = initialDay.availability();

        for (DailyAvailability currentDay : dailyAvailabilities) {
            if (isFirstDayAlreadyProcessed(currentDay, currentRangeStart)) {
                continue;
            }

            if (shouldStartNewRange(lastProcessedDate, currentDay, currentRangeAvailability)) {
                consolidatedRanges.add(createDateRange(currentRangeStart, lastProcessedDate, currentRangeAvailability));

                currentRangeStart = currentDay.date();
                currentRangeAvailability = currentDay.availability();
            }

            lastProcessedDate = currentDay.date();
        }

        consolidatedRanges.add(createDateRange(currentRangeStart, lastProcessedDate, currentRangeAvailability));
        return consolidatedRanges;
    }

    private boolean isEmptyOrNull(List<DailyAvailability> dailyAvailabilities) {
        return dailyAvailabilities == null || dailyAvailabilities.isEmpty();
    }

    private boolean isFirstDayAlreadyProcessed(DailyAvailability currentDay, LocalDate rangeStart) {
        return currentDay.date().equals(rangeStart);
    }

    private boolean shouldStartNewRange(LocalDate lastProcessedDate, DailyAvailability currentDay, int currentAvailability) {
        return !isConsecutiveDay(lastProcessedDate, currentDay.date()) ||
                !hasSameAvailability(currentDay, currentAvailability);
    }

    private boolean isConsecutiveDay(LocalDate lastDate, LocalDate currentDate) {
        return currentDate.equals(lastDate.plusDays(1));
    }

    private boolean hasSameAvailability(DailyAvailability day, int availability) {
        return day.availability() == availability;
    }

    private DateRangeAvailability createDateRange(LocalDate start, LocalDate end, int availability) {
        return new DateRangeAvailability(start, end, availability);
    }
}