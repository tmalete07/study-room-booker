// The logged-in learner, saved by the login page for this browser tab only
const LEARNER_KEY = "tss-learner";

let learner = null;
let roomsById = new Map();

function storedLearner() {
    try {
        return JSON.parse(sessionStorage.getItem(LEARNER_KEY));
    } catch (error) {
        return null;
    }
}

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

// ---------- Learner details ----------

function showLearner() {
    const firstName = learner.name.split(" ")[0];

    document.getElementById("learner-first-name").textContent = firstName;
    document.getElementById("learner-name").textContent = learner.name;
    document.getElementById("learner-grade").textContent = "Grade " + learner.grade;
    document.getElementById("learner-number").textContent = learner.studentNumber;
    document.getElementById("learner-chip-name").textContent = learner.name + " \u00b7 Grade " + learner.grade;
    document.getElementById("learner-initials").textContent = initials(learner.name);

    // Grade 12 learners get the white theme
    document.body.classList.toggle("grade-12", learner.grade === 12);
}

function initials(name) {
    return name.split(" ")
        .filter(part => part.length > 0)
        .slice(0, 2)
        .map(part => part[0].toUpperCase())
        .join("");
}

function logOut() {
    sessionStorage.removeItem(LEARNER_KEY);
    window.location.replace("/login.html");
}

// ---------- Rooms ----------

/**
 * A learner sees their own grade's quiet room and the shared rooms, nothing else.
 */
function isOpenToLearner(room) {
    return room.grade === null || room.grade === undefined || room.grade === learner.grade;
}

async function loadRooms() {
    const list = document.getElementById("room-list");
    const message = document.getElementById("rooms-message");

    try {
        const response = await fetch("/api/rooms");
        if (!response.ok) {
            throw new Error("Could not load rooms (status " + response.status + ")");
        }
        const rooms = (await response.json()).filter(isOpenToLearner);
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

    const tag = document.createElement("span");
    const isShared = room.grade === null || room.grade === undefined;
    tag.className = isShared ? "room-tag shared" : "room-tag";
    tag.textContent = isShared ? "All grades" : "Grade " + room.grade + " only";

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
    card.append(tag, name, meta);
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

// ---------- Start ----------

function start() {
    learner = storedLearner();

    // Only logged-in learners can use this page
    if (!learner) {
        window.location.replace("/login.html");
        return;
    }

    showLearner();
    document.getElementById("booking-date").value = tomorrowAsIsoDate();
    document.getElementById("booking-form").addEventListener("submit", submitBooking);
    document.getElementById("booking-dialog").addEventListener("close", resetBookingForm);
    document.getElementById("logout-button").addEventListener("click", logOut);

    checkHealth();
    loadRooms();
}

start();