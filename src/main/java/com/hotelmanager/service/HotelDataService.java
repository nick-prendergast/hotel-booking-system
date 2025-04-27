package com.hotelmanager.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelmanager.exception.BookingSystemException;
import com.hotelmanager.model.Booking;
import com.hotelmanager.model.Hotel;
import com.hotelmanager.model.Room;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HotelDataService {

    private final ObjectMapper objectMapper;
    private final Map<String, Hotel> hotels = new ConcurrentHashMap<>();
    private final List<Booking> bookings = Collections.synchronizedList(new ArrayList<>());
    private volatile boolean initialized = false;

    public void loadData(String hotelsFile, String bookingsFile) {
        log.info("Loading hotel data from: {}", hotelsFile);
        log.info("Loading booking data from: {}", bookingsFile);

        try {
            validateFiles(hotelsFile, bookingsFile);

            // Load hotels
            Hotel[] hotelArray = objectMapper.readValue(new File(hotelsFile), Hotel[].class);
            hotels.clear();
            for (Hotel hotel : hotelArray) {
                hotels.put(hotel.getId(), hotel);
                log.debug("Loaded hotel: {} with {} room types and {} rooms",
                        hotel.getId(), hotel.getRoomTypes().size(), hotel.getRooms().size());
            }

            // Load bookings
            Booking[] bookingArray = objectMapper.readValue(new File(bookingsFile), Booking[].class);
            bookings.clear();
            bookings.addAll(Arrays.asList(bookingArray));

            initialized = true;
            log.info("Successfully loaded {} hotels and {} bookings", hotels.size(), bookings.size());

        } catch (IOException e) {
            log.error("Failed to load data files", e);
            throw new BookingSystemException("Failed to load data: " + e.getMessage(), e);
        }
    }

    private void validateFiles(String hotelsFile, String bookingsFile) {
        File hotelFileObj = new File(hotelsFile);
        File bookingFileObj = new File(bookingsFile);

        if (!hotelFileObj.exists() || !hotelFileObj.canRead()) {
            throw new BookingSystemException("Hotels file not found or not readable: " + hotelsFile);
        }

        if (!bookingFileObj.exists() || !bookingFileObj.canRead()) {
            throw new BookingSystemException("Bookings file not found or not readable: " + bookingsFile);
        }
    }

    @Cacheable(value = "hotels", key = "#hotelId")
    public Hotel getHotel(String hotelId) {
        ensureInitialized();
        return hotels.get(hotelId);
    }

    @Cacheable(value = "bookings", key = "#hotelId + '-' + #roomType + '-' + #date")
    public List<Booking> getBookingsForDate(String hotelId, String roomType, LocalDate date) {
        ensureInitialized();
        return bookings.stream()
                .filter(booking -> booking.hotelId().equals(hotelId) &&
                        booking.roomType().equals(roomType) &&
                        !date.isBefore(booking.arrival()) &&
                        date.isBefore(booking.departure()))
                .collect(Collectors.toList());
    }


    private void ensureInitialized() {
        if (!initialized) {
            throw new BookingSystemException("Hotel data not initialized");
        }
    }


}