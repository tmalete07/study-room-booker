// The booking page. The logged-in learner comes from session.js.

let learner = null;
let roomsById = new Map();

// ---------- Rooms ----------

/**
 * A learner sees their own grade's quiet room and the shared rooms, nothing else.
 */
function isOpenToLearner(room) {
    return room.grade === null || room.grade === undefined || room.grade === learner.grade;
}

async function loadRooms() {
    const message = document.getElementById("rooms-message");

    try {
        const response = await fetch("/api/rooms");
        if (!response.ok) {
            throw new Error("Could not load rooms (status " + response.status + ")");
        }
        const rooms = (await response.json()).filter(isOpenToLearner);
        roomsById = new Map(rooms.map(room => [room.id, room]));

        fillRoomSelect(rooms);
        chooseRoomFromAddress();
        showMessage(message, rooms.length === 0 ? "No rooms are available yet." : "", "");
    } catch (error) {
        showMessage(message, "Could not load rooms. Is the server running?", "error");
    }
}

function seatsLabel(capacity) {
    return capacity === 1 ? "1 seat" : capacity + " seats";
}

function fillRoomSelect(rooms) {
    const select = document.getElementById("room-select");
    const placeholder = select.querySelector("option[value='']");

    const options = rooms.map(room => {
        const option = document.createElement("option");
        option.value = room.id;
        option.textContent = room.name + " (" + seatsLabel(room.capacity) + ")";
        return option;
    });

    select.replaceChildren(placeholder, ...options);
}

/**
 * The Rooms page links here as /book.html?room=Q11, so that room is already chosen.
 * A room the learner cannot book (for example another grade's quiet room) is simply not chosen.
 */
function chooseRoomFromAddress() {
    const roomId = new URLSearchParams(window.location.search).get("room");
    if (roomId && roomsById.has(roomId)) {
        document.getElementById("room-select").value = roomId;
    }
}

// ---------- Booking ----------

async function submitBooking(event) {
    event.preventDefault();

    const form = event.target;
    const button = document.getElementById("book-button");
    const message = document.getElementById("booking-message");

    const date = form.elements["date"].value;
    const start = form.elements["start"].value;
    const end = form.elements["end"].value;

    const request = {
        roomId: form.elements["roomId"].value,
        studentId: learner.studentNumber,
        start: date + "T" + start,
        end: date + "T" + end
    };

    // Stops a double click from sending the same booking twice
    button.disabled = true;
    showMessage(message, "Booking...", "");

    try {
        const response = await fetch("/api/bookings", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(request)
        });
        const body = await response.json().catch(() => ({}));
        showMessage(message, "", "");

        if (response.ok) {
            openBookingDialog(roomName(body.roomId) + ", " + date + ", " + start + " to " + end);
        } else {
            openErrorDialog(body.error || "Booking failed (status " + response.status + ").");
        }
    } catch (error) {
        showMessage(message, "", "");
        openErrorDialog("Could not reach the server. Please try again.");
    } finally {
        button.disabled = false;
    }
}

// ---------- Pop-ups ----------

function openBookingDialog(details) {
    document.getElementById("booking-dialog-details").textContent = details;
    document.getElementById("booking-dialog").showModal();
}

/**
 * Shows why a booking was rejected. The form keeps what the learner typed, so they only fix one thing.
 */
function openErrorDialog(reason) {
    document.getElementById("error-dialog-details").textContent = reason;
    document.getElementById("error-dialog").showModal();
}

/**
 * Clears the form so the next booking starts fresh.
 * Runs whenever the success pop-up closes, whether by the OK button or the Escape key.
 */
function resetBookingForm() {
    const form = document.getElementById("booking-form");
    form.reset();
    document.getElementById("booking-date").value = tomorrowAsIsoDate();
    showMessage(document.getElementById("booking-message"), "", "");
    document.getElementById("room-select").focus();
}

function roomName(roomId) {
    const room = roomsById.get(roomId);
    return room ? room.name : roomId;
}

function tomorrowAsIsoDate() {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);

    const year = tomorrow.getFullYear();
    const month = String(tomorrow.getMonth() + 1).padStart(2, "0");
    const day = String(tomorrow.getDate()).padStart(2, "0");
    return year + "-" + month + "-" + day;
}

// ---------- Start ----------

function start() {
    learner = requireLearner();
    if (!learner) {
        return;
    }

    document.getElementById("booking-as").textContent =
        learner.name + " \u00b7 Grade " + learner.grade + " \u00b7 " + learner.studentNumber;
    document.getElementById("booking-date").value = tomorrowAsIsoDate();
    document.getElementById("booking-form").addEventListener("submit", submitBooking);
    document.getElementById("booking-dialog").addEventListener("close", resetBookingForm);

    loadRooms();
}

start();