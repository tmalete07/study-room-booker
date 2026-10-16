package za.co.tapiso.studyroom.db;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DatabaseTest {

    @Test
    void createsTheBookingsTable() throws Exception {
        Database database = Database.inMemory(uniqueName());

        database.createSchema();

        assertThat(countTablesNamed(database, "BOOKINGS")).isEqualTo(1);
    }

    @Test
    void creatingTheSchemaTwiceIsSafe() {
        Database database = Database.inMemory(uniqueName());
        database.createSchema();

        assertThatCode(database::createSchema).doesNotThrowAnyException();
    }

    @Test
    void rejectsMissingUrl() {
        assertThatThrownBy(() -> new Database(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Database url is required");
    }

    private static int countTablesNamed(Database database, String tableName) throws Exception {
        try (Connection connection = database.connect();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(
                     "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = '" + tableName + "'")) {
            result.next();
            return result.getInt(1);
        }
    }

    private static String uniqueName() {
        return "test-" + UUID.randomUUID();
    }
}