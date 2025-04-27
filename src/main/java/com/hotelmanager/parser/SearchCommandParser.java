package com.hotelmanager.parser;

import com.hotelmanager.exception.InvalidCommandException;
import com.hotelmanager.model.request.SearchRequest;
import com.hotelmanager.service.validation.RequestValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class SearchCommandParser implements CommandParser<SearchRequest> {
    private static final Pattern PATTERN = Pattern.compile(
            "Search\\((?<hotelId>\\w+),\\s*(?<daysAhead>\\d+),\\s*(?<roomType>\\w+)\\)"
    );

    private final RequestValidationService validationService;


    @Override
    public boolean canParse(String input) {
        return PATTERN.matcher(input).matches();
    }

    @Override
    public SearchRequest parse(String input) {
        Matcher matcher = PATTERN.matcher(input);
        if (!matcher.find()) {
            throw new InvalidCommandException("Invalid search command");
        }

        try {
            SearchRequest request = new SearchRequest(
                    matcher.group("hotelId"),
                    Integer.parseInt(matcher.group("daysAhead")),
                    matcher.group("roomType")
            );

            validateCreatedRequest(request);

            return request;

        } catch (NumberFormatException e) {
            throw new InvalidCommandException("Invalid days ahead format");
        }
    }

    private void validateCreatedRequest(SearchRequest request) {
        validationService.validate(request);
    }
}
