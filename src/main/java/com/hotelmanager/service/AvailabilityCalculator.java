package com.hotelmanager.service;

import com.hotelmanager.model.DailyAvailability;
import com.hotelmanager.model.Hotel;
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

        Hotel hotel = hotelDataService.getHotel(hotelId);
        int totalRooms = hotel.getTotalRoomsByType(roomType);

        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate) + 1;

        // For large date ranges, use parallel processing
        Stream<LocalDate> dateStream = Stream.iterate(startDate, date -> date.plusDays(1))
                .limit(daysBetween);

        if (daysBetween > 30) {
            dateStream = dateStream.parallel();
        }

        int minAvailability = dateStream
                .mapToInt(date -> {
                    int bookings = hotelDataService.getBookingsForDate(hotelId, roomType, date).size();
                    return totalRooms - bookings;
                })
                .min()
                .orElse(totalRooms);

        log.debug("Minimum availability calculated: {}", minAvailability);
        return minAvailability;
    }

    public List<DailyAvailability> findAvailableDates(String hotelId, String roomType, int daysAhead) {
        log.debug("Finding available dates: hotelId={}, roomType={}, daysAhead={}",
                hotelId, roomType, daysAhead);

        Hotel hotel = hotelDataService.getHotel(hotelId);
        int totalRooms = hotel.getTotalRoomsByType(roomType);
        LocalDate today = LocalDate.now();

        // Use parallel processing for large searches
        Stream<Integer> dayStream = Stream.iterate(0, i -> i + 1).limit(daysAhead);

        if (daysAhead > 100) {
            dayStream = dayStream.parallel();
        }

        List<DailyAvailability> availabilities = dayStream
                .map(i -> {
                    LocalDate date = today.plusDays(i);
                    int bookings = hotelDataService.getBookingsForDate(hotelId, roomType, date).size();
                    int availability = totalRooms - bookings;
                    return new DailyAvailability(date, availability);
                })
                .filter(daily -> daily.availability() > 0)
                .collect(Collectors.toList());

        log.debug("Found {} available dates", availabilities.size());
        return availabilities;
    }

}