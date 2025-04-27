package com.hotelmanager.model.response;

import com.hotelmanager.model.DateRangeAvailability;

import java.util.List;

public record SearchResponse(
        List<DateRangeAvailability> availabilities,
        int totalResults
) {
}
