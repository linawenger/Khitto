document.addEventListener("DOMContentLoaded", function () {
    const meta = document.getElementById("gameMeta");
    if (!meta) return;

    const gameId = meta.dataset.gameId;
    let currentStatus = parseInt(meta.dataset.gameStatus, 10);

    if (!gameId || Number.isNaN(currentStatus)) {
        console.warn("game-logic: missing data-game-id or data-game-status");
        return;
    }

    const es = new EventSource(`/games/${encodeURIComponent(gameId)}/stream`);

    es.addEventListener("game", function (evt) {
        try {
            const game = JSON.parse(evt.data);
            const newStatus = game.status;

            if (typeof newStatus !== "number" || newStatus === currentStatus) return;

// Allow only ONE step forward per page load.
            const expected = currentStatus + 1;

            if (newStatus === expected) {
                currentStatus = newStatus;
                window.location.href = "/games/" + encodeURIComponent(gameId);
                return;
            }

            if (newStatus > expected) {
                console.warn(`game-logic: status jump ${currentStatus} -> ${newStatus}; gating to one step`);
                currentStatus = expected;
                window.location.href = "/games/" + encodeURIComponent(gameId);
                return;
            }
            if (newStatus < currentStatus) {
                console.warn(`game-logic: status went backwards ${currentStatus} -> ${newStatus}; resyncing`);
                currentStatus = newStatus;
                window.location.href = "/games/" + encodeURIComponent(gameId);
            }

        } catch (e) {
            console.error("game-logic: failed to parse SSE payload", e);
        }
    });

    es.onerror = function (err) {
        console.warn("game-logic: SSE connection issue", err);
    };

    window.addEventListener("beforeunload", function () {
        try { es.close(); } catch (_) {}
    });
});
