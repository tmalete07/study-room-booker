const MAX_GOALS = 5;
const MAX_ABOUT_LENGTH = 500;

let learner = null;
let profile = null;

// ---------- Loading and showing the profile ----------

async function loadProfile() {
    const message = document.getElementById("profile-message");

    try {
        const response = await fetch("/api/learners/" + encodeURIComponent(learner.studentNumber) + "/profile");
        const body = await response.json().catch(() => ({}));

        if (!response.ok) {
            showMessage(message, body.error || "Could not load your profile (status " + response.status + ").", "error");
            return;
        }
        profile = body;
        renderProfile();
    } catch (error) {
        showMessage(message, "Could not load your profile. Is the server running?", "error");
    }
}

function renderProfile() {
    const gradeAndClass = "Grade " + profile.grade + (profile.className ? " · Class " + profile.className : "");

    document.getElementById("profile-initials").textContent = initials(profile.name);
    showPhoto(profile.studentNumber);
    document.getElementById("profile-name").textContent = profile.name;
    document.getElementById("profile-grade-class").textContent = gradeAndClass;

    document.getElementById("detail-first-name").textContent = profile.firstName;
    document.getElementById("detail-surname").textContent = profile.surname || "-";
    document.getElementById("detail-grade").textContent = "Grade " + profile.grade;
    document.getElementById("detail-class").textContent = profile.className || "-";
    document.getElementById("detail-number").textContent = profile.studentNumber;
    document.getElementById("detail-email").textContent = profile.email || "-";

    renderSubjects();
    renderAbout();
    renderGoals();
}

/**
 * Shows the learner's photo from /img/learners/<student number>.jpg.
 * A learner without a photo keeps the initials instead.
 */
function showPhoto(studentNumber) {
    const photo = document.getElementById("profile-photo");
    const initialsText = document.getElementById("profile-initials");

    photo.onload = () => {
        photo.hidden = false;
        initialsText.hidden = true;
    };
    photo.onerror = () => {
        photo.hidden = true;
        initialsText.hidden = false;
    };
    photo.src = "/img/learners/" + encodeURIComponent(studentNumber) + ".jpg";
}

function renderSubjects() {
    const list = document.getElementById("subject-list");
    const count = profile.subjects.length;

    document.getElementById("subject-count").textContent = count === 1 ? "1 subject" : count + " subjects";

    if (count === 0) {
        list.replaceChildren(emptyNote("No subjects added yet."));
        return;
    }
    list.replaceChildren(...profile.subjects.map(subject => {
        const item = document.createElement("li");
        item.textContent = subject;
        return item;
    }));
}

function renderAbout() {
    const about = document.getElementById("about-text");
    about.textContent = profile.about || "Nothing here yet. Use Edit profile to tell people a little about yourself.";
    about.classList.toggle("empty-note", !profile.about);
}

function renderGoals() {
    const list = document.getElementById("goal-list");

    if (profile.goals.length === 0) {
        list.replaceChildren(emptyNote("No study goals yet."));
        return;
    }
    list.replaceChildren(...profile.goals.map(goal => {
        const item = document.createElement("li");

        const icon = document.createElement("span");
        icon.className = "goal-icon";
        icon.innerHTML = '<svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="12" r="8"/>'
            + '<circle cx="12" cy="12" r="4"/><circle cx="12" cy="12" r="0.8"/></svg>';

        const text = document.createElement("span");
        const title = document.createElement("span");
        title.className = "goal-title";
        title.textContent = goal.title;
        const detail = document.createElement("span");
        detail.className = "goal-detail";
        detail.textContent = goal.detail;
        text.append(title, detail);

        item.append(icon, text);
        return item;
    }));
}

function emptyNote(text) {
    const item = document.createElement("li");
    item.className = "empty-note";
    item.textContent = text;
    return item;
}

// ---------- Editing ----------

function openEditDialog() {
    if (!profile) {
        return;
    }
    document.getElementById("edit-subjects").value = profile.subjects.join("\n");
    document.getElementById("edit-about").value = profile.about;
    document.getElementById("goal-rows").replaceChildren();
    profile.goals.forEach(goal => addGoalRow(goal));

    showMessage(document.getElementById("edit-message"), "", "error");
    updateAboutCounter();
    updateAddGoalButton();
    document.getElementById("edit-dialog").showModal();
}

function addGoalRow(goal) {
    const rows = document.getElementById("goal-rows");

    const row = document.createElement("div");
    row.className = "goal-row";
    row.dataset.testid = "goal-row";

    const title = document.createElement("input");
    title.type = "text";
    title.name = "goal-title";
    title.placeholder = "Goal, e.g. Pass Mathematics";
    title.setAttribute("aria-label", "Goal title");
    title.value = goal ? goal.title : "";

    const detail = document.createElement("input");
    detail.type = "text";
    detail.name = "goal-detail";
    detail.placeholder = "How you will do it";
    detail.setAttribute("aria-label", "Goal detail");
    detail.value = goal ? goal.detail : "";

    const remove = document.createElement("button");
    remove.type = "button";
    remove.className = "remove-goal";
    remove.textContent = "Remove";
    remove.addEventListener("click", () => {
        row.remove();
        updateAddGoalButton();
    });

    row.append(title, detail, remove);
    rows.append(row);
    updateAddGoalButton();
    return row;
}

function updateAddGoalButton() {
    const count = document.querySelectorAll("#goal-rows .goal-row").length;
    document.getElementById("add-goal-button").disabled = count >= MAX_GOALS;
}

function updateAboutCounter() {
    const length = document.getElementById("edit-about").value.length;
    const counter = document.getElementById("about-counter");
    counter.textContent = "(" + length + " / " + MAX_ABOUT_LENGTH + ")";
    counter.classList.toggle("over-limit", length > MAX_ABOUT_LENGTH);
}

async function saveProfile(event) {
    event.preventDefault();

    const message = document.getElementById("edit-message");
    const saveButton = document.getElementById("save-profile-button");

    // Empty lines and completely empty goal rows are simply left out
    const subjects = document.getElementById("edit-subjects").value
        .split("\n")
        .map(line => line.trim())
        .filter(line => line.length > 0);

    const goals = [...document.querySelectorAll("#goal-rows .goal-row")]
        .map(row => ({
            title: row.querySelector("[name='goal-title']").value,
            detail: row.querySelector("[name='goal-detail']").value
        }))
        .filter(goal => goal.title.trim().length > 0 || goal.detail.trim().length > 0);

    const request = {
        subjects: subjects,
        about: document.getElementById("edit-about").value,
        goals: goals
    };

    saveButton.disabled = true;
    showMessage(message, "", "error");

    try {
        const response = await fetch("/api/learners/" + encodeURIComponent(learner.studentNumber) + "/profile", {
            method: "PUT",
            headers: { "Content-Type": "application/json", "X-Student-Id": learner.studentNumber },
            body: JSON.stringify(request)
        });
        const body = await response.json().catch(() => ({}));

        if (response.ok) {
            profile = body;
            renderProfile();
            document.getElementById("edit-dialog").close();
        } else {
            showMessage(message, body.error || "Could not save (status " + response.status + ").", "error");
        }
    } catch (error) {
        showMessage(message, "Could not reach the server. Please try again.", "error");
    } finally {
        saveButton.disabled = false;
    }
}

// ---------- Start ----------

function start() {
    learner = requireLearner();
    if (!learner) {
        return;
    }

    document.getElementById("edit-profile-button").addEventListener("click", openEditDialog);
    document.getElementById("add-goal-button").addEventListener("click", () => addGoalRow(null).querySelector("input").focus());
    document.getElementById("cancel-edit-button").addEventListener("click", () => document.getElementById("edit-dialog").close());
    document.getElementById("edit-about").addEventListener("input", updateAboutCounter);
    document.getElementById("edit-form").addEventListener("submit", saveProfile);

    loadProfile();
}

start();