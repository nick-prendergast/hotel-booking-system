package com.hotelmanager;

import com.hotelmanager.exception.BookingSystemException;
import com.hotelmanager.service.ConsoleOutputService;
import com.hotelmanager.service.HotelBookingService;
import com.hotelmanager.service.HotelDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class HotelBookingApplication implements CommandLineRunner {

	private final HotelDataService hotelDataService;
	private final HotelBookingService hotelBookingService;
	private final ConsoleOutputService consoleOutputService;

	public HotelBookingApplication(HotelDataService hotelDataService,
								   HotelBookingService hotelBookingService,
								   ConsoleOutputService consoleOutputService) {
		this.hotelDataService = hotelDataService;
		this.hotelBookingService = hotelBookingService;
		this.consoleOutputService = consoleOutputService;
	}

	public static void main(String[] args) {
		SpringApplication.run(HotelBookingApplication.class, args);
	}

	@Override
	public void run(String... args) {
		if (!executeApplication(args)) {
			System.exit(1);
		}
	}

	boolean executeApplication(String... args) {
		if (args.length < 4) {
			consoleOutputService.displayError("Usage: myapp --hotels <hotels-file> --bookings <bookings-file>");
			return false;
		}

		String hotelsFile = null;
		String bookingsFile = null;

		for (int i = 0; i < args.length - 1; i++) {
			if ("--hotels".equals(args[i])) {
				hotelsFile = args[i + 1];
			} else if ("--bookings".equals(args[i])) {
				bookingsFile = args[i + 1];
			}
		}

		if (hotelsFile == null || bookingsFile == null) {
			consoleOutputService.displayError("Usage: myapp --hotels <hotels-file> --bookings <bookings-file>");
			return false;
		}

		try {
			log.info("Loading data files - hotels: {}, bookings: {}", hotelsFile, bookingsFile);
			hotelDataService.loadData(hotelsFile, bookingsFile);
			hotelBookingService.startCommandLoop();
			return true;
		} catch (BookingSystemException e) {
			log.error("Application error", e);
			consoleOutputService.displayError(e.getMessage());
			return false;
		}
	}
}