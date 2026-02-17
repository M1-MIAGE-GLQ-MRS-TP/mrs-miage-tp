package fr.amu.univ.miage.m1.glq;

import fr.amu.univ.miage.m1.glq.util.DateUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class DateUtilsTest {

    @Nested
    @DisplayName("Date Formatting Rules")
    class DateFormatting {
        @Test
        void should_format_date_as_dd_mm_yyyy() {
            Calendar cal = Calendar.getInstance();
            cal.set(2023, Calendar.JANUARY, 1);
            Date date = cal.getTime();
            
            String result = DateUtils.formatDate(date);
            
            assertThat(result).isEqualTo("01/01/2023");
        }

        @Test
        void should_return_empty_string_when_formatting_null_date() {
            assertThat(DateUtils.formatDate(null)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Date Time Formatting Rules")
    class DateTimeFormatting {
        @Test
        void should_format_date_time_as_dd_mm_yyyy_hh_mm() {
            Calendar cal = Calendar.getInstance();
            cal.set(2023, Calendar.JANUARY, 1, 14, 30);
            Date date = cal.getTime();
            
            String result = DateUtils.formatDateTime(date);
            
            assertThat(result).isEqualTo("01/01/2023 14:30");
        }

        @Test
        void should_return_empty_string_when_formatting_null_date_time() {
            assertThat(DateUtils.formatDateTime(null)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Date Parsing Rules")
    class DateParsing {
        @Test
        void should_parse_valid_dd_mm_yyyy_date_string() {
            String dateStr = "01/01/2023";
            Date date = DateUtils.parseDate(dateStr);
            
            assertThat(date).isNotNull();
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            assertThat(cal.get(Calendar.YEAR)).isEqualTo(2023);
            assertThat(cal.get(Calendar.MONTH)).isEqualTo(Calendar.JANUARY);
            assertThat(cal.get(Calendar.DAY_OF_MONTH)).isEqualTo(1);
        }

        @Test
        void should_return_null_when_parsing_null_string() {
            assertThat(DateUtils.parseDate(null)).isNull();
        }

        @Test
        void should_return_null_when_parsing_empty_string() {
            assertThat(DateUtils.parseDate("")).isNull();
        }

        @Test
        void should_return_null_when_parsing_invalid_date_format() {
            assertThat(DateUtils.parseDate("invalid")).isNull();
        }
    }

    @Nested
    @DisplayName("Days Between Calculation")
    class DaysBetweenCalculation {
        @Test
        void should_calculate_number_of_days_between_two_dates() {
            Calendar cal1 = Calendar.getInstance();
            cal1.set(2023, Calendar.JANUARY, 1);
            Date start = cal1.getTime();

            Calendar cal2 = Calendar.getInstance();
            cal2.set(2023, Calendar.JANUARY, 5);
            Date end = cal2.getTime();

            assertThat(DateUtils.daysBetween(start, end)).isEqualTo(4);
        }

        @Test
        void should_return_zero_when_start_date_is_null() {
            assertThat(DateUtils.daysBetween(null, new Date())).isZero();
        }

        @Test
        void should_return_zero_when_end_date_is_null() {
            assertThat(DateUtils.daysBetween(new Date(), null)).isZero();
        }
    }

    @Nested
    @DisplayName("Date Addition Operations")
    class DateAddition {
        @Test
        void should_add_specified_number_of_days_to_date() {
            Calendar cal = Calendar.getInstance();
            cal.set(2023, Calendar.JANUARY, 1);
            Date date = cal.getTime();

            Date newDate = DateUtils.addDays(date, 5);

            Calendar expectedCal = Calendar.getInstance();
            expectedCal.set(2023, Calendar.JANUARY, 6);
            
            Calendar resCal = Calendar.getInstance();
            resCal.setTime(newDate);
            
            assertThat(resCal.get(Calendar.DAY_OF_YEAR)).isEqualTo(expectedCal.get(Calendar.DAY_OF_YEAR));
            assertThat(resCal.get(Calendar.YEAR)).isEqualTo(expectedCal.get(Calendar.YEAR));
        }

        @Test
        void should_return_null_when_adding_days_to_null_date() {
            assertThat(DateUtils.addDays(null, 5)).isNull();
        }
    }

    @Nested
    @DisplayName("Temporal Verification (Past/Future)")
    class TemporalVerification {
        @Test
        void should_return_true_if_date_is_in_the_past() {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, -1);
            Date pastDate = cal.getTime();

            assertThat(DateUtils.isPast(pastDate)).isTrue();
        }

        @Test
        void should_return_false_if_date_is_in_the_future() {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, 1);
            Date futureDate = cal.getTime();

            assertThat(DateUtils.isPast(futureDate)).isFalse();
        }

        @Test
        void should_return_false_checking_past_for_null_date() {
            assertThat(DateUtils.isPast(null)).isFalse();
        }

        @Test
        void should_return_true_if_date_matches_today() {
            assertThat(DateUtils.isToday(new Date())).isTrue();
        }

        @Test
        void should_return_false_if_date_does_not_match_today() {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, -5);
            Date notToday = cal.getTime();

            assertThat(DateUtils.isToday(notToday)).isFalse();
        }

        @Test
        void should_return_false_checking_today_for_null_date() {
            assertThat(DateUtils.isToday(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("Date Range Verification")
    class DateRangeVerification {
        @Test
        void should_return_true_when_date_is_within_specified_days_range() {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, 2);
            Date futureDate = cal.getTime();

            assertThat(DateUtils.isWithinDays(futureDate, 5)).isTrue();
        }

        @Test
        void should_return_false_when_date_is_outside_specified_days_range() {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, 6);
            Date futureDate = cal.getTime();

            assertThat(DateUtils.isWithinDays(futureDate, 5)).isFalse();
        }
        
        @Test
        void should_return_false_when_checking_range_for_past_date() {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, -1);
            Date pastDate = cal.getTime();

             assertThat(DateUtils.isWithinDays(pastDate, 5)).isFalse();
        }

        @Test
        void should_return_false_checking_range_for_null_date() {
            assertThat(DateUtils.isWithinDays(null, 5)).isFalse();
        }
    }

    @Nested
    @DisplayName("Day Boundaries Calculation")
    class DayBoundaries {
        @Test
        void should_return_beginning_of_day_00_00_00() {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 14);
            cal.set(Calendar.MINUTE, 30);
            cal.set(Calendar.SECOND, 15);
            cal.set(Calendar.MILLISECOND, 500);
            Date date = cal.getTime();

            Date start = DateUtils.startOfDay(date);
            Calendar startCal = Calendar.getInstance();
            startCal.setTime(start);

            assertThat(startCal.get(Calendar.HOUR_OF_DAY)).isZero();
            assertThat(startCal.get(Calendar.MINUTE)).isZero();
            assertThat(startCal.get(Calendar.SECOND)).isZero();
            assertThat(startCal.get(Calendar.MILLISECOND)).isZero();
        }

        @Test
        void should_return_null_when_calculating_start_of_day_for_null() {
            assertThat(DateUtils.startOfDay(null)).isNull();
        }

        @Test
        void should_return_end_of_day_23_59_59() {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 10);
            Date date = cal.getTime();

            Date end = DateUtils.endOfDay(date);
            Calendar endCal = Calendar.getInstance();
            endCal.setTime(end);

            assertThat(endCal.get(Calendar.HOUR_OF_DAY)).isEqualTo(23);
            assertThat(endCal.get(Calendar.MINUTE)).isEqualTo(59);
            assertThat(endCal.get(Calendar.SECOND)).isEqualTo(59);
            assertThat(endCal.get(Calendar.MILLISECOND)).isEqualTo(999);
        }

        @Test
        void should_return_null_when_calculating_end_of_day_for_null() {
            assertThat(DateUtils.endOfDay(null)).isNull();
        }
    }

    @Test
    @DisplayName("Private Constructor")
    void should_not_be_instantiable() throws Exception {
        java.lang.reflect.Constructor<DateUtils> constructor = DateUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        DateUtils instance = constructor.newInstance();
        assertThat(instance).isNotNull();
    }

    @Test
    @DisplayName("isToday Year Mismatch")
    void should_return_false_if_date_is_today_but_different_year() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -1);
        Date sameDayLastYear = cal.getTime();
        assertThat(DateUtils.isToday(sameDayLastYear)).isFalse();
    }
}
