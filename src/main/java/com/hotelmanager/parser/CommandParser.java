package com.hotelmanager.parser;

public interface CommandParser<T> {
    boolean canParse(String input);

    T parse(String input);
}
