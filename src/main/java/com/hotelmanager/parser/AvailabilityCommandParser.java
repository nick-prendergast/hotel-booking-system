package com.hotelmanager.parser;

import com.hotelmanager.exception.InvalidCommandException;
import com.hotelmanager.model.request.AvailabilityRequest;
import com.hotelmanager.service.RequestValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class AvailabilityCommandParser implements CommandParser<AvailabilityRequest> {
    private static final Pattern PATTERN = Pattern.compile(
            "Availability\\((?<hotelId>\\w+),\\s*(?<dateRange>[0-9-]+),\\s*(?<roomType>\\w+)\\)"
    );

    private final RequestValidationService validationService;


    @Override
    public boolean canParse(String input) {
        return PATTERN.matcher(input).matches();
    }

    @Override
    public AvailabilityRequest parse(String input) {
        Matcher matcher = PATTERN.matcher(input);
        if (!matcher.find()) {
            throw new InvalidCommandException("Invalid availability command");
        }

        AvailabilityRequest request = new AvailabilityRequest(
                matcher.group("hotelId"),
                matcher.group("dateRange"),
                matcher.group("roomType")
        );

        validateCreatedRequest(request);

        return request;
    }

    private void validateCreatedRequest(AvailabilityRequest request) {
        validationService.validate(request);
    }
}