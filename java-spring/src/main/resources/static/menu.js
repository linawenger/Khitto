document.addEventListener("DOMContentLoaded", function () {
    const gameListContainer = document.getElementById("gameList");
    if (!gameListContainer) {
        return;
    }

    const eventSource = new EventSource("/games/stream");

    eventSource.addEventListener("game_list", function (event) {
        gameListContainer.innerHTML = event.data;
    });

    eventSource.onerror = function (err) {
        console.error("Game SSE error:", err);
    };
});
