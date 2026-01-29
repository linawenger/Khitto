document.addEventListener("DOMContentLoaded", () => {
    const imported = sessionStorage.getItem("importGame");
    if (!imported) return;

    try {
        const data = JSON.parse(imported);

        const nameInput = document.getElementById("gameName");
        if (nameInput && !nameInput.value.trim()) {
            nameInput.value = data.title || "";
        }

        window.existingQuestions = [
            ...(window.existingQuestions || []),
            ...(data.questions || [])
        ];

        sessionStorage.removeItem("importGame");
    } catch (e) {
        console.error("Failed to import game data", e);
    }
});
