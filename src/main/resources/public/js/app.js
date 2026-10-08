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

checkHealth();