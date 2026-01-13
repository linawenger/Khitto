function markInvalid(input, message) {
    input.classList.add("error");
    input.value = "";
    input.placeholder = message;
}

function clearInvalid(input, originalPlaceholder) {
    input.classList.remove("error");
    input.placeholder = originalPlaceholder;
}

function validateMakerForm() {
    let valid = true;

    const gameName = document.getElementById("gameName");
    clearInvalid(gameName, "Game name");

    if (!gameName.value.trim()) {
        markInvalid(gameName, "Game name required");
        valid = false;
    }

    document.querySelectorAll(".question-block").forEach(block => {
        const qInput = block.querySelector('input[name="q[]"]');
        clearInvalid(qInput, "Question text");

        if (!qInput.value.trim()) {
            markInvalid(qInput, "Question required");
            valid = false;
        }

        block.querySelectorAll('input[name^="a"]').forEach((answer, i) => {
            const defaultText = `Answer ${i + 1}`;
            clearInvalid(answer, defaultText);

            if (!answer.value.trim()) {
                markInvalid(answer, "Answer required");
                valid = false;
            }
        });
    });

    return valid;
}