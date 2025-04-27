package com.hotelmanager.service;

import com.hotelmanager.model.DateRangeAvailability;
import com.hotelmanager.model.DailyAvailability;
import com.hotelmanager.model.Hotel;
import com.hotelmanager.model.request.AvailabilityRequest;
import com.hotelmanager.model.request.SearchRequest;
import com.hotelmanager.model.response.AvailabilityResponse;
import com.hotelmanager.model.response.SearchResponse;
import com.hotelmanager.util.DateRangeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private final ValidationService validationService;
    private final AvailabilityCalculator availabilityCalculator;

    public AvailabilityResponse checkAvailability(AvailabilityRequest request) {
        log.debug("Processing availability check: {}", request);

        try {
            Hotel hotel = validationService.validateHotelExists(request.hotelId());
            validationService.validateRoomTypeExists(hotel, request.roomType());
            LocalDate[] dateRange = validationService.validateAndParseDateRange(request.dateRange());

            int minAvailability = availabilityCalculator.calculateMinimumAvailability(
                    request.hotelId(), request.roomType(), dateRange[0], dateRange[1]);

            log.info("Availability check completed: hotelId={}, roomType={}, dateRange={}, availability={}",
                    request.hotelId(), request.roomType(), request.dateRange(), minAvailability);

            return new AvailabilityResponse(
                    request.hotelId(),
                    request.roomType(),
                    request.dateRange(),
                    minAvailability,
                    "Success"
            );

        } catch (Exception e) {
            log.error("Availability check failed: request={}", request, e);
            return new AvailabilityResponse(
                    request.hotelId(),
                    request.roomType(),
                    request.dateRange(),
                    0,
                    e.getMessage()
            );
        }
    }

    public SearchResponse searchAvailability(SearchRequest request) {
        log.debug("Processing availability search: {}", request);

        try {
            Hotel hotel = validationService.validateHotelExists(request.hotelId());
            validationService.validateRoomTypeExists(hotel, request.roomType());
            validationService.validateDaysAhead(request.daysAhead());

            List<DailyAvailability> dailyAvailabilities =
                    availabilityCalculator.findAvailableDates(
                            request.hotelId(), request.roomType(), request.daysAhead());

            List<DateRangeAvailability> results = DateRangeUtil.consolidateDateRanges(dailyAvailabilities);

            log.info("Availability search completed: hotelId={}, roomType={}, daysAhead={}, resultCount={}",
                    request.hotelId(), request.roomType(), request.daysAhead(), results.size());

            return new SearchResponse(results, results.size());

        } catch (Exception e) {
            log.error("Availability search failed: request={}", request, e);
            return new SearchResponse(List.of(), 0);
        }
    }
}