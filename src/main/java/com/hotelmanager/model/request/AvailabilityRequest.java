package com.hotelmanager.model.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record AvailabilityRequest(
        @NotNull(message = "{error.availability.hotelId.notNull}")
        String hotelId,

        @NotNull(message = "{error.availability.dateRange.notNull}")
        @Pattern(regexp = "\\d{8}(-\\d{8})?", message = "{error.availability.dateRange.pattern}")
        String dateRange,

        @NotNull(message = "{error.availability.roomType.notNull}")
        String roomType
) {}