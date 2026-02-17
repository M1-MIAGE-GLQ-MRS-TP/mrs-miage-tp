package fr.amu.univ.miage.m1.glq;

import fr.amu.univ.miage.m1.glq.util.ValidationUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Year;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ValidationUtilsTest {

    @Nested
    @DisplayName("Email Format Validation")
    class EmailValidation {
        @ParameterizedTest
        @ValueSource(strings = {"test@example.com", "user.name+tag@domain.co.uk", "user@sub.domain.com"})
        void should_validate_successfully_valid_emails(String email) {
            assertThat(ValidationUtils.isValidEmail(email)).isTrue();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"plainaddress", "@missingusername.com", "username@.com.my", "username@domain", "username@domain."})
        void should_reject_invalid_email_formats(String email) {
            assertThat(ValidationUtils.isValidEmail(email)).isFalse();
        }
    }

    @Nested
    @DisplayName("ISBN Format Validation")
    class IsbnValidation {
        @ParameterizedTest
        @ValueSource(strings = {"0-12345678-9", "1234567890", "978-0-123456-47-2", "9781234567890"})
        void should_validate_successfully_valid_isbn_10_and_13(String isbn) {
            assertThat(ValidationUtils.isValidIsbn(isbn)).isTrue();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"123456789", "12345678901", "abcdefghij", "123-456"})
        void should_reject_invalid_isbn_formats(String isbn) {
            assertThat(ValidationUtils.isValidIsbn(isbn)).isFalse();
        }
    }

    @Nested
    @DisplayName("Phone Number Validation")
    class PhoneValidation {
        @ParameterizedTest
        @ValueSource(strings = {"0123456789", "06.12.34.56.78", "01 23 45 67 89", "09-87-65-43-21"})
        void should_validate_successfully_valid_phone_numbers(String phone) {
            assertThat(ValidationUtils.isValidPhone(phone)).isTrue();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"012345678", "01234567890", "abcdefghij"})
        void should_reject_invalid_phone_numbers(String phone) {
            assertThat(ValidationUtils.isValidPhone(phone)).isFalse();
        }
    }

    @Nested
    @DisplayName("Postal Code Validation")
    class ZipCodeValidation {
        @ParameterizedTest
        @ValueSource(strings = {"75001", "13000", "01000", "99999"})
        void should_validate_successfully_french_zip_codes(String zipCode) {
            assertThat(ValidationUtils.isValidZipCode(zipCode)).isTrue();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"7500", "750010", "abcde", "12A45"})
        void should_reject_invalid_zip_codes(String zipCode) {
            assertThat(ValidationUtils.isValidZipCode(zipCode)).isFalse();
        }
    }

    @Nested
    @DisplayName("String Content Verification")
    class StringVerification {
        @Test
        void should_return_true_for_non_empty_string() {
            assertThat(ValidationUtils.isNotEmpty("hello")).isTrue();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        void should_return_false_for_empty_or_whitespace_strings(String str) {
            assertThat(ValidationUtils.isNotEmpty(str)).isFalse();
        }
    }

    @Nested
    @DisplayName("Integer Validation")
    class IntegerValidation {
        @Test
        void should_return_true_for_positive_integers() {
            assertThat(ValidationUtils.isPositive(1)).isTrue();
            assertThat(ValidationUtils.isPositive(100)).isTrue();
        }

        @Test
        void should_return_false_for_zero_or_negative_integers() {
            assertThat(ValidationUtils.isPositive(0)).isFalse();
            assertThat(ValidationUtils.isPositive(-1)).isFalse();
            assertThat(ValidationUtils.isPositive(-100)).isFalse();
        }

        @Test
        void should_return_true_when_value_is_within_range() {
            assertThat(ValidationUtils.isInRange(5, 1, 10)).isTrue();
        }

        @Test
        void should_return_true_when_value_is_at_range_boundary() {
            assertThat(ValidationUtils.isInRange(1, 1, 10)).isTrue();
            assertThat(ValidationUtils.isInRange(10, 1, 10)).isTrue();
        }

        @Test
        void should_return_false_when_value_is_outside_range() {
            assertThat(ValidationUtils.isInRange(0, 1, 10)).isFalse();
            assertThat(ValidationUtils.isInRange(11, 1, 10)).isFalse();
        }
    }

    @Nested
    @DisplayName("Publication Year Validation")
    class YearValidation {
        @Test
        void should_validate_successfully_historical_and_current_years() {
            assertThat(ValidationUtils.isValidPublicationYear(2000)).isTrue();
            assertThat(ValidationUtils.isValidPublicationYear(1450)).isTrue();
            assertThat(ValidationUtils.isValidPublicationYear(Year.now().getValue())).isTrue();
        }

        @Test
        void should_reject_years_before_1450_or_in_future() {
            assertThat(ValidationUtils.isValidPublicationYear(1449)).isFalse();
            assertThat(ValidationUtils.isValidPublicationYear(Year.now().getValue() + 1)).isFalse();
        }
    }

    @Nested
    @DisplayName("Member Type Validation")
    class MemberTypeValidation {
        @ParameterizedTest
        @ValueSource(strings = {"STUDENT", "TEACHER", "STAFF", "EXTERNAL"})
        void should_validate_successfully_known_member_types(String type) {
            assertThat(ValidationUtils.isValidMemberType(type)).isTrue();
        }

        @Test
        void should_reject_unknown_member_types() {
            assertThat(ValidationUtils.isValidMemberType("ALIEN")).isFalse();
        }

        @Test
        void should_reject_null_member_type() {
            assertThat(ValidationUtils.isValidMemberType(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("Book Category Validation")
    class BookCategoryValidation {
        @ParameterizedTest
        @ValueSource(strings = {"ROMAN", "SCIENCE", "TECHNIQUE", "HISTOIRE", "JEUNESSE", "BD", "AUTRE"})
        void should_validate_successfully_known_book_categories(String category) {
            assertThat(ValidationUtils.isValidBookCategory(category)).isTrue();
        }

        @Test
        void should_reject_unknown_book_categories() {
            assertThat(ValidationUtils.isValidBookCategory("COOKING")).isFalse();
        }

        @Test
        void should_reject_null_book_category() {
            assertThat(ValidationUtils.isValidBookCategory(null)).isFalse();
        }
    }
    
    @Nested
    @DisplayName("Error Messages Verification")
    class ErrorMessagesVerification {
        @Test
        void should_return_standard_email_error_message() {
            assertThat(ValidationUtils.getEmailErrorMessage()).isEqualTo("L'adresse email n'est pas valide");
        }
        
        @Test
        void should_return_standard_isbn_error_message() {
            assertThat(ValidationUtils.getIsbnErrorMessage()).isEqualTo("L'ISBN doit contenir 10 ou 13 chiffres");
        }
        
        @Test
        void should_return_dynamic_required_field_message() {
            assertThat(ValidationUtils.getRequiredFieldMessage("Nom")).isEqualTo("Le champ 'Nom' est obligatoire");
        }
    }
}
