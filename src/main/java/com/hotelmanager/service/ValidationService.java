package com.hotelmanager.service;

import com.hotelmanager.model.Hotel;
import com.hotelmanager.exception.BookingSystemException;
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

    private final HotelDataService hotelDataService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public Hotel validateHotelExists(String hotelId) {
        Hotel hotel = hotelDataService.getHotel(hotelId);
        if (hotel == null) {
            log.warn("Validation failed: Hotel not found - hotelId={}", hotelId);
            throw new BookingSystemException("Hotel not found: " + hotelId);
        }
        return hotel;
    }

    public void validateRoomTypeExists(Hotel hotel, String roomType) {
        if (hotel.getTotalRoomsByType(roomType) == 0) {
            log.warn("Validation failed: Room type not found - hotelId={}, roomType={}",
                    hotel.getId(), roomType);
            throw new BookingSystemException(String.format(
                    "Room type '%s' not found in hotel '%s'", roomType, hotel.getId()));
        }
    }

    public LocalDate[] validateAndParseDateRange(String dateRange) {
        try {
            if (dateRange.contains("-")) {
                String[] dates = dateRange.split("-");
                if (dates.length != 2) {
                    throw new BookingSystemException("Invalid date range format. Expected 'YYYYMMDD-YYYYMMDD'");
                }
                LocalDate startDate = LocalDate.parse(dates[0], DATE_FORMATTER);
                LocalDate endDate = LocalDate.parse(dates[1], DATE_FORMATTER);

                if (endDate.isBefore(startDate)) {
                    throw new BookingSystemException("End date cannot be before start date");
                }

                return new LocalDate[]{startDate, endDate};
            } else {
                LocalDate date = LocalDate.parse(dateRange, DATE_FORMATTER);
                return new LocalDate[]{date, date};
            }
        } catch (DateTimeParseException e) {
            log.error("Date parsing failed: dateRange={}", dateRange, e);
            throw new BookingSystemException("Invalid date format. Expected YYYYMMDD", e);
        }
    }

    public void validateDaysAhead(int daysAhead) {
        if (daysAhead <= 0) {
            log.warn("Validation failed: Invalid days ahead - daysAhead={}", daysAhead);
            throw new BookingSystemException("Days ahead must be positive");
        }
    }
}