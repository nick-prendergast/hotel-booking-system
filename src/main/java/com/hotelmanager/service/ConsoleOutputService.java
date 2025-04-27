package com.hotelmanager.service;

import com.hotelmanager.model.CommandResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ConsoleOutputService {

    public void display(CommandResult result) {
        if (result.success()) {
            System.out.println(result.output());
        } else {
            System.err.println("Error: " + result.errorMessage());
        }
    }

    public void displayError(String message) {
        System.err.println("Error: " + message);
    }

    public void displayWelcomeMessage() {
        System.out.println("Hotel Room Availability Manager");
        System.out.println("Commands: Availability(hotelId, date, roomType) or Search(hotelId, days, roomType)");
        System.out.println("Enter a blank line to exit");
    }

    public void displayPrompt() {
        System.out.print("\n> ");
    }
}