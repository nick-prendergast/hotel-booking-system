package com.hotelmanager.service;

import com.hotelmanager.model.CommandResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HotelBookingServiceTest {

    @Mock
    private CommandProcessor commandProcessor;

    @Mock
    private ConsoleOutputService consoleOutputService;

    @InjectMocks
    private HotelBookingService hotelBookingService;

    private InputStream originalSystemIn;

    @BeforeEach
    void setUp() {
        // Store the original System.in to restore it after each test
        originalSystemIn = System.in;
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        // Restore the original System.in
        System.setIn(originalSystemIn);
    }

    @Test
    @DisplayName("Should display welcome message when starting command loop")
    void startCommandLoop_ShouldDisplayWelcomeMessage() {
        // Given
        String input = "\n"; // Just a newline to exit immediately
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        // When
        hotelBookingService.startCommandLoop();

        // Then
        verify(consoleOutputService).displayWelcomeMessage();
    }

    @Test
    @DisplayName("Should exit loop when empty command is received")
    void startCommandLoop_WithEmptyCommand_ShouldExitLoop() {
        // Given
        String input = "\n"; // Just a newline
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        // When
        hotelBookingService.startCommandLoop();

        // Then
        verify(consoleOutputService).displayWelcomeMessage();
        verify(consoleOutputService).displayPrompt();
        verifyNoMoreInteractions(commandProcessor);
    }

    @Test
    @DisplayName("Should process single command and display result")
    void startCommandLoop_WithSingleCommand_ShouldProcessAndDisplay() {
        // Given
        String input = "Availability(H1, 20240901, SGL)\n\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        CommandResult commandResult = CommandResult.success("Room available");
        when(commandProcessor.processCommand("Availability(H1, 20240901, SGL)"))
                .thenReturn(commandResult);

        // When
        hotelBookingService.startCommandLoop();

        // Then
        verify(consoleOutputService).displayWelcomeMessage();
        verify(consoleOutputService, times(2)).displayPrompt();
        verify(commandProcessor).processCommand("Availability(H1, 20240901, SGL)");
        verify(consoleOutputService).display(commandResult);
    }

    @Test
    @DisplayName("Should process multiple commands until empty line")
    void startCommandLoop_WithMultipleCommands_ShouldProcessAll() {
        // Given
        String input = """
                Availability(H1, 20240901, SGL)
                Search(H1, 5, DBL)
                
                """;
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        CommandResult result1 = CommandResult.success("Room available");
        CommandResult result2 = CommandResult.success("Search results");

        when(commandProcessor.processCommand("Availability(H1, 20240901, SGL)"))
                .thenReturn(result1);
        when(commandProcessor.processCommand("Search(H1, 5, DBL)"))
                .thenReturn(result2);

        // When
        hotelBookingService.startCommandLoop();

        // Then
        verify(consoleOutputService).displayWelcomeMessage();
        verify(consoleOutputService, times(3)).displayPrompt();

        verify(commandProcessor).processCommand("Availability(H1, 20240901, SGL)");
        verify(consoleOutputService).display(result1);

        verify(commandProcessor).processCommand("Search(H1, 5, DBL)");
        verify(consoleOutputService).display(result2);
    }

    @Test
    @DisplayName("Should handle error results from command processor")
    void startCommandLoop_WithErrorResult_ShouldDisplayError() {
        // Given
        String input = "InvalidCommand\n\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        CommandResult errorResult = CommandResult.error("Invalid command format");
        when(commandProcessor.processCommand("InvalidCommand"))
                .thenReturn(errorResult);

        // When
        hotelBookingService.startCommandLoop();

        // Then
        verify(consoleOutputService).displayWelcomeMessage();
        verify(consoleOutputService, times(2)).displayPrompt();
        verify(commandProcessor).processCommand("InvalidCommand");
        verify(consoleOutputService).display(errorResult);
    }

    @Test
    @DisplayName("Should exit loop when whitespace-only command is received")
    void startCommandLoop_WithWhitespaceCommand_ShouldExitLoop() {
        // Given
        String input = "   \n"; // Spaces followed by newline
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        // When
        hotelBookingService.startCommandLoop();

        // Then
        verify(consoleOutputService).displayWelcomeMessage();
        verify(consoleOutputService).displayPrompt();
        verifyNoInteractions(commandProcessor);
    }

    @Test
    @DisplayName("Should continue processing after error")
    void startCommandLoop_WithErrorThenValidCommand_ShouldContinueProcessing() {
        // Given
        String input = """
                InvalidCommand
                Availability(H1, 20240901, SGL)
                
                """;
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        CommandResult errorResult = CommandResult.error("Invalid command");
        CommandResult successResult = CommandResult.success("Room available");

        when(commandProcessor.processCommand("InvalidCommand"))
                .thenReturn(errorResult);
        when(commandProcessor.processCommand("Availability(H1, 20240901, SGL)"))
                .thenReturn(successResult);

        // When
        hotelBookingService.startCommandLoop();

        // Then
        verify(consoleOutputService).displayWelcomeMessage();
        verify(consoleOutputService, times(3)).displayPrompt();

        verify(commandProcessor).processCommand("InvalidCommand");
        verify(consoleOutputService).display(errorResult);

        verify(commandProcessor).processCommand("Availability(H1, 20240901, SGL)");
        verify(consoleOutputService).display(successResult);
    }

    @Test
    @DisplayName("Should handle non-trimmed input correctly")
    void startCommandLoop_WithNonTrimmedCommand_ShouldProcessNormally() {
        // Given
        String input = " Availability(H1, 20240901, SGL) \n\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        CommandResult commandResult = CommandResult.success("Room available");
        when(commandProcessor.processCommand(" Availability(H1, 20240901, SGL) "))
                .thenReturn(commandResult);

        // When
        hotelBookingService.startCommandLoop();

        // Then
        verify(consoleOutputService).displayWelcomeMessage();
        verify(consoleOutputService, times(2)).displayPrompt();
        verify(commandProcessor).processCommand(" Availability(H1, 20240901, SGL) ");
        verify(consoleOutputService).display(commandResult);
    }


}