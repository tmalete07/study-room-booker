package za.co.tapiso.studyroom.domain;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A Tswelopele Secondary School student number, for example TSS1234-11.
 * "TSS" is the school code, 1234 is the learner's own number (exactly 4 digits),
 * and the number after the dash is the learner's grade (8 to 12).
 * Lower-case letters are accepted and changed to upper case. Spaces are not allowed anywhere.
 */
public final class StudentNumber {

    public static final int LOWEST_GRADE = 8;
    public static final int HIGHEST_GRADE = 12;

    // TSS, then exactly 4 digits, a dash, and a grade with no leading zero
    private static final Pattern FORMAT = Pattern.compile("TSS\\d{4}-([1-9]\\d?)");

    private final String value;
    private final int grade;

    private StudentNumber(String value, int grade) {
        this.value = value;
        this.grade = grade;
    }

    /**
     * Checks the text and turns it into a StudentNumber, or explains what is wrong with it.
     */
    public static StudentNumber parse(String input) {
        if (input == null || input.isEmpty()) {
            throw new IllegalArgumentException("Student number is required");
        }
        if (input.chars().anyMatch(Character::isWhitespace)) {
            throw new IllegalArgumentException("Student number cannot contain spaces");
        }

        // Locale.ROOT makes upper-casing work the same on every computer, whatever its language
        String normalised = input.toUpperCase(Locale.ROOT);
        Matcher matcher = FORMAT.matcher(normalised);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid student number. Use the format TSS1234-11");
        }

        int grade = Integer.parseInt(matcher.group(1));
        if (grade < LOWEST_GRADE || grade > HIGHEST_GRADE) {
            throw new IllegalArgumentException(
                    "Invalid student number. Grade must be between " + LOWEST_GRADE + " and " + HIGHEST_GRADE);
        }
        return new StudentNumber(normalised, grade);
    }

    public String value() {
        return value;
    }

    public int grade() {
        return grade;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof StudentNumber that && value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}