package com.hotelmanager.model.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AvailabilityRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Should validate valid availability request with single date")
    void validate_WithValidSingleDate_ShouldHaveNoViolations() {
        // Given
        AvailabilityRequest request = new AvailabilityRequest("H1", "20240901", "SGL");

        // When
        Set<ConstraintViolation<AvailabilityRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should validate valid availability request with date range")
    void validate_WithValidDateRange_ShouldHaveNoViolations() {
        // Given
        AvailabilityRequest request = new AvailabilityRequest("H1", "20240901-20240903", "SGL");

        // When
        Set<ConstraintViolation<AvailabilityRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should fail validation when hotel ID is blank")
    void validate_WithBlankHotelId_ShouldHaveViolation() {
        // Given
        AvailabilityRequest request = new AvailabilityRequest("", "20240901", "SGL");

        // When
        Set<ConstraintViolation<AvailabilityRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Hotel ID is required");
    }

    @Test
    @DisplayName("Should fail validation when date range is blank")
    void validate_WithBlankDateRange_ShouldHaveViolation() {
        // Given
        AvailabilityRequest request = new AvailabilityRequest("H1", "", "SGL");

        // When
        Set<ConstraintViolation<AvailabilityRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(2); // Both @NotBlank and @Pattern are triggered
        assertThat(violations.stream()
                .map(ConstraintViolation::getMessage)
                .toList())
                .contains("Date range is required",
                        "Date range must be in format YYYYMMDD or YYYYMMDD-YYYYMMDD");
    }

    @Test
    @DisplayName("Should fail validation when date range has invalid format")
    void validate_WithInvalidDateRangeFormat_ShouldHaveViolation() {
        // Given
        AvailabilityRequest request = new AvailabilityRequest("H1", "invalid-date", "SGL");

        // When
        Set<ConstraintViolation<AvailabilityRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Date range must be in format YYYYMMDD or YYYYMMDD-YYYYMMDD");
    }

    @Test
    @DisplayName("Should fail validation when date range has partial date")
    void validate_WithPartialDate_ShouldHaveViolation() {
        // Given
        AvailabilityRequest request = new AvailabilityRequest("H1", "2024", "SGL");

        // When
        Set<ConstraintViolation<AvailabilityRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Date range must be in format YYYYMMDD or YYYYMMDD-YYYYMMDD");
    }

    @Test
    @DisplayName("Should fail validation when room type is blank")
    void validate_WithBlankRoomType_ShouldHaveViolation() {
        // Given
        AvailabilityRequest request = new AvailabilityRequest("H1", "20240901", "");

        // When
        Set<ConstraintViolation<AvailabilityRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Room type is required");
    }

    @Test
    @DisplayName("Should fail validation with multiple violations")
    void validate_WithMultipleInvalidFields_ShouldHaveMultipleViolations() {
        // Given
        AvailabilityRequest request = new AvailabilityRequest("", "", "");

        // When
        Set<ConstraintViolation<AvailabilityRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(4); // 1 for hotelId, 2 for dateRange (@NotBlank + @Pattern), 1 for roomType
    }
}