package com.hotelmanager.service;

import com.hotelmanager.exception.HotelNotFoundException;
import com.hotelmanager.exception.InvalidCommandException;
import com.hotelmanager.exception.InvalidDateRangeException;
import com.hotelmanager.exception.RoomTypeNotFoundException;
import com.hotelmanager.model.Hotel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ValidationService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final HotelDataService hotelDataService;

    public Hotel validateHotelExists(String hotelId) {
        return hotelDataService.findHotelById(hotelId)
                .orElseThrow(() -> {
                    log.warn("Validation failed: Hotel not found - hotelId={}", hotelId);
                    return new HotelNotFoundException(hotelId);
                });
    }

    public void validateRoomTypeExists(Hotel hotel, String roomType) {
        if (hotel.getTotalRoomsByType(roomType) == 0) {
            log.warn("Validation failed: Room type not found - hotelId={}, roomType={}",
                    hotel.getId(), roomType);
            throw new RoomTypeNotFoundException(hotel.getId(), roomType);
        }
    }

    public LocalDate[] parseDateRange(String dateRange) {
        try {
            if (dateRange.contains("-")) {
                return parseRangeDates(dateRange);
            } else {
                LocalDate date = parseDate(dateRange);
                return new LocalDate[]{date, date};
            }
        } catch (DateTimeParseException e) {
            log.error("Date parsing failed: dateRange={}", dateRange, e);
            throw new InvalidDateRangeException("Invalid date format. Expected YYYYMMDD");
        }
    }

    public void validateDaysAhead(int daysAhead) {
        if (daysAhead <= 0) {
            log.warn("Validation failed: Invalid days ahead - daysAhead={}", daysAhead);
            throw new InvalidCommandException("Days ahead must be positive");
        }
    }

    private LocalDate[] parseRangeDates(String dateRange) {
        String[] dates = dateRange.split("-");
        if (dates.length != 2) {
            throw new InvalidDateRangeException("Invalid date range format. Expected 'YYYYMMDD-YYYYMMDD'");
        }

        LocalDate startDate = parseDate(dates[0]);
        LocalDate endDate = parseDate(dates[1]);

        if (endDate.isBefore(startDate)) {
            throw new InvalidDateRangeException("End date cannot be before start date");
        }

        return new LocalDate[]{startDate, endDate};
    }

    private LocalDate parseDate(String date) {
        return LocalDate.parse(date, DATE_FORMATTER);
    }
}