package za.co.tapiso.studyroom.db;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Opens connections to an H2 database and creates the tables the app needs.
 */
public class Database {

    private static final String SCHEMA_FILE = "/db/schema.sql";

    private final String url;

    public Database(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("Database url is required");
        }
        this.url = url;
    }

    /**
     * A database that lives only in memory. Each name is a separate database,
     * so tests can each use their own and never share data.
     */
    public static Database inMemory(String name) {
        return new Database("jdbc:h2:mem:" + name + ";DB_CLOSE_DELAY=-1");
    }

    /**
     * A database saved to a file, so data survives when the app restarts.
     */
    public static Database inFile(String path) {
        return new Database("jdbc:h2:file:" + path);
    }

    public Connection connect() {
        try {
            return DriverManager.getConnection(url, "sa", "");
        } catch (SQLException e) {
            throw new DatabaseException("Could not connect to " + url, e);
        }
    }

    /**
     * Runs schema.sql. Safe to call more than once, because the SQL uses CREATE TABLE IF NOT EXISTS.
     */
    public void createSchema() {
        String sql = readSchemaFile();

        try (Connection connection = connect();
             Statement statement = connection.createStatement()) {
            for (String command : sql.split(";")) {
                if (!command.isBlank()) {
                    statement.execute(command);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not create the database schema", e);
        }
    }

    private static String readSchemaFile() {
        try (InputStream in = Database.class.getResourceAsStream(SCHEMA_FILE)) {
            if (in == null) {
                throw new IllegalStateException("Missing " + SCHEMA_FILE);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new DatabaseException("Could not read " + SCHEMA_FILE, e);
        }
    }
}