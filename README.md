# Hotel Room Booking Management System

A command-line application to manage hotel room availability and reservations, built with Java Spring Boot.

## Features

- Check room availability for specific dates and room types
- Search for available rooms across a date range
- Support for multiple hotels and room types
- Handles overbooking scenarios
- JSON-based data storage

## Technologies Used

- Java 21
- Spring Boot 3.4.5
- Maven
- Docker
- JUnit 5, Mockito, AssertJ (Testing)

## Prerequisites

- Docker

## Quick Start

1. Clone the repository:
   ```bash
   git clone https://github.com/nick-prendergast/hotel-booking-system
   cd hotel-booking-system
   ```

2. Run the application:
   ```bash
   docker-compose run --rm app --hotels hotels.json --bookings bookings.json
   ```

3. Start using the commands (see [Commands](COMMANDS.md) for detailed usage)

## Available Commands Overview

Once the application is running, you can use these command types:

### 1. Check Availability

```
Availability(hotelId, date, roomType)
Availability(hotelId, startDate-endDate, roomType)
```

### 2. Search for Availability

```
Search(hotelId, daysAhead, roomType)
```

### 3. Exit

Enter a blank line (press Enter without typing) to exit the application.

## Data Files

The application reads data from two JSON files:

- `hotels.json`: Contains hotel information, room types, and inventory
- `bookings.json`: Contains existing reservations

## Documentation

- [Detailed Command Documentation](COMMANDS.md) - Complete information about all commands, examples, and error handling