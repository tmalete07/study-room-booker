async function checkHealth() {
    const statusElement = document.getElementById("health-status");
    try {
        const response = await fetch("/api/health");
        const data = await response.json();
        statusElement.textContent = data.status;
    } catch (error) {
        statusElement.textContent = "DOWN";
    }
}

checkHealth();