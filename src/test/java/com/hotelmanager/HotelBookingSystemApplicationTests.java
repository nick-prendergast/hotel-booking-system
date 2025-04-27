package com.hotelmanager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class HotelBookingSystemApplicationTests {

    @MockitoBean
    private CommandLineRunner commandLineRunner;

    @Test
    void contextLoads() {
    }

}
