package com.hotelmanager.service;

import com.hotelmanager.model.DateRangeAvailability;
import com.hotelmanager.model.response.AvailabilityResponse;
import com.hotelmanager.model.response.SearchResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ResponseFormatterTest {

    private ResponseFormatter responseFormatter;

    @BeforeEach
    void setUp() {
        responseFormatter = new ResponseFormatter();
    }

    @Test
    @DisplayName("Should format search response with multiple availabilities")
    void formatSearchResponse_WithMultipleAvailabilities_ShouldReturnFormattedString() {
        // Given
        List<DateRangeAvailability> availabilities = List.of(
                new DateRangeAvailability(
                        LocalDate.of(2024, 11, 1),
                        LocalDate.of(2024, 11, 3),
                        2
                ),
                new DateRangeAvailability(
                        LocalDate.of(2024, 12, 3),
                        LocalDate.of(2024, 12, 10),
                        1
                )
        );
        SearchResponse response = new SearchResponse(availabilities, 2);

        // When
        String result = responseFormatter.formatSearchResponse(response);

        // Then
        assertThat(result).isEqualTo("(20241101-20241103, 2), (20241203-20241210, 1)");
    }

    @Test
    @DisplayName("Should return empty string for search response with no availabilities")
    void formatSearchResponse_WithNoAvailabilities_ShouldReturnEmptyString() {
        // Given
        SearchResponse response = new SearchResponse(new ArrayList<>(), 0);

        // When
        String result = responseFormatter.formatSearchResponse(response);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should format search response with single availability")
    void formatSearchResponse_WithSingleAvailability_ShouldReturnFormattedString() {
        // Given
        List<DateRangeAvailability> availabilities = List.of(
                new DateRangeAvailability(
                        LocalDate.of(2024, 11, 1),
                        LocalDate.of(2024, 11, 3),
                        2
                )
        );
        SearchResponse response = new SearchResponse(availabilities, 1);

        // When
        String result = responseFormatter.formatSearchResponse(response);

        // Then
        assertThat(result).isEqualTo("(20241101-20241103, 2)");
    }

    @Test
    @DisplayName("Should format availability response")
    void formatAvailabilityResponse_ShouldReturnToStringResult() {
        // Given
        AvailabilityResponse response = new AvailabilityResponse(
                "H1",
                "SGL",
                "20240901-20240903",
                1,
                "Success"
        );

        // When
        String result = responseFormatter.formatAvailabilityResponse(response);

        // Then
        assertThat(result).isEqualTo("Hotel: H1, Room Type: SGL, Date Range: 20240901-20240903, Available Rooms: 1");
    }
}