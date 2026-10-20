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

        list.replaceChildren(...rooms.map(createRoomCard));
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

checkHealth();
loadRooms();