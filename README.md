
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

3. Start using the commands (see Usage section)

## Usage

### Running the Application

```bash
docker-compose run --rm app --hotels hotels.json --bookings bookings.json
```

### Available Commands

Once the application is running, you can use these commands:

#### 1. Check Availability

Check availability for a specific room type on a date or date range:

```
Availability(hotelId, date, roomType)
Availability(hotelId, startDate-endDate, roomType)
```

Examples:
```
Availability(H1, 20240901, SGL)
Availability(H1, 20240901-20240903, DBL)
```

Output: Number of available rooms (negative values indicate overbooking)

#### 2. Search for Availability

Search for availability over a specified number of days:

```
Search(hotelId, daysAhead, roomType)
```

Example:
```
Search(H1, 365, SGL)
```

Output: Comma-separated list of date ranges with availability:
```
(20241101-20241103, 2), (20241203-20241210, 1)
```

#### 3. Exit

Enter a blank line (press Enter without typing) to exit the application.


