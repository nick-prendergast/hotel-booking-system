package com.hotelmanager.service;

import com.hotelmanager.exception.BookingSystemException;
import com.hotelmanager.model.CommandResult;
import com.hotelmanager.model.request.AvailabilityRequest;
import com.hotelmanager.model.request.SearchRequest;
import com.hotelmanager.model.response.AvailabilityResponse;
import com.hotelmanager.model.response.SearchResponse;
import com.hotelmanager.parser.AvailabilityCommandParser;
import com.hotelmanager.parser.SearchCommandParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommandProcessor {
    private final AvailabilityCommandParser availabilityParser;
    private final SearchCommandParser searchParser;
    private final AvailabilityService availabilityService;
    private final ResponseFormatter responseFormatter;

    public CommandResult processCommand(String command) {
        if (command == null || command.trim().isEmpty()) {
            return CommandResult.error("Empty command");
        }

        try {
            if (availabilityParser.canParse(command)) {
                return processAvailabilityCommand(command);
            }

            if (searchParser.canParse(command)) {
                return processSearchCommand(command);
            }

            return CommandResult.error("Invalid command format");

        } catch (BookingSystemException e) {
            log.error("Error processing command: {}", command, e);
            return CommandResult.error(e.getMessage());
        }
    }

    private CommandResult processAvailabilityCommand(String command) {
        AvailabilityRequest request = availabilityParser.parse(command);
        AvailabilityResponse response = availabilityService.checkAvailability(request);
        String output = responseFormatter.formatAvailabilityResponse(response);
        return CommandResult.success(output);
    }

    private CommandResult processSearchCommand(String command) {
        SearchRequest request = searchParser.parse(command);
        SearchResponse response = availabilityService.searchAvailability(request);
        String output = responseFormatter.formatSearchResponse(response);
        return CommandResult.success(output);
    }
}