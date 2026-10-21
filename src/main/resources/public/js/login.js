// The logged-in learner is kept for this browser tab only, and is gone when the tab closes
const LEARNER_KEY = "tss-learner";

function storedLearner() {
    try {
        return JSON.parse(sessionStorage.getItem(LEARNER_KEY));
    } catch (error) {
        return null;
    }
}

async function submitLogin(event) {
    event.preventDefault();

    const form = event.target;
    const button = document.getElementById("login-button");
    const message = document.getElementById("login-message");

    // Sent exactly as typed: the server checks the format and explains any problem
    const request = {
        name: form.elements["name"].value,
        studentNumber: form.elements["studentNumber"].value
    };

    button.disabled = true;
    showMessage(message, "Logging in...", "");

    try {
        const response = await fetch("/api/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(request)
        });
        const body = await response.json().catch(() => ({}));

        if (response.ok) {
            sessionStorage.setItem(LEARNER_KEY, JSON.stringify(body));
            window.location.replace("/");
            return;
        }
        showMessage(message, body.error || "Login failed (status " + response.status + ").", "error");
    } catch (error) {
        showMessage(message, "Could not reach the server. Please try again.", "error");
    } finally {
        button.disabled = false;
    }
}

function showMessage(element, text, type) {
    element.textContent = text;
    element.className = type ? "message " + type : "message";
}

// Someone who is already logged in goes straight to the booking page
if (storedLearner()) {
    window.location.replace("/");
}

document.getElementById("login-form").addEventListener("submit", submitLogin);