package com.hotelmanager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class HotelBookingSystemApplicationTests {

    // Mock CommandLineRunner to prevent the main application from executing during tests,
    // which would fail due to missing command line arguments (--hotels, --bookings)
    @MockitoBean
    private CommandLineRunner commandLineRunner;

    @Test
    void contextLoads() {
    }

}
