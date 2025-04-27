package com.hotelmanager.model;

import java.time.LocalDate;

public record DailyAvailability(LocalDate date, int availability) {
}