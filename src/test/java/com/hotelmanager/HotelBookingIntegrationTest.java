package com.hotelmanager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hotelmanager.model.*;
import com.hotelmanager.service.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HotelBookingIntegrationTest {

    private HotelBookingApplication application;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;
    private InputStream originalIn;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());

        HotelDataService hotelDataService = new HotelDataService(objectMapper);
        AvailabilityCalculator availabilityCalculator = new AvailabilityCalculator(hotelDataService);
        ValidationService validationService = new ValidationService(hotelDataService);
        AvailabilityService availabilityService = new AvailabilityService(validationService, availabilityCalculator);
        CommandProcessor commandProcessor = new CommandProcessor(availabilityService);
        ConsoleOutputService consoleOutputService = new ConsoleOutputService();
        HotelBookingService hotelBookingService = new HotelBookingService(commandProcessor, consoleOutputService);

        application = new HotelBookingApplication(hotelDataService, hotelBookingService, consoleOutputService);

        originalOut = System.out;
        outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        // Capture System.in
        originalIn = System.in;
    }

    @AfterEach
    void tearDown() {
        // Restore original streams
        System.setOut(originalOut);
        System.setIn(originalIn);
    }

    @Test
    void testCommandLineApplication(@TempDir Path tempDir) throws Exception {
        // Create test data objects
        Hotel hotel = createTestHotel();
        List<Booking> bookings = createTestBookings();

        // Write objects to files using ObjectMapper
        Path hotelsFile = tempDir.resolve("hotels.json");
        Path bookingsFile = tempDir.resolve("bookings.json");

        objectMapper.writeValue(hotelsFile.toFile(), List.of(hotel));
        objectMapper.writeValue(bookingsFile.toFile(), bookings);

        // Set up input commands
        String commands = """
            Availability(H1, 20240901, SGL)
            Availability(H1, 20240901-20240903, DBL)

            """;
        System.setIn(new ByteArrayInputStream(commands.getBytes()));

        // Execute application
        String[] args = {"--hotels", hotelsFile.toString(), "--bookings", bookingsFile.toString()};
        boolean success = application.executeApplication(args);

        // Verify output
        assertThat(success).isTrue();
        String output = outputStream.toString();
        assertThat(output).contains("Hotel: H1, Room Type: SGL, Date Range: 20240901, Available Rooms: 2");
        assertThat(output).contains("Hotel: H1, Room Type: DBL, Date Range: 20240901-20240903, Available Rooms: 1");
    }

    private Hotel createTestHotel() {
        Hotel hotel = new Hotel();
        hotel.setId("H1");
        hotel.setName("Hotel California");

        // Create room types
        RoomType singleRoom = new RoomType(
                "SGL",
                "Single Room",
                List.of("WiFi", "TV"),
                List.of("Non-smoking")
        );

        RoomType doubleRoom = new RoomType(
                "DBL",
                "Double Room",
                List.of("WiFi", "TV", "Minibar"),
                List.of("Non-smoking", "Sea View")
        );

        hotel.setRoomTypes(Arrays.asList(singleRoom, doubleRoom));

        // Create rooms
        List<Room> rooms = Arrays.asList(
                new Room("SGL", "101"),
                new Room("SGL", "102"),
                new Room("DBL", "201"),
                new Room("DBL", "202")
        );

        hotel.setRooms(rooms);
        return hotel;
    }

    private List<Booking> createTestBookings() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        Booking booking1 = new Booking(
                "H1",
                "DBL",
                "Prepaid",
                LocalDate.parse("20240901", formatter),
                LocalDate.parse("20240903", formatter)
        );

        Booking booking2 = new Booking(
                "H1",
                "SGL",
                "Standard",
                LocalDate.parse("20240902", formatter),
                LocalDate.parse("20240905", formatter)
        );

        return Arrays.asList(booking1, booking2);
    }
}