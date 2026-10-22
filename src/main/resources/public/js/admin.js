// The admin office page: every booking from every learner, with filters.

const DAYS = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];
const MONTHS = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];

let allBookings = [];

// ---------- Loading ----------

async function loadRooms() {
    try {
        const response = await fetch("/api/rooms");
        if (!response.ok) {
            return;
        }
        const rooms = await response.json();
        const select = document.getElementById("filter-room");
        const allOption = select.querySelector("option[value='all']");

        select.replaceChildren(allOption, ...rooms.map(room => {
            const option = document.createElement("option");
            option.value = room.id;
            option.textContent = room.name;
            return option;
        }));
    } catch (error) {
        // The room filter just stays at "All rooms"
    }
}

async function loadBookings() {
    const message = document.getElementById("admin-message");
    const button = document.getElementById("refresh-button");

    button.disabled = true;
    try {
        const response = await fetch("/api/admin/bookings");
        const body = await response.json().catch(() => ({}));

        if (!response.ok) {
            showMessage(message, body.error || "Could not load bookings (status " + response.status + ").", "error");
            return;
        }
        allBookings = body;
        showMessage(message, "", "");
        render();
    } catch (error) {
        showMessage(message, "Could not load bookings. Is the server running?", "error");
    } finally {
        button.disabled = false;
    }
}

// ---------- Filtering and showing ----------

function render() {
    const now = new Date();
    renderSummary(now);

    const when = document.getElementById("filter-when").value;
    const grade = document.getElementById("filter-grade").value;
    const room = document.getElementById("filter-room").value;

    const shown = allBookings
        .filter(booking => when === "all"
            || (when === "upcoming" && endOf(booking) > now)
            || (when === "past" && endOf(booking) <= now))
        .filter(booking => grade === "all" || String(booking.grade) === grade)
        .filter(booking => room === "all" || booking.roomId === room);

    // Upcoming reads best soonest first; past reads best most recent first
    if (when === "past") {
        shown.sort((a, b) => startOf(b) - startOf(a));
    }

    document.getElementById("showing-count").textContent =
        "Showing " + shown.length + " of " + allBookings.length + " bookings";

    const body = document.getElementById("bookings-body");
    if (shown.length === 0) {
        body.replaceChildren(emptyRow());
    } else {
        body.replaceChildren(...shown.map(booking => bookingRow(booking, now)));
    }
}

function renderSummary(now) {
    const today = dateKey(now);

    document.getElementById("count-total").textContent = String(allBookings.length);
    document.getElementById("count-today").textContent =
        String(allBookings.filter(booking => dateKey(startOf(booking)) === today).length);
    document.getElementById("count-upcoming").textContent =
        String(allBookings.filter(booking => endOf(booking) > now).length);
}

function bookingRow(booking, now) {
    const row = document.createElement("tr");
    row.dataset.testid = "booking-row-" + booking.id;

    const status = statusOf(booking, now);
    const statusBadge = document.createElement("span");
    statusBadge.className = "status-pill status-" + status.toLowerCase();
    statusBadge.textContent = status;

    row.append(
        cell(fullDate(startOf(booking))),
        cell(time(startOf(booking)) + " to " + time(endOf(booking))),
        cell(booking.roomName),
        cell(booking.learnerName),
        cell(booking.grade === null || booking.grade === undefined ? "-" : "Grade " + booking.grade),
        cell(booking.studentNumber),
        cell(statusBadge));
    return row;
}

function statusOf(booking, now) {
    if (endOf(booking) <= now) {
        return "Past";
    }
    return startOf(booking) <= now ? "Now" : "Upcoming";
}

function emptyRow() {
    const row = document.createElement("tr");
    row.className = "empty-row";
    const td = document.createElement("td");
    td.colSpan = 7;
    td.textContent = "No bookings match these filters.";
    row.append(td);
    return row;
}

function cell(content) {
    const td = document.createElement("td");
    if (typeof content === "string") {
        td.textContent = content;
    } else {
        td.append(content);
    }
    return td;
}

// ---------- Dates ----------

// The API sends times like "2026-10-23T09:00:00" with no time zone, which the browser reads as local time
function startOf(booking) {
    return new Date(booking.start);
}

function endOf(booking) {
    return new Date(booking.end);
}

function dateKey(date) {
    return date.getFullYear() + "-" + date.getMonth() + "-" + date.getDate();
}

function fullDate(date) {
    return DAYS[date.getDay()] + " " + date.getDate() + " " + MONTHS[date.getMonth()] + " " + date.getFullYear();
}

function time(date) {
    return String(date.getHours()).padStart(2, "0") + ":" + String(date.getMinutes()).padStart(2, "0");
}

function showMessage(element, text, type) {
    element.textContent = text;
    element.className = type ? "message " + type : "message";
}

// ---------- Start ----------

function start() {
    ["filter-when", "filter-grade", "filter-room"].forEach(id =>
        document.getElementById(id).addEventListener("change", render));
    document.getElementById("refresh-button").addEventListener("click", loadBookings);

    loadRooms();
    loadBookings();
}

start();