package com.hotelmanager.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelmanager.exception.DataLoadException;
import com.hotelmanager.model.Booking;
import com.hotelmanager.model.Hotel;
import com.hotelmanager.model.Room;
import com.hotelmanager.model.RoomType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HotelDataServiceTest {

    @TempDir
    Path tempDir;
    @Mock
    private ObjectMapper objectMapper;
    @InjectMocks
    private HotelDataService hotelDataService;
    private Path hotelsFilePath;
    private Path bookingsFilePath;
    private Hotel testHotel;
    private Booking testBooking;

    @BeforeEach
    void setUp() throws IOException {
        // Create temporary files
        hotelsFilePath = Files.createFile(tempDir.resolve("test_hotels.json"));
        bookingsFilePath = Files.createFile(tempDir.resolve("test_bookings.json"));

        // Create test data
        testHotel = createTestHotel();
        testBooking = createTestBooking();
    }

    private Hotel createTestHotel() {
        Hotel hotel = new Hotel();
        hotel.setId("H1");
        hotel.setName("Test Hotel");

        RoomType singleRoom = new RoomType("SGL", "Single Room",
                List.of("WiFi", "TV"), List.of("Non-smoking"));

        hotel.setRoomTypes(List.of(singleRoom));
        hotel.setRooms(List.of(
                new Room("SGL", "101"),
                new Room("SGL", "102")
        ));

        return hotel;
    }

    private Booking createTestBooking() {
        return new Booking("H1", "SGL", "Standard",
                LocalDate.of(2024, 9, 1),
                LocalDate.of(2024, 9, 3));
    }

    @Test
    @DisplayName("Should successfully load data from valid files")
    void loadFromFiles_WithValidFiles_ShouldLoadSuccessfully() throws IOException {
        // Given
        Hotel[] hotels = {testHotel};
        Booking[] bookings = {testBooking};

        when(objectMapper.readValue(any(File.class), eq(Hotel[].class))).thenReturn(hotels);
        when(objectMapper.readValue(any(File.class), eq(Booking[].class))).thenReturn(bookings);

        // When
        hotelDataService.loadFromFiles(hotelsFilePath.toString(), bookingsFilePath.toString());

        // Then
        verify(objectMapper).readValue(any(File.class), eq(Hotel[].class));
        verify(objectMapper).readValue(any(File.class), eq(Booking[].class));

        Optional<Hotel> loadedHotel = hotelDataService.findHotelById("H1");
        assertThat(loadedHotel).isPresent();
        assertThat(loadedHotel.get().getId()).isEqualTo("H1");
    }

    @Test
    @DisplayName("Should throw exception when hotels file doesn't exist")
    void loadFromFiles_WithNonExistentHotelsFile_ShouldThrowException() {
        // Given
        String nonExistentFile = "non_existent_hotels.json";

        // When/Then
        assertThatThrownBy(() -> hotelDataService.loadFromFiles(nonExistentFile, bookingsFilePath.toString()))
                .isInstanceOf(DataLoadException.class)
                .hasMessageContaining("Hotels file not found or not readable");
    }

    @Test
    @DisplayName("Should throw exception when bookings file doesn't exist")
    void loadFromFiles_WithNonExistentBookingsFile_ShouldThrowException() {
        // Given
        String nonExistentFile = "non_existent_bookings.json";

        // When/Then
        assertThatThrownBy(() -> hotelDataService.loadFromFiles(hotelsFilePath.toString(), nonExistentFile))
                .isInstanceOf(DataLoadException.class)
                .hasMessageContaining("Bookings file not found or not readable");
    }

    @Test
    @DisplayName("Should throw exception when JSON parsing fails")
    void loadFromFiles_WhenJsonParsingFails_ShouldThrowException() throws IOException {
        // Given
        when(objectMapper.readValue(any(File.class), eq(Hotel[].class)))
                .thenThrow(new IOException("JSON parsing error"));

        // When/Then
        assertThatThrownBy(() -> hotelDataService.loadFromFiles(hotelsFilePath.toString(), bookingsFilePath.toString()))
                .isInstanceOf(DataLoadException.class)
                .hasMessageContaining("Failed to load data");
    }

    @Test
    @DisplayName("Should find hotel by ID when hotel exists")
    void findHotelById_WhenHotelExists_ShouldReturnHotel() throws IOException {
        // Given
        Hotel[] hotels = {testHotel};
        Booking[] bookings = {};

        when(objectMapper.readValue(any(File.class), eq(Hotel[].class))).thenReturn(hotels);
        when(objectMapper.readValue(any(File.class), eq(Booking[].class))).thenReturn(bookings);

        hotelDataService.loadFromFiles(hotelsFilePath.toString(), bookingsFilePath.toString());

        // When
        Optional<Hotel> result = hotelDataService.findHotelById("H1");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo("H1");
        assertThat(result.get().getName()).isEqualTo("Test Hotel");
    }

    @Test
    @DisplayName("Should return empty Optional when hotel doesn't exist")
    void findHotelById_WhenHotelDoesNotExist_ShouldReturnEmpty() throws IOException {
        // Given
        Hotel[] hotels = {testHotel};
        Booking[] bookings = {};

        when(objectMapper.readValue(any(File.class), eq(Hotel[].class))).thenReturn(hotels);
        when(objectMapper.readValue(any(File.class), eq(Booking[].class))).thenReturn(bookings);

        hotelDataService.loadFromFiles(hotelsFilePath.toString(), bookingsFilePath.toString());

        // When
        Optional<Hotel> result = hotelDataService.findHotelById("H99");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should throw exception when finding hotel before initialization")
    void findHotelById_BeforeInitialization_ShouldThrowException() {
        // When/Then
        assertThatThrownBy(() -> hotelDataService.findHotelById("H1"))
                .isInstanceOf(DataLoadException.class)
                .hasMessage("Hotel data not initialized");
    }

    @Test
    @DisplayName("Should find bookings for specific date")
    void findBookingsForDate_WithMatchingBookings_ShouldReturnBookings() throws IOException {
        // Given
        Hotel[] hotels = {testHotel};
        Booking[] bookings = {testBooking};

        when(objectMapper.readValue(any(File.class), eq(Hotel[].class))).thenReturn(hotels);
        when(objectMapper.readValue(any(File.class), eq(Booking[].class))).thenReturn(bookings);

        hotelDataService.loadFromFiles(hotelsFilePath.toString(), bookingsFilePath.toString());

        // When
        List<Booking> result = hotelDataService.findBookingsForDate("H1", "SGL", LocalDate.of(2024, 9, 2));

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).hotelId()).isEqualTo("H1");
        assertThat(result.get(0).roomType()).isEqualTo("SGL");
    }

    @Test
    @DisplayName("Should return empty list when no bookings match")
    void findBookingsForDate_WithNoMatchingBookings_ShouldReturnEmptyList() throws IOException {
        // Given
        Hotel[] hotels = {testHotel};
        Booking[] bookings = {testBooking};

        when(objectMapper.readValue(any(File.class), eq(Hotel[].class))).thenReturn(hotels);
        when(objectMapper.readValue(any(File.class), eq(Booking[].class))).thenReturn(bookings);

        hotelDataService.loadFromFiles(hotelsFilePath.toString(), bookingsFilePath.toString());

        // When
        List<Booking> result = hotelDataService.findBookingsForDate("H1", "SGL", LocalDate.of(2024, 9, 10));

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should throw exception when finding bookings before initialization")
    void findBookingsForDate_BeforeInitialization_ShouldThrowException() {
        // When/Then
        assertThatThrownBy(() ->
                hotelDataService.findBookingsForDate("H1", "SGL", LocalDate.now()))
                .isInstanceOf(DataLoadException.class)
                .hasMessage("Hotel data not initialized");
    }

    @Test
    @DisplayName("Should correctly filter bookings by date range")
    void findBookingsForDate_WithMultipleBookings_ShouldFilterCorrectly() throws IOException {
        // Given
        Hotel[] hotels = {testHotel};
        Booking booking1 = new Booking("H1", "SGL", "Standard",
                LocalDate.of(2024, 9, 1), LocalDate.of(2024, 9, 3));
        Booking booking2 = new Booking("H1", "SGL", "Standard",
                LocalDate.of(2024, 9, 5), LocalDate.of(2024, 9, 7));
        Booking booking3 = new Booking("H1", "DBL", "Standard",
                LocalDate.of(2024, 9, 1), LocalDate.of(2024, 9, 3));
        Booking[] bookings = {booking1, booking2, booking3};

        when(objectMapper.readValue(any(File.class), eq(Hotel[].class))).thenReturn(hotels);
        when(objectMapper.readValue(any(File.class), eq(Booking[].class))).thenReturn(bookings);

        hotelDataService.loadFromFiles(hotelsFilePath.toString(), bookingsFilePath.toString());

        // When
        List<Booking> result = hotelDataService.findBookingsForDate("H1", "SGL", LocalDate.of(2024, 9, 2));

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).arrival()).isEqualTo(LocalDate.of(2024, 9, 1));
        assertThat(result.get(0).departure()).isEqualTo(LocalDate.of(2024, 9, 3));
    }

    @Test
    @DisplayName("Should handle empty data files")
    void loadFromFiles_WithEmptyData_ShouldHandleGracefully() throws IOException {
        // Given
        Hotel[] hotels = {};
        Booking[] bookings = {};

        when(objectMapper.readValue(any(File.class), eq(Hotel[].class))).thenReturn(hotels);
        when(objectMapper.readValue(any(File.class), eq(Booking[].class))).thenReturn(bookings);

        // When
        hotelDataService.loadFromFiles(hotelsFilePath.toString(), bookingsFilePath.toString());

        // Then
        assertThat(hotelDataService.findHotelById("H1")).isEmpty();
        assertThat(hotelDataService.findBookingsForDate("H1", "SGL", LocalDate.now())).isEmpty();
    }
}