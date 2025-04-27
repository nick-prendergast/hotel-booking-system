package com.hotelmanager.model.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SearchRequest(
        @NotNull(message = "{error.search.hotelId.notNull}")
        String hotelId,

        @Positive(message = "{error.search.daysAhead.positive}")
        int daysAhead,

        @NotNull(message = "{error.search.roomType.notNull}")
        String roomType
) {}
