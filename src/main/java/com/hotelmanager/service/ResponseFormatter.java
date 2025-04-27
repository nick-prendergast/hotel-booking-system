package com.hotelmanager.service;

import com.hotelmanager.model.DateRangeAvailability;
import com.hotelmanager.model.response.AvailabilityResponse;
import com.hotelmanager.model.response.SearchResponse;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class ResponseFormatter {

    public String formatSearchResponse(SearchResponse response) {
        if (response.availabilities().isEmpty()) {
            return "";
        }
        return response.availabilities().stream()
                .map(DateRangeAvailability::toString)
                .collect(Collectors.joining(", "));
    }

    public String formatAvailabilityResponse(AvailabilityResponse response) {
        return response.toString();
    }
}