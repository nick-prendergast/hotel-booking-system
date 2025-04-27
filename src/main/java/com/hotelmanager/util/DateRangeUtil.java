package com.hotelmanager.util;

import com.hotelmanager.model.DateRangeAvailability;
import com.hotelmanager.model.DailyAvailability;
import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@UtilityClass
public class DateRangeUtil {


    public List<DateRangeAvailability> consolidateDateRanges(List<DailyAvailability> dailyAvailabilities) {
        if (dailyAvailabilities.isEmpty()) {
            return Collections.emptyList();
        }

        List<DateRangeAvailability> result = new ArrayList<>();
        LocalDate rangeStart = dailyAvailabilities.get(0).date();
        int currentAvailability = dailyAvailabilities.get(0).availability();

        for (int i = 1; i < dailyAvailabilities.size(); i++) {
            DailyAvailability current = dailyAvailabilities.get(i);
            DailyAvailability previous = dailyAvailabilities.get(i - 1);

            if (current.date().equals(previous.date().plusDays(1)) &&
                    current.availability() == currentAvailability) {
            } else {
                result.add(new DateRangeAvailability(rangeStart, previous.date(), currentAvailability));
                rangeStart = current.date();
                currentAvailability = current.availability();
            }
        }

        result.add(new DateRangeAvailability(rangeStart,
                dailyAvailabilities.get(dailyAvailabilities.size() - 1).date(),
                currentAvailability));

        return result;
    }
}