package com.hotelmanager.model;

public record CommandResult(
        boolean success,
        String output,
        String errorMessage
) {
    public static CommandResult success(String output) {
        return new CommandResult(true, output, null);
    }

    public static CommandResult error(String errorMessage) {
        return new CommandResult(false, null, errorMessage);
    }
}