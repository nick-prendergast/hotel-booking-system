package com.hotelmanager.parser;

import com.hotelmanager.exception.InvalidCommandException;
import com.hotelmanager.model.request.SearchRequest;
import com.hotelmanager.service.RequestValidationService;
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
class SearchCommandParserTest {

    @Mock
    private RequestValidationService validationService;

    @InjectMocks
    private SearchCommandParser parser;

    @Test
    @DisplayName("Should return true for valid search command")
    void canParse_WithValidCommand_ShouldReturnTrue() {
        // Given
        String command = "Search(H1, 365, SGL)";

        // When
        boolean result = parser.canParse(command);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should return false for invalid command format")
    void canParse_WithInvalidCommand_ShouldReturnFalse() {
        // Given
        String command = "InvalidCommand(H1, 365, SGL)";

        // When
        boolean result = parser.canParse(command);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should parse valid search command")
    void parse_WithValidCommand_ShouldReturnRequest() {
        // Given
        String command = "Search(H1, 365, SGL)";

        // When
        SearchRequest result = parser.parse(command);

        // Then
        assertThat(result.hotelId()).isEqualTo("H1");
        assertThat(result.daysAhead()).isEqualTo(365);
        assertThat(result.roomType()).isEqualTo("SGL");
        verify(validationService).validate(any(SearchRequest.class));
    }

    @Test
    @DisplayName("Should parse command with extra spaces")
    void parse_WithExtraSpaces_ShouldReturnRequest() {
        // Given
        String command = "Search(H1,  365,  SGL)";

        // When
        SearchRequest result = parser.parse(command);

        // Then
        assertThat(result.hotelId()).isEqualTo("H1");
        assertThat(result.daysAhead()).isEqualTo(365);
        assertThat(result.roomType()).isEqualTo("SGL");
    }

    @Test
    @DisplayName("Should throw exception for invalid command format")
    void parse_WithInvalidFormat_ShouldThrowException() {
        // Given
        String command = "InvalidCommand(H1, 365, SGL)";

        // When/Then
        assertThatThrownBy(() -> parser.parse(command))
                .isInstanceOf(InvalidCommandException.class)
                .hasMessage("Invalid search command");
    }

    @Test
    @DisplayName("Should throw exception for invalid days ahead format")
    void parse_WithInvalidDaysAhead_ShouldThrowException() {
        // Given
        String command = "Search(H1, notanumber, SGL)";

        // When/Then
        assertThatThrownBy(() -> parser.parse(command))
                .isInstanceOf(InvalidCommandException.class)
                .hasMessage("Invalid search command");
    }

    @Test
    @DisplayName("Should throw exception when validation fails")
    void parse_WhenValidationFails_ShouldThrowException() {
        // Given
        String command = "Search(H1, 365, SGL)";
        doThrow(new InvalidCommandException("Validation error"))
                .when(validationService).validate(any(SearchRequest.class));

        // When/Then
        assertThatThrownBy(() -> parser.parse(command))
                .isInstanceOf(InvalidCommandException.class)
                .hasMessage("Validation error");
    }

    @Test
    @DisplayName("Should throw exception for malformed days ahead value")
    void parse_WithMalformedDaysAhead_ShouldThrowException() {
        // Given
        String command = "Search(H1, 123abc, SGL)";

        // When/Then - This will not match the regex pattern
        assertThatThrownBy(() -> parser.parse(command))
                .isInstanceOf(InvalidCommandException.class)
                .hasMessage("Invalid search command");
    }
}