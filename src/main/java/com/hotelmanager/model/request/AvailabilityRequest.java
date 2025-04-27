package com.hotelmanager.model.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AvailabilityRequest(
        @NotBlank(message = "Hotel ID is required")
        String hotelId,

        @NotBlank(message = "Date range is required")
        @Pattern(regexp = "\\d{8}(-\\d{8})?", message = "{error.availability.dateRange.pattern}")
        String dateRange,

        @NotBlank(message = "Room type is required")
        String roomType
) {
}