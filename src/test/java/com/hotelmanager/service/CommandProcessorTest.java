package com.hotelmanager.service;

import com.hotelmanager.exception.BookingSystemException;
import com.hotelmanager.exception.InvalidCommandException;
import com.hotelmanager.model.CommandResult;
import com.hotelmanager.model.request.AvailabilityRequest;
import com.hotelmanager.model.request.SearchRequest;
import com.hotelmanager.model.response.AvailabilityResponse;
import com.hotelmanager.model.response.SearchResponse;
import com.hotelmanager.parser.AvailabilityCommandParser;
import com.hotelmanager.parser.SearchCommandParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommandProcessorTest {

    @Mock
    private AvailabilityCommandParser availabilityParser;

    @Mock
    private SearchCommandParser searchParser;

    @Mock
    private AvailabilityService availabilityService;

    @Mock
    private ResponseFormatter responseFormatter;

    @InjectMocks
    private CommandProcessor commandProcessor;

    private AvailabilityRequest mockAvailabilityRequest;
    private SearchRequest mockSearchRequest;
    private AvailabilityResponse mockAvailabilityResponse;
    private SearchResponse mockSearchResponse;

    @BeforeEach
    void setUp() {
        mockAvailabilityRequest = new AvailabilityRequest("H1", "20240901", "SGL");
        mockAvailabilityResponse = new AvailabilityResponse("H1", "SGL", "20240901", 2, "Success");

        mockSearchRequest = new SearchRequest("H1", 5, "DBL");
        mockSearchResponse = new SearchResponse(null, 0);
    }

    @Test
    @DisplayName("Should return error for null command")
    void processCommand_WithNullCommand_ShouldReturnError() {
        // When
        CommandResult result = commandProcessor.processCommand(null);

        // Then
        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).isEqualTo("Empty command");
        assertThat(result.output()).isNull();
        verifyNoInteractions(availabilityParser, searchParser, availabilityService, responseFormatter);
    }

    @Test
    @DisplayName("Should return error for empty command")
    void processCommand_WithEmptyCommand_ShouldReturnError() {
        // When
        CommandResult result = commandProcessor.processCommand("");

        // Then
        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).isEqualTo("Empty command");
        assertThat(result.output()).isNull();
        verifyNoInteractions(availabilityParser, searchParser, availabilityService, responseFormatter);
    }

    @Test
    @DisplayName("Should return error for command with only whitespace")
    void processCommand_WithWhitespaceCommand_ShouldReturnError() {
        // When
        CommandResult result = commandProcessor.processCommand("   ");

        // Then
        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).isEqualTo("Empty command");
        assertThat(result.output()).isNull();
        verifyNoInteractions(availabilityParser, searchParser, availabilityService, responseFormatter);
    }

    @Test
    @DisplayName("Should process availability command successfully")
    void processCommand_WithValidAvailabilityCommand_ShouldProcessSuccessfully() {
        // Given
        String command = "Availability(H1, 20240901, SGL)";
        String formattedOutput = "Room available";

        when(availabilityParser.canParse(command)).thenReturn(true);
        when(availabilityParser.parse(command)).thenReturn(mockAvailabilityRequest);
        when(availabilityService.checkAvailability(mockAvailabilityRequest)).thenReturn(mockAvailabilityResponse);
        when(responseFormatter.formatAvailabilityResponse(mockAvailabilityResponse)).thenReturn(formattedOutput);

        // When
        CommandResult result = commandProcessor.processCommand(command);

        // Then
        assertThat(result.success()).isTrue();
        assertThat(result.output()).isEqualTo(formattedOutput);
        assertThat(result.errorMessage()).isNull();

        verify(availabilityParser).canParse(command);
        verify(availabilityParser).parse(command);
        verify(availabilityService).checkAvailability(mockAvailabilityRequest);
        verify(responseFormatter).formatAvailabilityResponse(mockAvailabilityResponse);
    }

    @Test
    @DisplayName("Should process search command successfully")
    void processCommand_WithValidSearchCommand_ShouldProcessSuccessfully() {
        // Given
        String command = "Search(H1, 5, DBL)";
        String formattedOutput = "Search results";

        when(availabilityParser.canParse(command)).thenReturn(false);
        when(searchParser.canParse(command)).thenReturn(true);
        when(searchParser.parse(command)).thenReturn(mockSearchRequest);
        when(availabilityService.searchAvailability(mockSearchRequest)).thenReturn(mockSearchResponse);
        when(responseFormatter.formatSearchResponse(mockSearchResponse)).thenReturn(formattedOutput);

        // When
        CommandResult result = commandProcessor.processCommand(command);

        // Then
        assertThat(result.success()).isTrue();
        assertThat(result.output()).isEqualTo(formattedOutput);
        assertThat(result.errorMessage()).isNull();

        verify(availabilityParser).canParse(command);
        verify(searchParser).canParse(command);
        verify(searchParser).parse(command);
        verify(availabilityService).searchAvailability(mockSearchRequest);
        verify(responseFormatter).formatSearchResponse(mockSearchResponse);
    }

    @Test
    @DisplayName("Should return error for invalid command format")
    void processCommand_WithInvalidCommand_ShouldReturnError() {
        // Given
        String command = "InvalidCommand";

        when(availabilityParser.canParse(command)).thenReturn(false);
        when(searchParser.canParse(command)).thenReturn(false);

        // When
        CommandResult result = commandProcessor.processCommand(command);

        // Then
        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).isEqualTo("Invalid command format");
        assertThat(result.output()).isNull();

        verify(availabilityParser).canParse(command);
        verify(searchParser).canParse(command);
        verifyNoInteractions(availabilityService, responseFormatter);
    }

    @Test
    @DisplayName("Should handle exceptions from availability parser")
    void processCommand_WithParserException_ShouldReturnError() {
        // Given
        String command = "Availability(H1, 20240901, SGL)";
        String errorMessage = "Invalid date format";

        when(availabilityParser.canParse(command)).thenReturn(true);
        when(availabilityParser.parse(command)).thenThrow(new InvalidCommandException(errorMessage));

        // When
        CommandResult result = commandProcessor.processCommand(command);

        // Then
        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).isEqualTo(errorMessage);
        assertThat(result.output()).isNull();

        verify(availabilityParser).canParse(command);
        verify(availabilityParser).parse(command);
        verifyNoInteractions(availabilityService, responseFormatter);
    }

    @Test
    @DisplayName("Should handle exceptions from availability service")
    void processCommand_WithServiceException_ShouldReturnError() {
        // Given
        String command = "Availability(H1, 20240901, SGL)";
        String errorMessage = "Hotel not found";

        when(availabilityParser.canParse(command)).thenReturn(true);
        when(availabilityParser.parse(command)).thenReturn(mockAvailabilityRequest);
        when(availabilityService.checkAvailability(mockAvailabilityRequest))
                .thenThrow(new BookingSystemException(errorMessage) {
                });

        // When
        CommandResult result = commandProcessor.processCommand(command);

        // Then
        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).isEqualTo(errorMessage);
        assertThat(result.output()).isNull();

        verify(availabilityParser).canParse(command);
        verify(availabilityParser).parse(command);
        verify(availabilityService).checkAvailability(mockAvailabilityRequest);
        verifyNoInteractions(responseFormatter);
    }

    @Test
    @DisplayName("Should handle exceptions from search service")
    void processCommand_WithSearchServiceException_ShouldReturnError() {
        // Given
        String command = "Search(H1, 5, DBL)";
        String errorMessage = "Room type not found";

        when(availabilityParser.canParse(command)).thenReturn(false);
        when(searchParser.canParse(command)).thenReturn(true);
        when(searchParser.parse(command)).thenReturn(mockSearchRequest);
        when(availabilityService.searchAvailability(mockSearchRequest))
                .thenThrow(new BookingSystemException(errorMessage) {
                });

        // When
        CommandResult result = commandProcessor.processCommand(command);

        // Then
        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).isEqualTo(errorMessage);
        assertThat(result.output()).isNull();

        verify(availabilityParser).canParse(command);
        verify(searchParser).canParse(command);
        verify(searchParser).parse(command);
        verify(availabilityService).searchAvailability(mockSearchRequest);
        verifyNoInteractions(responseFormatter);
    }

    @Test
    @DisplayName("Should not catch runtime exceptions")
    void processCommand_WithRuntimeException_ShouldThrowException() {
        // Given
        String command = "Availability(H1, 20240901, SGL)";
        String exceptionMessage = "Unexpected runtime error";

        when(availabilityParser.canParse(command)).thenReturn(true);
        when(availabilityParser.parse(command)).thenThrow(new RuntimeException(exceptionMessage));

        // When/Then
        assertThatThrownBy(() -> commandProcessor.processCommand(command))
                .isInstanceOf(RuntimeException.class)
                .hasMessage(exceptionMessage);

        verify(availabilityParser).canParse(command);
        verify(availabilityParser).parse(command);
        verifyNoInteractions(availabilityService, responseFormatter);
    }
}