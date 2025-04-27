package com.hotelmanager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hotelmanager.model.Booking;
import com.hotelmanager.model.Hotel;
import com.hotelmanager.model.Room;
import com.hotelmanager.model.RoomType;
import com.hotelmanager.service.availability.AvailabilityCalculator;
import com.hotelmanager.service.availability.AvailabilityService;
import com.hotelmanager.service.data.HotelDataService;
import com.hotelmanager.service.CommandProcessor;
import com.hotelmanager.service.HotelBookingService;
import com.hotelmanager.service.presentation.ConsoleOutputService;
import com.hotelmanager.service.presentation.ResponseFormatter;
import com.hotelmanager.service.validation.RequestValidationService;
import com.hotelmanager.service.validation.ValidationService;
import jakarta.validation.Validator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HotelBookingIntegrationTest {

    private HotelBookingApplication application;
    private ObjectMapper objectMapper;
    private ByteArrayOutputStream outputStream;
    private ByteArrayOutputStream errorStream;

    private static CommandProcessor getCommandProcessor(Validator validator, AvailabilityService availabilityService) {
        var requestValidationService = new RequestValidationService(validator);

        var availabilityParser = new com.hotelmanager.parser.AvailabilityCommandParser(requestValidationService);
        var searchParser = new com.hotelmanager.parser.SearchCommandParser(requestValidationService);
        var responseFormatter = new ResponseFormatter();

        return new CommandProcessor(
                availabilityParser, searchParser, availabilityService, responseFormatter);
    }

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        application = createApplication();

        // Capture outputs
        outputStream = new ByteArrayOutputStream();
        errorStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
        System.setErr(new PrintStream(errorStream));
    }

    @AfterEach
    void tearDown() {
        System.setOut(System.out);
        System.setErr(System.err);
    }

    @Test
    void testAvailabilityCommands(@TempDir Path tempDir) throws Exception {
        // Setup data
        Hotel hotel = createTestHotel();
        List<Booking> bookings = createTestBookings();

        // Write to files
        Path hotelFile = tempDir.resolve("hotels.json");
        Path bookingFile = tempDir.resolve("bookings.json");
        objectMapper.writeValue(hotelFile.toFile(), List.of(hotel));
        objectMapper.writeValue(bookingFile.toFile(), bookings);

        // Execute
        String input = "Availability(H1, 20240901, SGL)\n\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        boolean success = application.executeApplication("--hotels", hotelFile.toString(),
                "--bookings", bookingFile.toString());

        // Verify
        assertThat(success).isTrue();
        assertThat(outputStream.toString()).contains("Available Rooms: 2");
    }

    @Test
    void testSearchCommand(@TempDir Path tempDir) throws Exception {
        // Setup data
        Hotel hotel = createTestHotel();
        LocalDate today = LocalDate.now();
        List<Booking> bookings = List.of(
                new Booking("H1", "SGL", "Standard",
                        today.plusDays(1),
                        today.plusDays(3))  // Blocks 1 room for days 1-2
        );

        // Write to files
        Path hotelFile = tempDir.resolve("hotels.json");
        Path bookingFile = tempDir.resolve("bookings.json");
        objectMapper.writeValue(hotelFile.toFile(), List.of(hotel));
        objectMapper.writeValue(bookingFile.toFile(), bookings);

        // Execute
        String input = "Search(H1, 5, SGL)\n\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        boolean success = application.executeApplication("--hotels", hotelFile.toString(),
                "--bookings", bookingFile.toString());

        // Verify
        assertThat(success).isTrue();
        String output = outputStream.toString();

        // Expected availability:
        // Today: 2 rooms available
        // Days 1-2: 1 room available (1 booked)
        // Days 3-4: 2 rooms available
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        String expectedRange1 = String.format("(%s-%s, 2)",
                today.format(formatter),
                today.format(formatter));

        String expectedRange2 = String.format("(%s-%s, 1)",
                today.plusDays(1).format(formatter),
                today.plusDays(2).format(formatter));

        String expectedRange3 = String.format("(%s-%s, 2)",
                today.plusDays(3).format(formatter),
                today.plusDays(4).format(formatter));

        assertThat(output).contains(expectedRange1);
        assertThat(output).contains(expectedRange2);
        assertThat(output).contains(expectedRange3);
    }

    @Test
    void testInvalidCommands(@TempDir Path tempDir) throws Exception {
        // Setup data
        Hotel hotel = createTestHotel();
        List<Booking> bookings = createTestBookings();

        // Write to files
        Path hotelFile = tempDir.resolve("hotels.json");
        Path bookingFile = tempDir.resolve("bookings.json");
        objectMapper.writeValue(hotelFile.toFile(), List.of(hotel));
        objectMapper.writeValue(bookingFile.toFile(), bookings);

        // Execute
        String input = "InvalidCommand\n\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        boolean success = application.executeApplication("--hotels", hotelFile.toString(),
                "--bookings", bookingFile.toString());

        // Verify
        assertThat(success).isTrue();
        assertThat(errorStream.toString()).contains("Error");
    }

    private Hotel createTestHotel() {
        Hotel hotel = new Hotel();
        hotel.setId("H1");
        hotel.setName("Hotel California");
        hotel.setRoomTypes(Arrays.asList(
                new RoomType("SGL", "Single Room", List.of("WiFi", "TV"), List.of("Non-smoking")),
                new RoomType("DBL", "Double Room", List.of("WiFi", "TV", "Minibar"), List.of("Non-smoking", "Sea View"))
        ));
        hotel.setRooms(Arrays.asList(
                new Room("SGL", "101"),
                new Room("SGL", "102"),
                new Room("DBL", "201"),
                new Room("DBL", "202")
        ));
        return hotel;
    }

    private List<Booking> createTestBookings() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return List.of(
                new Booking("H1", "DBL", "Prepaid",
                        LocalDate.parse("20240901", formatter),
                        LocalDate.parse("20240903", formatter))
        );
    }

    private HotelBookingApplication createApplication() {
        var dataService = new HotelDataService(objectMapper);
        var availabilityCalculator = new AvailabilityCalculator(dataService);
        var validationService = new ValidationService(dataService);
        var availabilityService = new AvailabilityService(validationService, availabilityCalculator);

        var validator = jakarta.validation.Validation.buildDefaultValidatorFactory().getValidator();
        var commandProcessor = getCommandProcessor(validator, availabilityService);

        var consoleOutputService = new ConsoleOutputService();
        var hotelBookingService = new HotelBookingService(commandProcessor, consoleOutputService);

        return new HotelBookingApplication(dataService, hotelBookingService, consoleOutputService);
    }
}