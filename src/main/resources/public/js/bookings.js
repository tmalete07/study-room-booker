// My bookings: every upcoming booking (with Cancel), and the last 5 past bookings.

const RECENT_LIMIT = 5;
const DAYS = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];
const MONTHS = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];

let learner = null;
let roomNames = new Map();
let bookingToCancel = null;

// ---------- Loading ----------

async function loadPage() {
    const message = document.getElementById("bookings-message");

    try {
        const [roomsResponse, bookingsResponse] = await Promise.all([
            fetch("/api/rooms"),
            fetch("/api/bookings?studentId=" + encodeURIComponent(learner.studentNumber))
        ]);

        if (roomsResponse.ok) {
            const rooms = await roomsResponse.json();
            roomNames = new Map(rooms.map(room => [room.id, room.name]));
        }

        const body = await bookingsResponse.json().catch(() => ({}));
        if (!bookingsResponse.ok) {
            showMessage(message, body.error || "Could not load your bookings (status " + bookingsResponse.status + ").", "error");
            return;
        }

        showMessage(message, "", "");
        renderBookings(body);
    } catch (error) {
        showMessage(message, "Could not load your bookings. Is the server running?", "error");
    }
}

// ---------- Splitting and showing ----------

/**
 * A booking is upcoming until it has ended, so one that is happening right now still shows as upcoming.
 * Upcoming bookings are listed soonest first; past ones newest first, and only the last 5.
 */
function renderBookings(bookings) {
    const now = new Date();

    const upcoming = bookings
        .filter(booking => endOf(booking) > now)
        .sort((a, b) => startOf(a) - startOf(b));

    const recent = bookings
        .filter(booking => endOf(booking) <= now)
        .sort((a, b) => startOf(b) - startOf(a))
        .slice(0, RECENT_LIMIT);

    const upcomingCount = upcoming.length;
    document.getElementById("upcoming-count").textContent =
        upcomingCount === 1 ? "1 booking" : upcomingCount + " bookings";

    const upcomingList = document.getElementById("upcoming-list");
    if (upcoming.length === 0) {
        upcomingList.replaceChildren(emptyUpcomingNote());
    } else {
        upcomingList.replaceChildren(...upcoming.map(booking => bookingItem(booking, now, false)));
    }

    const pastList = document.getElementById("past-list");
    if (recent.length === 0) {
        pastList.replaceChildren(emptyNote("No past bookings yet."));
    } else {
        pastList.replaceChildren(...recent.map(booking => bookingItem(booking, now, true)));
    }
}

function bookingItem(booking, now, isPast) {
    const start = startOf(booking);
    const end = endOf(booking);

    const item = document.createElement("li");
    item.className = isPast ? "booking-item past" : "booking-item";
    item.dataset.testid = "booking-" + booking.id;

    const dateBlock = document.createElement("div");
    dateBlock.className = "date-block";
    dateBlock.setAttribute("aria-hidden", "true");
    dateBlock.append(
        span("date-day", DAYS[start.getDay()]),
        span("date-number", String(start.getDate())),
        span("date-month", MONTHS[start.getMonth()]));

    const details = document.createElement("div");
    const room = span("booking-room", roomNames.get(booking.roomId) || booking.roomId);
    if (!isPast && start <= now) {
        room.append(span("status-tag", "Now"));
    }
    details.append(room, span("booking-time", fullDate(start) + ", " + time(start) + " to " + time(end)));

    item.append(dateBlock, details);

    // Only bookings that have not started yet can be cancelled
    if (!isPast && start > now) {
        const cancel = document.createElement("button");
        cancel.type = "button";
        cancel.className = "button button-outline cancel-button";
        cancel.textContent = "Cancel";
        cancel.dataset.testid = "cancel-" + booking.id;
        cancel.setAttribute("aria-label", "Cancel booking for " + (roomNames.get(booking.roomId) || booking.roomId)
            + " on " + fullDate(start) + " at " + time(start));
        cancel.addEventListener("click", () => askToCancel(booking));
        item.append(cancel);
    }
    return item;
}

function emptyUpcomingNote() {
    const item = emptyNote("No upcoming bookings. ");
    const link = document.createElement("a");
    link.href = "/book.html";
    link.textContent = "Book a room";
    item.append(link);
    return item;
}

function emptyNote(text) {
    const item = document.createElement("li");
    item.className = "empty-note";
    item.textContent = text;
    return item;
}

function span(className, text) {
    const element = document.createElement("span");
    element.className = className;
    element.textContent = text;
    return element;
}

// ---------- Dates ----------

// The API sends times like "2026-10-23T09:00:00" with no time zone, which the browser reads as local time
function startOf(booking) {
    return new Date(booking.slot.start);
}

function endOf(booking) {
    return new Date(booking.slot.end);
}

function fullDate(date) {
    return DAYS[date.getDay()] + " " + date.getDate() + " " + MONTHS[date.getMonth()] + " " + date.getFullYear();
}

function time(date) {
    return String(date.getHours()).padStart(2, "0") + ":" + String(date.getMinutes()).padStart(2, "0");
}

// ---------- Cancelling ----------

function askToCancel(booking) {
    bookingToCancel = booking;
    const start = startOf(booking);
    document.getElementById("cancel-dialog-details").textContent =
        (roomNames.get(booking.roomId) || booking.roomId) + ", " + fullDate(start) + ", "
        + time(start) + " to " + time(endOf(booking));
    document.getElementById("cancel-dialog").showModal();
}

async function confirmCancel() {
    const dialog = document.getElementById("cancel-dialog");
    const message = document.getElementById("bookings-message");
    const button = document.getElementById("confirm-cancel-button");

    if (!bookingToCancel) {
        dialog.close();
        return;
    }

    button.disabled = true;
    try {
        const response = await fetch("/api/bookings/" + encodeURIComponent(bookingToCancel.id), {
            method: "DELETE",
            headers: { "X-Student-Id": learner.studentNumber }
        });

        if (response.ok) {
            showMessage(message, "Booking cancelled.", "success");
            await reloadBookings();
        } else {
            const body = await response.json().catch(() => ({}));
            showMessage(message, body.error || "Could not cancel (status " + response.status + ").", "error");
        }
    } catch (error) {
        showMessage(message, "Could not reach the server. Please try again.", "error");
    } finally {
        button.disabled = false;
        bookingToCancel = null;
        dialog.close();
    }
}

async function reloadBookings() {
    const response = await fetch("/api/bookings?studentId=" + encodeURIComponent(learner.studentNumber));
    if (response.ok) {
        renderBookings(await response.json());
    }
}

// ---------- Start ----------

function start() {
    learner = requireLearner();
    if (!learner) {
        return;
    }

    document.getElementById("keep-booking-button").addEventListener("click", () => {
        bookingToCancel = null;
        document.getElementById("cancel-dialog").close();
    });
    document.getElementById("confirm-cancel-button").addEventListener("click", confirmCancel);

    loadPage();
}

start();