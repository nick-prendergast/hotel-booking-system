package com.hotelmanager.service;

import com.hotelmanager.exception.HotelNotFoundException;
import com.hotelmanager.model.Booking;
import com.hotelmanager.model.DailyAvailability;
import com.hotelmanager.model.Hotel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvailabilityCalculatorTest {

    @Mock
    private HotelDataService hotelDataService;

    @InjectMocks
    private AvailabilityCalculator availabilityCalculator;

    private Hotel mockHotel;
    private List<Booking> mockBookings;

    @BeforeEach
    void setUp() {
        mockHotel = mock(Hotel.class);

        mockBookings = new ArrayList<>();
        Booking booking1 = new Booking("H1", "SGL", "Standard",
                LocalDate.of(2024, 9, 1), LocalDate.of(2024, 9, 3));
        mockBookings.add(booking1);
    }

    @Test
    @DisplayName("Should calculate minimum availability for date range")
    void calculateMinimumAvailability_WithValidDateRange_ShouldReturnMinimum() {
        // Given
        String hotelId = "H1";
        String roomType = "SGL";
        LocalDate startDate = LocalDate.of(2024, 9, 1);
        LocalDate endDate = LocalDate.of(2024, 9, 3);

        when(hotelDataService.findHotelById(hotelId)).thenReturn(Optional.of(mockHotel));
        when(mockHotel.getTotalRoomsByType(roomType)).thenReturn(5);

        // Different bookings for different dates
        when(hotelDataService.findBookingsForDate(hotelId, roomType, startDate))
                .thenReturn(List.of(mockBookings.get(0), mockBookings.get(0))); // 2 bookings
        when(hotelDataService.findBookingsForDate(hotelId, roomType, startDate.plusDays(1)))
                .thenReturn(List.of(mockBookings.get(0))); // 1 booking
        when(hotelDataService.findBookingsForDate(hotelId, roomType, endDate))
                .thenReturn(List.of()); // 0 bookings

        // When
        int result = availabilityCalculator.calculateMinimumAvailability(hotelId, roomType, startDate, endDate);

        // Then
        assertThat(result).isEqualTo(3); // 5 total rooms - 2 bookings = 3 minimum
    }

    @Test
    @DisplayName("Should handle single date range")
    void calculateMinimumAvailability_WithSingleDate_ShouldCalculateCorrectly() {
        // Given
        String hotelId = "H1";
        String roomType = "SGL";
        LocalDate date = LocalDate.of(2024, 9, 1);

        when(hotelDataService.findHotelById(hotelId)).thenReturn(Optional.of(mockHotel));
        when(mockHotel.getTotalRoomsByType(roomType)).thenReturn(5);
        when(hotelDataService.findBookingsForDate(hotelId, roomType, date))
                .thenReturn(List.of(mockBookings.get(0)));

        // When
        int result = availabilityCalculator.calculateMinimumAvailability(hotelId, roomType, date, date);

        // Then
        assertThat(result).isEqualTo(4); // 5 total rooms - 1 booking = 4
    }

    @Test
    @DisplayName("Should throw exception when hotel not found")
    void calculateMinimumAvailability_WhenHotelNotFound_ShouldThrowException() {
        // Given
        String hotelId = "NonExistent";
        String roomType = "SGL";
        LocalDate startDate = LocalDate.of(2024, 9, 1);
        LocalDate endDate = LocalDate.of(2024, 9, 3);

        when(hotelDataService.findHotelById(hotelId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() ->
                availabilityCalculator.calculateMinimumAvailability(hotelId, roomType, startDate, endDate))
                .isInstanceOf(HotelNotFoundException.class)
                .hasMessage("Hotel not found: " + hotelId);
    }

    @Test
    @DisplayName("Should find available dates with positive availability")
    void findAvailableDates_WithSomeAvailability_ShouldReturnOnlyAvailableDates() {
        // Given
        String hotelId = "H1";
        String roomType = "SGL";
        int daysAhead = 3;
        LocalDate today = LocalDate.now();

        when(hotelDataService.findHotelById(hotelId)).thenReturn(Optional.of(mockHotel));
        when(mockHotel.getTotalRoomsByType(roomType)).thenReturn(2);

        // Different bookings for different dates
        when(hotelDataService.findBookingsForDate(eq(hotelId), eq(roomType), eq(today)))
                .thenReturn(List.of(mockBookings.get(0))); // 1 booking
        when(hotelDataService.findBookingsForDate(eq(hotelId), eq(roomType), eq(today.plusDays(1))))
                .thenReturn(List.of(mockBookings.get(0), mockBookings.get(0))); // 2 bookings (no availability)
        when(hotelDataService.findBookingsForDate(eq(hotelId), eq(roomType), eq(today.plusDays(2))))
                .thenReturn(List.of()); // 0 bookings

        // When
        List<DailyAvailability> result = availabilityCalculator.findAvailableDates(hotelId, roomType, daysAhead);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).date()).isEqualTo(today);
        assertThat(result.get(0).availability()).isEqualTo(1);
        assertThat(result.get(1).date()).isEqualTo(today.plusDays(2));
        assertThat(result.get(1).availability()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should return empty list when no availability")
    void findAvailableDates_WithNoAvailability_ShouldReturnEmptyList() {
        // Given
        String hotelId = "H1";
        String roomType = "SGL";
        int daysAhead = 3;

        when(hotelDataService.findHotelById(hotelId)).thenReturn(Optional.of(mockHotel));
        when(mockHotel.getTotalRoomsByType(roomType)).thenReturn(1);

        // All dates fully booked
        when(hotelDataService.findBookingsForDate(anyString(), anyString(), any(LocalDate.class)))
                .thenReturn(List.of(mockBookings.get(0)));

        // When
        List<DailyAvailability> result = availabilityCalculator.findAvailableDates(hotelId, roomType, daysAhead);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should handle zero days ahead")
    void findAvailableDates_WithZeroDaysAhead_ShouldReturnEmptyList() {
        // Given
        String hotelId = "H1";
        String roomType = "SGL";
        int daysAhead = 0;

        when(hotelDataService.findHotelById(hotelId)).thenReturn(Optional.of(mockHotel));
        when(mockHotel.getTotalRoomsByType(roomType)).thenReturn(5);

        // When
        List<DailyAvailability> result = availabilityCalculator.findAvailableDates(hotelId, roomType, daysAhead);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should calculate availability considering zero bookings")
    void calculateAvailabilityForDate_WithNoBookings_ShouldReturnTotalRooms() {
        // Given
        String hotelId = "H1";
        String roomType = "SGL";
        LocalDate date = LocalDate.of(2024, 9, 1);

        when(hotelDataService.findHotelById(hotelId)).thenReturn(Optional.of(mockHotel));
        when(mockHotel.getTotalRoomsByType(roomType)).thenReturn(5);
        when(hotelDataService.findBookingsForDate(hotelId, roomType, date))
                .thenReturn(List.of());

        // When
        int result = availabilityCalculator.calculateMinimumAvailability(hotelId, roomType, date, date);

        // Then
        assertThat(result).isEqualTo(5); // All rooms available
    }

    @Test
    @DisplayName("Should handle negative availability (overbooking)")
    void calculateAvailabilityForDate_WithOverbooking_ShouldReturnNegative() {
        // Given
        String hotelId = "H1";
        String roomType = "SGL";
        LocalDate date = LocalDate.of(2024, 9, 1);

        when(hotelDataService.findHotelById(hotelId)).thenReturn(Optional.of(mockHotel));
        when(mockHotel.getTotalRoomsByType(roomType)).thenReturn(2);

        // More bookings than rooms
        List<Booking> overbooked = List.of(
                mockBookings.get(0),
                mockBookings.get(0),
                mockBookings.get(0)
        );
        when(hotelDataService.findBookingsForDate(hotelId, roomType, date))
                .thenReturn(overbooked);

        // When
        int result = availabilityCalculator.calculateMinimumAvailability(hotelId, roomType, date, date);

        // Then
        assertThat(result).isEqualTo(-1); // 2 rooms - 3 bookings = -1
    }
}