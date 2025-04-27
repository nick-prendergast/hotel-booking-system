package com.hotelmanager.service;

import com.hotelmanager.model.DailyAvailability;
import com.hotelmanager.model.DateRangeAvailability;
import com.hotelmanager.model.Hotel;
import com.hotelmanager.model.request.AvailabilityRequest;
import com.hotelmanager.model.request.SearchRequest;
import com.hotelmanager.model.response.AvailabilityResponse;
import com.hotelmanager.model.response.SearchResponse;
import com.hotelmanager.util.DateRangeUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceTest {

    @Mock
    private ValidationService validationService;

    @Mock
    private AvailabilityCalculator availabilityCalculator;

    @InjectMocks
    private AvailabilityService availabilityService;

    private Hotel mockHotel;
    private LocalDate[] mockDateRange;
    private List<DailyAvailability> mockDailyAvailabilities;
    private List<DateRangeAvailability> mockDateRangeAvailabilities;

    @BeforeEach
    void setUp() {
        mockHotel = new Hotel();
        mockHotel.setId("H1");
        mockHotel.setName("Test Hotel");

        mockDateRange = new LocalDate[]{
                LocalDate.of(2024, 9, 1),
                LocalDate.of(2024, 9, 3)
        };

        mockDailyAvailabilities = List.of(
                new DailyAvailability(LocalDate.of(2024, 9, 1), 2),
                new DailyAvailability(LocalDate.of(2024, 9, 2), 2),
                new DailyAvailability(LocalDate.of(2024, 9, 3), 1)
        );

        mockDateRangeAvailabilities = List.of(
                new DateRangeAvailability(
                        LocalDate.of(2024, 9, 1),
                        LocalDate.of(2024, 9, 2),
                        2
                ),
                new DateRangeAvailability(
                        LocalDate.of(2024, 9, 3),
                        LocalDate.of(2024, 9, 3),
                        1
                )
        );
    }

    @Test
    @DisplayName("Should check availability successfully")
    void checkAvailability_WithValidRequest_ShouldReturnAvailabilityResponse() {
        // Given
        AvailabilityRequest request = new AvailabilityRequest("H1", "20240901-20240903", "SGL");

        when(validationService.validateHotelExists("H1")).thenReturn(mockHotel);
        when(validationService.parseDateRange("20240901-20240903")).thenReturn(mockDateRange);
        when(availabilityCalculator.calculateMinimumAvailability(
                "H1", "SGL", mockDateRange[0], mockDateRange[1])).thenReturn(2);

        // When
        AvailabilityResponse response = availabilityService.checkAvailability(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.hotelId()).isEqualTo("H1");
        assertThat(response.roomType()).isEqualTo("SGL");
        assertThat(response.dateRange()).isEqualTo("20240901-20240903");
        assertThat(response.availability()).isEqualTo(2);
        assertThat(response.message()).isEqualTo("Success");

        verify(validationService).validateHotelExists("H1");
        verify(validationService).validateRoomTypeExists(mockHotel, "SGL");
        verify(validationService).parseDateRange("20240901-20240903");
        verify(availabilityCalculator).calculateMinimumAvailability(
                "H1", "SGL", mockDateRange[0], mockDateRange[1]);
    }

    @Test
    @DisplayName("Should search availability successfully")
    void searchAvailability_WithValidRequest_ShouldReturnSearchResponse() {
        try (MockedStatic<DateRangeUtil> mockedUtil = mockStatic(DateRangeUtil.class)) {
            // Given
            SearchRequest request = new SearchRequest("H1", 5, "DBL");

            when(validationService.validateHotelExists("H1")).thenReturn(mockHotel);
            when(availabilityCalculator.findAvailableDates("H1", "DBL", 5))
                    .thenReturn(mockDailyAvailabilities);
            mockedUtil.when(() -> DateRangeUtil.consolidateDateRanges(mockDailyAvailabilities))
                    .thenReturn(mockDateRangeAvailabilities);

            // When
            SearchResponse response = availabilityService.searchAvailability(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.availabilities()).isEqualTo(mockDateRangeAvailabilities);
            assertThat(response.totalResults()).isEqualTo(2);

            verify(validationService).validateHotelExists("H1");
            verify(validationService).validateRoomTypeExists(mockHotel, "DBL");
            verify(validationService).validateDaysAhead(5);
            verify(availabilityCalculator).findAvailableDates("H1", "DBL", 5);
            mockedUtil.verify(() -> DateRangeUtil.consolidateDateRanges(mockDailyAvailabilities));
        }
    }

    @Test
    @DisplayName("Should handle single date availability check")
    void checkAvailability_WithSingleDate_ShouldReturnAvailabilityResponse() {
        // Given
        AvailabilityRequest request = new AvailabilityRequest("H1", "20240901", "SGL");
        LocalDate[] singleDateRange = new LocalDate[]{
                LocalDate.of(2024, 9, 1),
                LocalDate.of(2024, 9, 1)
        };

        when(validationService.validateHotelExists("H1")).thenReturn(mockHotel);
        when(validationService.parseDateRange("20240901")).thenReturn(singleDateRange);
        when(availabilityCalculator.calculateMinimumAvailability(
                "H1", "SGL", singleDateRange[0], singleDateRange[1])).thenReturn(3);

        // When
        AvailabilityResponse response = availabilityService.checkAvailability(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.availability()).isEqualTo(3);
        assertThat(response.dateRange()).isEqualTo("20240901");

        verify(availabilityCalculator).calculateMinimumAvailability(
                "H1", "SGL", singleDateRange[0], singleDateRange[1]);
    }

    @Test
    @DisplayName("Should handle zero availability")
    void checkAvailability_WithZeroAvailability_ShouldReturnZero() {
        // Given
        AvailabilityRequest request = new AvailabilityRequest("H1", "20240901", "SGL");

        when(validationService.validateHotelExists("H1")).thenReturn(mockHotel);
        when(validationService.parseDateRange("20240901")).thenReturn(mockDateRange);
        when(availabilityCalculator.calculateMinimumAvailability(
                anyString(), anyString(), any(LocalDate.class), any(LocalDate.class))).thenReturn(0);

        // When
        AvailabilityResponse response = availabilityService.checkAvailability(request);

        // Then
        assertThat(response.availability()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should handle negative availability (overbooking)")
    void checkAvailability_WithOverbooking_ShouldReturnNegative() {
        // Given
        AvailabilityRequest request = new AvailabilityRequest("H1", "20240901", "SGL");

        when(validationService.validateHotelExists("H1")).thenReturn(mockHotel);
        when(validationService.parseDateRange("20240901")).thenReturn(mockDateRange);
        when(availabilityCalculator.calculateMinimumAvailability(
                anyString(), anyString(), any(LocalDate.class), any(LocalDate.class))).thenReturn(-2);

        // When
        AvailabilityResponse response = availabilityService.checkAvailability(request);

        // Then
        assertThat(response.availability()).isEqualTo(-2);
    }

    @Test
    @DisplayName("Should handle empty search results")
    void searchAvailability_WithNoAvailability_ShouldReturnEmptyResults() {
        try (MockedStatic<DateRangeUtil> mockedUtil = mockStatic(DateRangeUtil.class)) {
            // Given
            SearchRequest request = new SearchRequest("H1", 5, "DBL");
            List<DailyAvailability> emptyDailyAvailabilities = new ArrayList<>();
            List<DateRangeAvailability> emptyResults = new ArrayList<>();

            when(validationService.validateHotelExists("H1")).thenReturn(mockHotel);
            when(availabilityCalculator.findAvailableDates("H1", "DBL", 5))
                    .thenReturn(emptyDailyAvailabilities);
            mockedUtil.when(() -> DateRangeUtil.consolidateDateRanges(emptyDailyAvailabilities))
                    .thenReturn(emptyResults);

            // When
            SearchResponse response = availabilityService.searchAvailability(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.availabilities()).isEmpty();
            assertThat(response.totalResults()).isEqualTo(0);
        }
    }
}