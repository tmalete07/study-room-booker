let roomsById = new Map();

async function checkHealth() {
    const statusElement = document.getElementById("health-status");
    try {
        const response = await fetch("/api/health");
        const data = await response.json();
        statusElement.textContent = data.status;
        statusElement.dataset.status = data.status;
    } catch (error) {
        statusElement.textContent = "DOWN";
        statusElement.dataset.status = "DOWN";
    }
}

async function loadRooms() {
    const list = document.getElementById("room-list");
    const message = document.getElementById("rooms-message");

    try {
        const response = await fetch("/api/rooms");
        if (!response.ok) {
            throw new Error("Could not load rooms (status " + response.status + ")");
        }
        const rooms = await response.json();
        roomsById = new Map(rooms.map(room => [room.id, room]));

        list.replaceChildren(...rooms.map(createRoomCard));
        fillRoomSelect(rooms);
        message.textContent = rooms.length === 0 ? "No rooms are available yet." : "";
        message.classList.remove("error");
    } catch (error) {
        list.replaceChildren();
        message.textContent = "Could not load rooms. Is the server running?";
        message.classList.add("error");
    }
}

function createRoomCard(room) {
    const card = document.createElement("li");
    card.className = "room-card";
    card.dataset.testid = "room-" + room.id;

    const name = document.createElement("h3");
    name.className = "room-name";
    name.textContent = room.name;

    const meta = document.createElement("div");
    meta.className = "room-meta";

    const id = document.createElement("span");
    id.className = "room-id";
    id.textContent = room.id;

    const seats = document.createElement("span");
    seats.textContent = seatsLabel(room.capacity);

    meta.append(id, seats);
    card.append(name, meta);
    return card;
}

function seatsLabel(capacity) {
    return capacity === 1 ? "1 seat" : capacity + " seats";
}

// ---------- Booking form ----------

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
        studentId: form.elements["studentId"].value.trim(),
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

        if (response.ok) {
            showMessage(message, "", "");
            openBookingDialog(roomName(body.roomId) + ", " + date + ", " + start + " to " + end);
        } else {
            showMessage(message, body.error || "Booking failed (status " + response.status + ").", "error");
        }
    } catch (error) {
        showMessage(message, "Could not reach the server. Please try again.", "error");
    } finally {
        button.disabled = false;
    }
}

// ---------- Success pop-up ----------

function openBookingDialog(details) {
    const dialog = document.getElementById("booking-dialog");
    document.getElementById("booking-dialog-details").textContent = details;
    dialog.showModal();
}

/**
 * Clears the form so the next booking starts fresh.
 * Runs whenever the pop-up closes, whether by the OK button or the Escape key.
 */
function resetBookingForm() {
    const form = document.getElementById("booking-form");
    form.reset();
    document.getElementById("booking-date").value = tomorrowAsIsoDate();
    showMessage(document.getElementById("booking-message"), "", "");
    document.getElementById("student-id").focus();
}

function roomName(roomId) {
    const room = roomsById.get(roomId);
    return room ? room.name : roomId;
}

function showMessage(element, text, type) {
    element.textContent = text;
    element.className = type ? "message " + type : "message";
}

function tomorrowAsIsoDate() {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);

    const year = tomorrow.getFullYear();
    const month = String(tomorrow.getMonth() + 1).padStart(2, "0");
    const day = String(tomorrow.getDate()).padStart(2, "0");
    return year + "-" + month + "-" + day;
}

document.getElementById("booking-date").value = tomorrowAsIsoDate();
document.getElementById("booking-form").addEventListener("submit", submitBooking);
document.getElementById("booking-dialog").addEventListener("close", resetBookingForm);

checkHealth();
loadRooms();