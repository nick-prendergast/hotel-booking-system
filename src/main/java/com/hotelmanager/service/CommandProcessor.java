package com.hotelmanager.service;

import com.hotelmanager.exception.BookingSystemException;
import com.hotelmanager.model.CommandResult;
import com.hotelmanager.model.DateRangeAvailability;
import com.hotelmanager.model.request.AvailabilityRequest;
import com.hotelmanager.model.request.SearchRequest;
import com.hotelmanager.model.response.AvailabilityResponse;
import com.hotelmanager.model.response.SearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommandProcessor {

    private final AvailabilityService availabilityService;
    private static final Pattern AVAILABILITY_PATTERN =
            Pattern.compile("Availability\\((\\w+),\\s*([0-9-]+),\\s*(\\w+)\\)");
    private static final Pattern SEARCH_PATTERN =
            Pattern.compile("Search\\((\\w+),\\s*(\\d+),\\s*(\\w+)\\)");

    public CommandResult processCommand(String command) {
        if (command == null || command.trim().isEmpty()) {
            // This check is now redundant since HotelBookingService
            // already checks for empty commands, but keeping for safety
            return CommandResult.error("Empty command");
        }

        log.debug("Processing command: {}", command);

        try {
            Matcher availabilityMatcher = AVAILABILITY_PATTERN.matcher(command);
            if (availabilityMatcher.find()) {
                return processAvailabilityCommand(availabilityMatcher);
            }

            Matcher searchMatcher = SEARCH_PATTERN.matcher(command);
            if (searchMatcher.find()) {
                return processSearchCommand(searchMatcher);
            }

            log.warn("Invalid command format: {}", command);
            return CommandResult.error("Invalid command");

        } catch (Exception e) {
            log.error("Error processing command: {}", command, e);
            return CommandResult.error(e.getMessage());
        }
    }

    private CommandResult processAvailabilityCommand(Matcher matcher) {
        AvailabilityRequest request = new AvailabilityRequest(
                matcher.group(1),
                matcher.group(2),
                matcher.group(3)
        );

        log.info("Processing availability command: {}", request);

        AvailabilityResponse response = availabilityService.checkAvailability(request);

        return CommandResult.success(response.toString());
    }

    private CommandResult processSearchCommand(Matcher matcher) {
        try {
            int daysAhead = Integer.parseInt(matcher.group(2));

            SearchRequest request = new SearchRequest(
                    matcher.group(1),
                    daysAhead,
                    matcher.group(3)
            );

            log.info("Processing search command: {}", request);

            SearchResponse response = availabilityService.searchAvailability(request);

            if (response.availabilities().isEmpty()) {
                return CommandResult.success("");
            }

            String result = response.availabilities().stream()
                    .map(DateRangeAvailability::toString)
                    .collect(Collectors.joining(", "));

            return CommandResult.success(result);

        } catch (NumberFormatException e) {
            log.error("Invalid number format in search command", e);
            return CommandResult.error("Invalid days ahead format");
        }
    }
}