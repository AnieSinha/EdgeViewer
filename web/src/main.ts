function setStats(text: string) {
    const stats = document.getElementById("stats");
    if (stats) stats.textContent = text;
}

// Example static metadata:
const resolution = "512 x 512";
const fps = "N/A (static frame)";

setStats(`Resolution: ${resolution} | FPS: ${fps}`);

console.log("Web viewer loaded successfully.");
