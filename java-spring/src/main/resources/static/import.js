let importedQuestions = [];

const titleInput = document.getElementById("importTitle");
const importBtn  = document.getElementById("importBtn");
const dropZone   = document.getElementById("dropZone");
const fileInput  = document.getElementById("fileInput");

function decodeHtml(str) {
    const txt = document.createElement("textarea");
    txt.innerHTML = str;
    return txt.value;
}

function validate() {
    importBtn.disabled = importedQuestions.length === 0;
}

dropZone.addEventListener("click", () => fileInput.click());

dropZone.addEventListener("dragover", e => {
    e.preventDefault();
    dropZone.style.borderColor = "#888";
});

dropZone.addEventListener("dragleave", () => {
    dropZone.style.borderColor = "#bbb";
});

dropZone.addEventListener("drop", e => {
    e.preventDefault();
    dropZone.style.borderColor = "#bbb";

    const file = e.dataTransfer.files[0];
    if (file) handleFile(file);
});

fileInput.addEventListener("change", e => {
    const file = e.target.files[0];
    if (file) handleFile(file);
});

function handleFile(file) {
    if (!file.name.toLowerCase().endsWith(".json")) {
        alert("Please select a JSON file");
        return;
    }

    const reader = new FileReader();

    reader.onload = () => {
        try {
            const json = JSON.parse(reader.result);

            const rawQuestions = Array.isArray(json)
                ? json
                : Array.isArray(json.results)
                    ? json.results
                    : [];

            if (rawQuestions.length === 0) {
                alert("No valid questions found in JSON");
                return;
            }

            importedQuestions = rawQuestions
                .filter(q =>
                    q &&
                    q.type === "multiple" &&
                    typeof q.question === "string" &&
                    typeof q.correct_answer === "string" &&
                    Array.isArray(q.incorrect_answers) &&
                    q.incorrect_answers.length >= 3
                )
                .map(q => {
                    const answers = [
                        decodeHtml(q.correct_answer),
                        ...q.incorrect_answers.slice(0, 3).map(decodeHtml)
                    ];

                    while (answers.length < 4) answers.push("");

                    return {
                        question: decodeHtml(q.question),
                        a1: answers[0],   // ← richtige Antwort
                        a2: answers[1],
                        a3: answers[2],
                        a4: answers[3],
                        correctIndex: 1  // ← immer a1
                    };
                });

            if (importedQuestions.length === 0) {
                alert("JSON loaded, but no valid questions found");
                return;
            }

            dropZone.innerHTML = `
                <strong>${file.name}</strong><br>
                ${importedQuestions.length} questions loaded
            `;

            validate();

        } catch (e) {
            alert("JSON parse error");
        }
    };

    reader.readAsText(file);
}

importBtn.addEventListener("click", () => {
    sessionStorage.setItem("importGame", JSON.stringify({
        title: titleInput?.value?.trim() ?? "",
        questions: importedQuestions
    }));

    window.location.href = "/maker/import";
});
