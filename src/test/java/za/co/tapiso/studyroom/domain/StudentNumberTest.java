package za.co.tapiso.studyroom.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StudentNumberTest {

    // ---------- Valid numbers ----------

    @ParameterizedTest(name = "{0} becomes {1} (grade {2})")
    @CsvSource({
            "TSS1234-11, TSS1234-11, 11",
            "tss1234-11, TSS1234-11, 11",   // lower case is turned into upper case
            "tSs4321-10, TSS4321-10, 10",   // mixed case too
            "TSS0001-8,  TSS0001-8,  8",    // lowest grade, and the digits may start with 0
            "TSS9999-12, TSS9999-12, 12"    // highest grade
    })
    void acceptsValidNumbers(String input, String expectedValue, int expectedGrade) {
        StudentNumber number = StudentNumber.parse(input);

        assertThat(number.value()).isEqualTo(expectedValue);
        assertThat(number.grade()).isEqualTo(expectedGrade);
    }

    @Test
    void numbersThatDifferOnlyByCaseAreEqual() {
        assertThat(StudentNumber.parse("tss1234-11")).isEqualTo(StudentNumber.parse("TSS1234-11"));
    }

    // ---------- Wrong format ----------

    @ParameterizedTest(name = "\"{0}\" has the wrong format")
    @ValueSource(strings = {
            "TS1234-11",     // only 2 letters
            "TSSS1234-11",   // 4 letters
            "ABC1234-11",    // wrong letters
            "TSS123-11",     // only 3 digits
            "TSS12345-11",   // 5 digits
            "TSS12A4-11",    // a letter among the digits
            "1234-11",       // no letters at all
            "TSS-1234-11",   // extra dash
            "TSS1234",       // no grade
            "TSS1234-",      // dash but no grade
            "TSS1234_11",    // underscore instead of dash
            "TSS1234-08",    // grade with a leading zero
            "TSS1234-011",   // 3-digit grade
            "TSS1234-0"      // grade 0
    })
    void rejectsWrongFormat(String input) {
        assertThatThrownBy(() -> StudentNumber.parse(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid student number. Use the format TSS1234-11");
    }

    // ---------- Grade must be 8 to 12 ----------

    @ParameterizedTest(name = "grade in \"{0}\" is out of range")
    @ValueSource(strings = {
            "TSS1234-1",
            "TSS1234-7",     // just below the lowest grade
            "TSS1234-13",    // just above the highest grade
            "TSS1234-99"
    })
    void rejectsGradesOutsideEightToTwelve(String input) {
        assertThatThrownBy(() -> StudentNumber.parse(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid student number. Grade must be between 8 and 12");
    }

    // ---------- No spaces anywhere ----------

    @ParameterizedTest(name = "\"{0}\" contains a space")
    @ValueSource(strings = {
            "TSS 1234-11",   // space in the middle
            " TSS1234-11",   // space at the start
            "TSS1234-11 ",   // space at the end
            "TSS1234 -11",   // space before the dash
            "TSS1234-\t11"   // a tab counts as a space too
    })
    void rejectsSpaces(String input) {
        assertThatThrownBy(() -> StudentNumber.parse(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Student number cannot contain spaces");
    }

    @ParameterizedTest(name = "missing: \"{0}\"")
    @NullAndEmptySource
    void rejectsMissingNumber(String input) {
        assertThatThrownBy(() -> StudentNumber.parse(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Student number is required");
    }
}