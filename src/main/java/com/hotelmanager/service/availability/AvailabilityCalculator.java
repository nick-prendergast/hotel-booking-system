package com.hotelmanager.service.availability;

import com.hotelmanager.exception.HotelNotFoundException;
import com.hotelmanager.model.DailyAvailability;
import com.hotelmanager.service.data.HotelDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class AvailabilityCalculator {

    private final HotelDataService hotelDataService;

    public int calculateMinimumAvailability(String hotelId, String roomType,
                                            LocalDate startDate, LocalDate endDate) {
        log.debug("Calculating minimum availability: hotelId={}, roomType={}, start={}, end={}",
                hotelId, roomType, startDate, endDate);

        int totalRooms = getTotalRoomsByType(hotelId, roomType);
        List<LocalDate> dateRange = generateDateRange(startDate, endDate);

        int minAvailability = dateRange.stream()
                .mapToInt(date -> calculateAvailabilityForDate(hotelId, roomType, totalRooms, date))
                .min()
                .orElse(totalRooms);

        log.debug("Minimum availability calculated: {}", minAvailability);
        return minAvailability;
    }

    public List<DailyAvailability> findAvailableDates(String hotelId, String roomType, int daysAhead) {
        log.debug("Finding available dates: hotelId={}, roomType={}, daysAhead={}",
                hotelId, roomType, daysAhead);

        int totalRooms = getTotalRoomsByType(hotelId, roomType);
        LocalDate today = LocalDate.now();

        return Stream.iterate(today, date -> date.plusDays(1))
                .limit(daysAhead)
                .map(date -> {
                    int availability = calculateAvailabilityForDate(hotelId, roomType, totalRooms, date);
                    return new DailyAvailability(date, availability);
                })
                .filter(daily -> daily.availability() > 0)
                .collect(Collectors.toList());
    }

    private int getTotalRoomsByType(String hotelId, String roomType) {
        return hotelDataService.findHotelById(hotelId)
                .orElseThrow(() -> new HotelNotFoundException(hotelId))
                .getTotalRoomsByType(roomType);
    }

    private int calculateAvailabilityForDate(String hotelId, String roomType, int totalRooms, LocalDate date) {
        int bookings = hotelDataService.findBookingsForDate(hotelId, roomType, date).size();
        return totalRooms - bookings;
    }

    private List<LocalDate> generateDateRange(LocalDate startDate, LocalDate endDate) {
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        return Stream.iterate(startDate, date -> date.plusDays(1))
                .limit(daysBetween)
                .collect(Collectors.toList());
    }
}