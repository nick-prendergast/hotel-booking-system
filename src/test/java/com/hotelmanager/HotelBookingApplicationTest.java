package com.hotelmanager;

import com.hotelmanager.exception.DataLoadException;
import com.hotelmanager.service.ConsoleOutputService;
import com.hotelmanager.service.HotelBookingService;
import com.hotelmanager.service.HotelDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HotelBookingApplicationTest {

    @Mock
    private HotelDataService hotelDataService;

    @Mock
    private HotelBookingService hotelBookingService;

    @Mock
    private ConsoleOutputService consoleOutputService;

    private HotelBookingApplication application;

    @BeforeEach
    void setUp() {
        application = new HotelBookingApplication(hotelDataService, hotelBookingService, consoleOutputService);
    }

    @Test
    void executeApplication_WithValidArgs_ShouldLoadDataAndStartCommandLoop() {
        // Given
        String[] args = {"--hotels", "test_hotels.json", "--bookings", "test_bookings.json"};

        // When
        boolean result = application.executeApplication(args);

        // Then
        assertThat(result).isTrue();
        verify(hotelDataService).loadFromFiles("test_hotels.json", "test_bookings.json");
        verify(hotelBookingService).startCommandLoop();
        verifyNoInteractions(consoleOutputService);
    }

    @Test
    void executeApplication_WithMissingArgs_ShouldDisplayErrorAndReturnFalse() {
        // Given
        String[] args = {};

        // When
        boolean result = application.executeApplication(args);

        // Then
        assertThat(result).isFalse();
        verify(consoleOutputService).displayError(contains("Usage:"));
        verifyNoInteractions(hotelDataService);
        verifyNoInteractions(hotelBookingService);
    }

    @Test
    void executeApplication_WithDataLoadError_ShouldDisplayErrorAndReturnFalse() {
        // Given
        String[] args = {"--hotels", "test_hotels.json", "--bookings", "test_bookings.json"};
        doThrow(new DataLoadException("File not found"))
                .when(hotelDataService).loadFromFiles(anyString(), anyString());

        // When
        boolean result = application.executeApplication(args);

        // Then
        assertThat(result).isFalse();
        verify(hotelDataService).loadFromFiles("test_hotels.json", "test_bookings.json");
        verify(consoleOutputService).displayError("File not found");
        verify(hotelBookingService, never()).startCommandLoop();
    }
}