package com.hotelmanager.service.validation;

import com.hotelmanager.exception.InvalidCommandException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestValidationServiceTest {

    @Mock
    private Validator validator;

    @InjectMocks
    private RequestValidationService requestValidationService;

    @Test
    @DisplayName("Should validate request successfully when no violations")
    void validate_WithNoViolations_ShouldNotThrowException() {
        // Given
        Object request = new Object();
        when(validator.validate(request)).thenReturn(Collections.emptySet());

        // When/Then
        assertThatNoException().isThrownBy(() -> requestValidationService.validate(request));
    }

    @Test
    @DisplayName("Should throw exception with all violation messages")
    void validate_WithViolations_ShouldThrowExceptionWithAllMessages() {
        // Given
        Object request = new Object();
        ConstraintViolation<Object> violation1 = mockConstraintViolation("Error 1");
        ConstraintViolation<Object> violation2 = mockConstraintViolation("Error 2");
        Set<ConstraintViolation<Object>> violations = new HashSet<>();
        violations.add(violation1);
        violations.add(violation2);

        when(validator.validate(request)).thenReturn(violations);

        // When/Then
        assertThatThrownBy(() -> requestValidationService.validate(request))
                .isInstanceOf(InvalidCommandException.class)
                .hasMessageContaining("Error 1")
                .hasMessageContaining("Error 2");
    }

    @SuppressWarnings("unchecked")
    private <T> ConstraintViolation<T> mockConstraintViolation(String message) {
        ConstraintViolation<T> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn(message);
        return violation;
    }
}