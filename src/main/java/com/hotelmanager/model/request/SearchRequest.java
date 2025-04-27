package com.hotelmanager.model.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record SearchRequest(
        @NotBlank(message = "Hotel ID is required")
        String hotelId,

        @Positive(message = "Days ahead must be positive")
        int daysAhead,

        @NotBlank(message = "Room type is required")
        String roomType
) {
}
