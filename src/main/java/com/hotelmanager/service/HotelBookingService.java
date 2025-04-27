package com.hotelmanager.service;

import com.hotelmanager.model.CommandResult;
import com.hotelmanager.service.presentation.ConsoleOutputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Scanner;

@Service
@RequiredArgsConstructor
@Slf4j
public class HotelBookingService {

    private final CommandProcessor commandProcessor;
    private final ConsoleOutputService consoleOutputService;

    public void startCommandLoop() {
        Scanner scanner = new Scanner(System.in);

        log.info("Starting command loop for hotel booking system");
        consoleOutputService.displayWelcomeMessage();

        while (true) {
            consoleOutputService.displayPrompt();
            String command = scanner.nextLine();

            if (command.trim().isEmpty()) {
                log.info("Empty command received, exiting command loop");
                break;
            }

            CommandResult result = commandProcessor.processCommand(command);
            consoleOutputService.display(result);
        }

        log.info("Command loop ended");
        scanner.close();
    }
}