package za.co.tapiso.studyroom.repository;

import za.co.tapiso.studyroom.db.Database;
import za.co.tapiso.studyroom.db.DatabaseException;
import za.co.tapiso.studyroom.domain.Booking;
import za.co.tapiso.studyroom.domain.TimeSlot;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Stores bookings in an SQL database (H2), so they survive when the app restarts.
 */
public class JdbcBookingRepository implements BookingRepository {

    private static final String COLUMNS = "id, room_id, student_id, start_time, end_time";

    private final Database database;

    public JdbcBookingRepository(Database database) {
        if (database == null) {
            throw new IllegalArgumentException("Database is required");
        }
        this.database = database;
    }

    @Override
    public Booking save(Booking booking) {
        if (booking == null) {
            throw new IllegalArgumentException("Booking is required");
        }
        // MERGE inserts a new row, or replaces the row that already has this id
        update("MERGE INTO bookings (" + COLUMNS + ") KEY (id) VALUES (?, ?, ?, ?, ?)",
                booking.id(),
                booking.roomId(),
                booking.studentId(),
                booking.slot().start(),
                booking.slot().end());
        return booking;
    }

    @Override
    public Optional<Booking> findById(String id) {
        return query("SELECT " + COLUMNS + " FROM bookings WHERE id = ?", id)
                .stream()
                .findFirst();
    }

    @Override
    public List<Booking> findAll() {
        return query("SELECT " + COLUMNS + " FROM bookings ORDER BY start_time, id");
    }

    @Override
    public List<Booking> findByRoomId(String roomId) {
        return query("SELECT " + COLUMNS + " FROM bookings WHERE room_id = ? ORDER BY start_time, id",
                roomId);
    }

    @Override
    public List<Booking> findByStudentIdOn(String studentId, LocalDate date) {
        // Everything from midnight on that day up to (but not including) midnight the next day
        return query("SELECT " + COLUMNS + " FROM bookings"
                        + " WHERE student_id = ? AND start_time >= ? AND start_time < ?"
                        + " ORDER BY start_time, id",
                studentId,
                date.atStartOfDay(),
                date.plusDays(1).atStartOfDay());
    }

    @Override
    public void deleteById(String id) {
        update("DELETE FROM bookings WHERE id = ?", id);
    }

    // ---------- JDBC helpers ----------

    private void update(String sql, Object... parameters) {
        try (Connection connection = database.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, parameters);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Could not run: " + sql, e);
        }
    }

    private List<Booking> query(String sql, Object... parameters) {
        try (Connection connection = database.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, parameters);

            try (ResultSet rows = statement.executeQuery()) {
                List<Booking> bookings = new ArrayList<>();
                while (rows.next()) {
                    bookings.add(toBooking(rows));
                }
                return List.copyOf(bookings);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not run: " + sql, e);
        }
    }

    private static void bind(PreparedStatement statement, Object... parameters) throws SQLException {
        for (int i = 0; i < parameters.length; i++) {
            statement.setObject(i + 1, parameters[i]);
        }
    }

    private static Booking toBooking(ResultSet row) throws SQLException {
        return new Booking(
                row.getString("id"),
                row.getString("room_id"),
                row.getString("student_id"),
                new TimeSlot(
                        row.getObject("start_time", LocalDateTime.class),
                        row.getObject("end_time", LocalDateTime.class)));
    }
}