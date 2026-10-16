-- One row per booking. Rooms are fixed, so they stay in memory.
CREATE TABLE IF NOT EXISTS bookings (
    id          VARCHAR(64)  PRIMARY KEY,
    room_id     VARCHAR(20)  NOT NULL,
    student_id  VARCHAR(100) NOT NULL,
    start_time  TIMESTAMP    NOT NULL,
    end_time    TIMESTAMP    NOT NULL
    );