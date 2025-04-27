package com.hotelmanager.service;

import com.hotelmanager.exception.HotelNotFoundException;
import com.hotelmanager.exception.InvalidCommandException;
import com.hotelmanager.exception.InvalidDateRangeException;
import com.hotelmanager.exception.RoomTypeNotFoundException;
import com.hotelmanager.model.Hotel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidationServiceTest {

    @Mock
    private HotelDataService hotelDataService;

    @InjectMocks
    private ValidationService validationService;

    private Hotel mockHotel;

    @BeforeEach
    void setUp() {
        mockHotel = mock(Hotel.class);
    }

    @Test
    @DisplayName("Should return hotel when it exists")
    void validateHotelExists_WhenHotelExists_ShouldReturnHotel() {
        // Given
        String hotelId = "H1";
        when(hotelDataService.findHotelById(hotelId)).thenReturn(Optional.of(mockHotel));

        // When
        Hotel result = validationService.validateHotelExists(hotelId);

        // Then
        assertThat(result).isEqualTo(mockHotel);
    }

    @Test
    @DisplayName("Should throw exception when hotel doesn't exist")
    void validateHotelExists_WhenHotelDoesNotExist_ShouldThrowException() {
        // Given
        String hotelId = "NonExistent";
        when(hotelDataService.findHotelById(hotelId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> validationService.validateHotelExists(hotelId))
                .isInstanceOf(HotelNotFoundException.class)
                .hasMessage("Hotel not found: NonExistent");
    }

    @Test
    @DisplayName("Should validate successfully when room type exists")
    void validateRoomTypeExists_WhenRoomTypeExists_ShouldNotThrowException() {
        // Given
        when(mockHotel.getTotalRoomsByType("SGL")).thenReturn(5);

        // When/Then
        validationService.validateRoomTypeExists(mockHotel, "SGL");
    }

    @Test
    @DisplayName("Should throw exception when room type doesn't exist")
    void validateRoomTypeExists_WhenRoomTypeDoesNotExist_ShouldThrowException() {
        // Given
        when(mockHotel.getId()).thenReturn("H1");
        when(mockHotel.getTotalRoomsByType("SGL")).thenReturn(0);

        // When/Then
        assertThatThrownBy(() -> validationService.validateRoomTypeExists(mockHotel, "SGL"))
                .isInstanceOf(RoomTypeNotFoundException.class)
                .hasMessage("Room type 'SGL' not found in hotel 'H1'");
    }

    @Test
    @DisplayName("Should parse single date correctly")
    void parseDateRange_WithSingleDate_ShouldReturnSameDateTwice() {
        // Given
        String dateRange = "20240901";

        // When
        LocalDate[] result = validationService.parseDateRange(dateRange);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result[0]).isEqualTo(LocalDate.of(2024, 9, 1));
        assertThat(result[1]).isEqualTo(LocalDate.of(2024, 9, 1));
    }

    @Test
    @DisplayName("Should parse date range correctly")
    void parseDateRange_WithDateRange_ShouldReturnStartAndEndDates() {
        // Given
        String dateRange = "20240901-20240903";

        // When
        LocalDate[] result = validationService.parseDateRange(dateRange);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result[0]).isEqualTo(LocalDate.of(2024, 9, 1));
        assertThat(result[1]).isEqualTo(LocalDate.of(2024, 9, 3));
    }

    @Test
    @DisplayName("Should throw exception for invalid single date format")
    void parseDateRange_WithInvalidSingleDateFormat_ShouldThrowException() {
        // Given
        String dateRange = "invalid";

        // When/Then
        assertThatThrownBy(() -> validationService.parseDateRange(dateRange))
                .isInstanceOf(InvalidDateRangeException.class)
                .hasMessage("Invalid date format. Expected YYYYMMDD");
    }

    @Test
    @DisplayName("Should throw exception for invalid date within range")
    void parseDateRange_WithInvalidDateInRange_ShouldThrowException() {
        // Given
        String dateRange = "20240901-invalid";

        // When/Then
        assertThatThrownBy(() -> validationService.parseDateRange(dateRange))
                .isInstanceOf(InvalidDateRangeException.class)
                .hasMessage("Invalid date format. Expected YYYYMMDD");
    }

    @Test
    @DisplayName("Should throw exception for invalid date range format")
    void parseDateRange_WithInvalidRangeFormat_ShouldThrowException() {
        // Given
        String dateRange = "20240901-20240903-20240905";

        // When/Then
        assertThatThrownBy(() -> validationService.parseDateRange(dateRange))
                .isInstanceOf(InvalidDateRangeException.class)
                .hasMessage("Invalid date range format. Expected 'YYYYMMDD-YYYYMMDD'");
    }

    @Test
    @DisplayName("Should throw exception when end date is before start date")
    void parseDateRange_WithEndDateBeforeStartDate_ShouldThrowException() {
        // Given
        String dateRange = "20240903-20240901";

        // When/Then
        assertThatThrownBy(() -> validationService.parseDateRange(dateRange))
                .isInstanceOf(InvalidDateRangeException.class)
                .hasMessage("End date cannot be before start date");
    }

    @Test
    @DisplayName("Should validate positive days ahead successfully")
    void validateDaysAhead_WithPositiveValue_ShouldNotThrowException() {
        // When/Then
        validationService.validateDaysAhead(5);
    }

    @Test
    @DisplayName("Should throw exception for zero days ahead")
    void validateDaysAhead_WithZero_ShouldThrowException() {
        // When/Then
        assertThatThrownBy(() -> validationService.validateDaysAhead(0))
                .isInstanceOf(InvalidCommandException.class)
                .hasMessage("Days ahead must be positive");
    }

    @Test
    @DisplayName("Should throw exception for negative days ahead")
    void validateDaysAhead_WithNegativeValue_ShouldThrowException() {
        // When/Then
        assertThatThrownBy(() -> validationService.validateDaysAhead(-1))
                .isInstanceOf(InvalidCommandException.class)
                .hasMessage("Days ahead must be positive");
    }
}