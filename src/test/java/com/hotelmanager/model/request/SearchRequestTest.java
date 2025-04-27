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

class SearchRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Should validate valid search request")
    void validate_WithValidRequest_ShouldHaveNoViolations() {
        // Given
        SearchRequest request = new SearchRequest("H1", 5, "SGL");

        // When
        Set<ConstraintViolation<SearchRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should fail validation when hotel ID is blank")
    void validate_WithBlankHotelId_ShouldHaveViolation() {
        // Given
        SearchRequest request = new SearchRequest("", 5, "SGL");

        // When
        Set<ConstraintViolation<SearchRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Hotel ID is required");
    }

    @Test
    @DisplayName("Should fail validation when days ahead is zero")
    void validate_WithZeroDaysAhead_ShouldHaveViolation() {
        // Given
        SearchRequest request = new SearchRequest("H1", 0, "SGL");

        // When
        Set<ConstraintViolation<SearchRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Days ahead must be positive");
    }

    @Test
    @DisplayName("Should fail validation when days ahead is negative")
    void validate_WithNegativeDaysAhead_ShouldHaveViolation() {
        // Given
        SearchRequest request = new SearchRequest("H1", -1, "SGL");

        // When
        Set<ConstraintViolation<SearchRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Days ahead must be positive");
    }

    @Test
    @DisplayName("Should fail validation when room type is blank")
    void validate_WithBlankRoomType_ShouldHaveViolation() {
        // Given
        SearchRequest request = new SearchRequest("H1", 5, "");

        // When
        Set<ConstraintViolation<SearchRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Room type is required");
    }

    @Test
    @DisplayName("Should fail validation with multiple violations")
    void validate_WithMultipleInvalidFields_ShouldHaveMultipleViolations() {
        // Given
        SearchRequest request = new SearchRequest("", 0, "");

        // When
        Set<ConstraintViolation<SearchRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(3);
    }
}