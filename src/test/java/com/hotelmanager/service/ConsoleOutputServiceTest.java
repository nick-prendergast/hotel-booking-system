package com.hotelmanager.service;

import com.hotelmanager.model.CommandResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;

class ConsoleOutputServiceTest {

    private ConsoleOutputService consoleOutputService;
    private ByteArrayOutputStream outputStream;
    private ByteArrayOutputStream errorStream;
    private PrintStream originalOut;
    private PrintStream originalErr;

    @BeforeEach
    void setUp() {
        consoleOutputService = new ConsoleOutputService();

        // Capture System.out
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        // Capture System.err
        errorStream = new ByteArrayOutputStream();
        originalErr = System.err;
        System.setErr(new PrintStream(errorStream));
    }

    @AfterEach
    void tearDown() {
        // Restore original streams
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    @DisplayName("Should display successful command result to standard output")
    void display_WithSuccessfulResult_ShouldPrintToStdOut() {
        // Given
        CommandResult result = CommandResult.success("Room available");

        // When
        consoleOutputService.display(result);

        // Then
        assertThat(outputStream.toString()).contains("Room available");
        assertThat(errorStream.toString()).isEmpty();
    }

    @Test
    @DisplayName("Should display error command result to standard error")
    void display_WithErrorResult_ShouldPrintToStdErr() {
        // Given
        CommandResult result = CommandResult.error("Invalid command");

        // When
        consoleOutputService.display(result);

        // Then
        assertThat(errorStream.toString()).contains("Error: Invalid command");
        assertThat(outputStream.toString()).isEmpty();
    }

    @Test
    @DisplayName("Should display error message to standard error")
    void displayError_ShouldPrintToStdErr() {
        // Given
        String errorMessage = "File not found";

        // When
        consoleOutputService.displayError(errorMessage);

        // Then
        assertThat(errorStream.toString()).contains("Error: File not found");
        assertThat(outputStream.toString()).isEmpty();
    }

    @Test
    @DisplayName("Should display welcome message with all information")
    void displayWelcomeMessage_ShouldPrintCompleteWelcomeMessage() {
        // When
        consoleOutputService.displayWelcomeMessage();

        // Then
        String output = outputStream.toString();
        assertThat(output).contains("Hotel Room Availability Manager");
        assertThat(output).contains("Commands: Availability(hotelId, date, roomType) or Search(hotelId, days, roomType)");
        assertThat(output).contains("Enter a blank line to exit");
        assertThat(errorStream.toString()).isEmpty();
    }

    @Test
    @DisplayName("Should display prompt with newline and greater-than symbol")
    void displayPrompt_ShouldPrintPromptSymbol() {
        // When
        consoleOutputService.displayPrompt();

        // Then
        assertThat(outputStream.toString()).isEqualTo("\n> ");
        assertThat(errorStream.toString()).isEmpty();
    }

    @Test
    @DisplayName("Should handle null output in successful result")
    void display_WithNullSuccessOutput_ShouldNotThrowException() {
        // Given
        CommandResult result = CommandResult.success(null);

        // When
        consoleOutputService.display(result);

        // Then
        assertThat(outputStream.toString()).contains("null");
        assertThat(errorStream.toString()).isEmpty();
    }

    @Test
    @DisplayName("Should handle null error message in error result")
    void display_WithNullErrorMessage_ShouldNotThrowException() {
        // Given
        CommandResult result = CommandResult.error(null);

        // When
        consoleOutputService.display(result);

        // Then
        assertThat(errorStream.toString()).contains("Error: null");
        assertThat(outputStream.toString()).isEmpty();
    }

    @Test
    @DisplayName("Should handle empty error message")
    void displayError_WithEmptyMessage_ShouldPrintErrorPrefix() {
        // Given
        String emptyMessage = "";

        // When
        consoleOutputService.displayError(emptyMessage);

        // Then
        assertThat(errorStream.toString()).isEqualTo("Error: \n");
        assertThat(outputStream.toString()).isEmpty();
    }

    @Test
    @DisplayName("Should handle multiline output in command result")
    void display_WithMultilineOutput_ShouldPrintAllLines() {
        // Given
        String multilineOutput = "Line 1\nLine 2\nLine 3";
        CommandResult result = CommandResult.success(multilineOutput);

        // When
        consoleOutputService.display(result);

        // Then
        assertThat(outputStream.toString()).contains("Line 1\nLine 2\nLine 3");
        assertThat(errorStream.toString()).isEmpty();
    }

    @Test
    @DisplayName("Should maintain proper line endings in output")
    void display_ShouldMaintainLineEndings() {
        // Given
        CommandResult result = CommandResult.success("Test output");

        // When
        consoleOutputService.display(result);

        // Then
        assertThat(outputStream.toString()).isEqualTo("Test output\n");
    }
}