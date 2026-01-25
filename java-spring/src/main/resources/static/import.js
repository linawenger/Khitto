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

dropZone.addEventListener("click", () => fileInput.click());

fileInput.addEventListener("change", e => {
    const file = e.target.files[0];
    if (!file) return;

    dropZone.innerHTML = `<strong>${file.name}</strong>`;

    handleFile(file);
});

function handleFile(file) {
    if (!file.name.toLowerCase().endsWith(".json")) {
        alert("Please select a JSON file");
        importedQuestions = [];
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

            importedQuestions = rawQuestions
                .filter(q =>
                    q &&
                    q.type === "multiple" &&
                    typeof q.question === "string" &&
                    typeof q.correct_answer === "string" &&
                    Array.isArray(q.incorrect_answers) &&
                    q.incorrect_answers.length >= 3
                )
                .map(q => ({
                    question: decodeHtml(q.question),
                    a1: decodeHtml(q.correct_answer),
                    a2: decodeHtml(q.incorrect_answers[0]),
                    a3: decodeHtml(q.incorrect_answers[1]),
                    a4: decodeHtml(q.incorrect_answers[2]),
                    correctIndex: 1
                }));
        } catch (e) {
            importedQuestions = [];
            alert("JSON not in the correct format. Check an OpenTriviaDB export for the correct format.");
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
