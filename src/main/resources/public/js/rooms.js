// The Rooms page: the rooms this learner can book, each with a Book button.

let learner = null;

function isSharedRoom(room) {
    return room.grade === null || room.grade === undefined;
}

function seatsLabel(capacity) {
    return capacity === 1 ? "1 seat" : capacity + " seats";
}

async function loadRooms() {
    const message = document.getElementById("rooms-message");

    try {
        const response = await fetch("/api/rooms");
        if (!response.ok) {
            throw new Error("Could not load rooms (status " + response.status + ")");
        }
        const rooms = await response.json();

        // Only this learner's own quiet room, plus the shared rooms
        const gradeRooms = rooms.filter(room => room.grade === learner.grade);
        const sharedRooms = rooms.filter(isSharedRoom);

        document.getElementById("grade-room-list").replaceChildren(...gradeRooms.map(createRoomCard));
        document.getElementById("shared-room-list").replaceChildren(...sharedRooms.map(createRoomCard));
        showMessage(message, "", "");
    } catch (error) {
        showMessage(message, "Could not load rooms. Is the server running?", "error");
    }
}

function createRoomCard(room) {
    const card = document.createElement("li");
    card.className = "room-card";
    card.dataset.testid = "room-" + room.id;

    const tag = document.createElement("span");
    tag.className = isSharedRoom(room) ? "room-tag shared" : "room-tag";
    tag.textContent = isSharedRoom(room) ? "All grades" : "Grade " + room.grade + " only";

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

    const book = document.createElement("a");
    book.className = "button button-primary book-room-link";
    book.href = "/book.html?room=" + encodeURIComponent(room.id);
    book.textContent = "Book";
    book.dataset.testid = "book-" + room.id;
    book.setAttribute("aria-label", "Book the " + room.name);

    card.append(tag, name, meta, book);
    return card;
}

function start() {
    learner = requireLearner();
    if (!learner) {
        return;
    }
    loadRooms();
}

start();