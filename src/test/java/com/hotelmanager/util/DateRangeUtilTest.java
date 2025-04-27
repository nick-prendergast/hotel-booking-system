package com.hotelmanager.util;

import com.hotelmanager.model.DailyAvailability;
import com.hotelmanager.model.DateRangeAvailability;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DateRangeUtilTest {

    @Test
    @DisplayName("Should return empty list when input is null")
    void consolidateDateRanges_WithNullInput_ShouldReturnEmptyList() {
        // When
        List<DateRangeAvailability> result = DateRangeUtil.consolidateDateRanges(null);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return empty list when input is empty")
    void consolidateDateRanges_WithEmptyInput_ShouldReturnEmptyList() {
        // When
        List<DateRangeAvailability> result = DateRangeUtil.consolidateDateRanges(new ArrayList<>());

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should consolidate consecutive dates with same availability into single range")
    void consolidateDateRanges_WithConsecutiveDatesAndSameAvailability_ShouldReturnSingleRange() {
        // Given
        List<DailyAvailability> dailyAvailabilities = Arrays.asList(
                new DailyAvailability(LocalDate.of(2024, 9, 1), 2),
                new DailyAvailability(LocalDate.of(2024, 9, 2), 2),
                new DailyAvailability(LocalDate.of(2024, 9, 3), 2)
        );

        // When
        List<DateRangeAvailability> result = DateRangeUtil.consolidateDateRanges(dailyAvailabilities);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).startDate()).isEqualTo(LocalDate.of(2024, 9, 1));
        assertThat(result.get(0).endDate()).isEqualTo(LocalDate.of(2024, 9, 3));
        assertThat(result.get(0).availability()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should create separate ranges for different availability values")
    void consolidateDateRanges_WithDifferentAvailability_ShouldCreateSeparateRanges() {
        // Given
        List<DailyAvailability> dailyAvailabilities = Arrays.asList(
                new DailyAvailability(LocalDate.of(2024, 9, 1), 2),
                new DailyAvailability(LocalDate.of(2024, 9, 2), 2),
                new DailyAvailability(LocalDate.of(2024, 9, 3), 1),
                new DailyAvailability(LocalDate.of(2024, 9, 4), 1)
        );

        // When
        List<DateRangeAvailability> result = DateRangeUtil.consolidateDateRanges(dailyAvailabilities);

        // Then
        assertThat(result).hasSize(2);

        assertThat(result.get(0).startDate()).isEqualTo(LocalDate.of(2024, 9, 1));
        assertThat(result.get(0).endDate()).isEqualTo(LocalDate.of(2024, 9, 2));
        assertThat(result.get(0).availability()).isEqualTo(2);

        assertThat(result.get(1).startDate()).isEqualTo(LocalDate.of(2024, 9, 3));
        assertThat(result.get(1).endDate()).isEqualTo(LocalDate.of(2024, 9, 4));
        assertThat(result.get(1).availability()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should create separate ranges for non-consecutive dates")
    void consolidateDateRanges_WithNonConsecutiveDates_ShouldCreateSeparateRanges() {
        // Given
        List<DailyAvailability> dailyAvailabilities = Arrays.asList(
                new DailyAvailability(LocalDate.of(2024, 9, 1), 2),
                new DailyAvailability(LocalDate.of(2024, 9, 2), 2),
                new DailyAvailability(LocalDate.of(2024, 9, 5), 2),
                new DailyAvailability(LocalDate.of(2024, 9, 6), 2)
        );

        // When
        List<DateRangeAvailability> result = DateRangeUtil.consolidateDateRanges(dailyAvailabilities);

        // Then
        assertThat(result).hasSize(2);

        assertThat(result.get(0).startDate()).isEqualTo(LocalDate.of(2024, 9, 1));
        assertThat(result.get(0).endDate()).isEqualTo(LocalDate.of(2024, 9, 2));
        assertThat(result.get(0).availability()).isEqualTo(2);

        assertThat(result.get(1).startDate()).isEqualTo(LocalDate.of(2024, 9, 5));
        assertThat(result.get(1).endDate()).isEqualTo(LocalDate.of(2024, 9, 6));
        assertThat(result.get(1).availability()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should handle single day input")
    void consolidateDateRanges_WithSingleDay_ShouldReturnSingleRange() {
        // Given
        List<DailyAvailability> dailyAvailabilities = Collections.singletonList(
                new DailyAvailability(LocalDate.of(2024, 9, 1), 3)
        );

        // When
        List<DateRangeAvailability> result = DateRangeUtil.consolidateDateRanges(dailyAvailabilities);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).startDate()).isEqualTo(LocalDate.of(2024, 9, 1));
        assertThat(result.get(0).endDate()).isEqualTo(LocalDate.of(2024, 9, 1));
        assertThat(result.get(0).availability()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should handle alternating availability values")
    void consolidateDateRanges_WithAlternatingAvailability_ShouldCreateMultipleRanges() {
        // Given
        List<DailyAvailability> dailyAvailabilities = Arrays.asList(
                new DailyAvailability(LocalDate.of(2024, 9, 1), 2),
                new DailyAvailability(LocalDate.of(2024, 9, 2), 1),
                new DailyAvailability(LocalDate.of(2024, 9, 3), 2),
                new DailyAvailability(LocalDate.of(2024, 9, 4), 1)
        );

        // When
        List<DateRangeAvailability> result = DateRangeUtil.consolidateDateRanges(dailyAvailabilities);

        // Then
        assertThat(result).hasSize(4);

        assertThat(result.get(0).startDate()).isEqualTo(LocalDate.of(2024, 9, 1));
        assertThat(result.get(0).endDate()).isEqualTo(LocalDate.of(2024, 9, 1));
        assertThat(result.get(0).availability()).isEqualTo(2);

        assertThat(result.get(1).startDate()).isEqualTo(LocalDate.of(2024, 9, 2));
        assertThat(result.get(1).endDate()).isEqualTo(LocalDate.of(2024, 9, 2));
        assertThat(result.get(1).availability()).isEqualTo(1);

        assertThat(result.get(2).startDate()).isEqualTo(LocalDate.of(2024, 9, 3));
        assertThat(result.get(2).endDate()).isEqualTo(LocalDate.of(2024, 9, 3));
        assertThat(result.get(2).availability()).isEqualTo(2);

        assertThat(result.get(3).startDate()).isEqualTo(LocalDate.of(2024, 9, 4));
        assertThat(result.get(3).endDate()).isEqualTo(LocalDate.of(2024, 9, 4));
        assertThat(result.get(3).availability()).isEqualTo(1);
    }
}