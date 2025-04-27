package com.hotelmanager.parser;

import com.hotelmanager.exception.InvalidCommandException;
import com.hotelmanager.model.request.AvailabilityRequest;
import com.hotelmanager.service.validation.RequestValidationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AvailabilityCommandParserTest {

    @Mock
    private RequestValidationService validationService;

    @InjectMocks
    private AvailabilityCommandParser parser;

    @Test
    @DisplayName("Should return true for valid availability command")
    void canParse_WithValidCommand_ShouldReturnTrue() {
        // Given
        String command = "Availability(H1, 20240901, SGL)";

        // When
        boolean result = parser.canParse(command);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should return true for valid availability command with date range")
    void canParse_WithValidCommandWithDateRange_ShouldReturnTrue() {
        // Given
        String command = "Availability(H1, 20240901-20240903, SGL)";

        // When
        boolean result = parser.canParse(command);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should return false for invalid command format")
    void canParse_WithInvalidCommand_ShouldReturnFalse() {
        // Given
        String command = "InvalidCommand(H1, 20240901, SGL)";

        // When
        boolean result = parser.canParse(command);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should parse valid availability command")
    void parse_WithValidCommand_ShouldReturnRequest() {
        // Given
        String command = "Availability(H1, 20240901, SGL)";

        // When
        AvailabilityRequest result = parser.parse(command);

        // Then
        assertThat(result.hotelId()).isEqualTo("H1");
        assertThat(result.dateRange()).isEqualTo("20240901");
        assertThat(result.roomType()).isEqualTo("SGL");
        verify(validationService).validate(any(AvailabilityRequest.class));
    }

    @Test
    @DisplayName("Should parse command with extra spaces")
    void parse_WithExtraSpaces_ShouldReturnRequest() {
        // Given
        String command = "Availability(H1,  20240901,  SGL)";

        // When
        AvailabilityRequest result = parser.parse(command);

        // Then
        assertThat(result.hotelId()).isEqualTo("H1");
        assertThat(result.dateRange()).isEqualTo("20240901");
        assertThat(result.roomType()).isEqualTo("SGL");
    }

    @Test
    @DisplayName("Should throw exception for invalid command format")
    void parse_WithInvalidFormat_ShouldThrowException() {
        // Given
        String command = "InvalidCommand(H1, 20240901, SGL)";

        // When/Then
        assertThatThrownBy(() -> parser.parse(command))
                .isInstanceOf(InvalidCommandException.class)
                .hasMessage("Invalid availability command");
    }

    @Test
    @DisplayName("Should throw exception when validation fails")
    void parse_WhenValidationFails_ShouldThrowException() {
        // Given
        String command = "Availability(H1, 20240901, SGL)";
        doThrow(new InvalidCommandException("Validation error"))
                .when(validationService).validate(any(AvailabilityRequest.class));

        // When/Then
        assertThatThrownBy(() -> parser.parse(command))
                .isInstanceOf(InvalidCommandException.class)
                .hasMessage("Validation error");
    }
}
