// Shared by every page a learner sees after logging in.
// The logged-in learner is saved by the login page for this browser tab only.
const LEARNER_KEY = "tss-learner";

function storedLearner() {
    try {
        return JSON.parse(sessionStorage.getItem(LEARNER_KEY));
    } catch (error) {
        return null;
    }
}

/**
 * Returns the logged-in learner, or sends the browser to the login page if nobody is logged in.
 * Also switches on the white theme for Grade 12 learners and wires up the Log out button.
 */
function requireLearner() {
    const learner = storedLearner();
    if (!learner) {
        window.location.replace("/login.html");
        return null;
    }

    document.body.classList.toggle("grade-12", learner.grade === 12);

    const logoutButton = document.getElementById("logout-button");
    if (logoutButton) {
        logoutButton.addEventListener("click", logOut);
    }
    return learner;
}

function logOut() {
    sessionStorage.removeItem(LEARNER_KEY);
    window.location.replace("/login.html");
}

function initials(name) {
    return name.split(" ")
        .filter(part => part.length > 0)
        .slice(0, 2)
        .map(part => part[0].toUpperCase())
        .join("");
}

function showMessage(element, text, type) {
    element.textContent = text;
    element.className = type ? "message " + type : "message";
}