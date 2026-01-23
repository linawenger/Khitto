let answerAlreadySelected = false;

document.addEventListener('DOMContentLoaded', function () {
    const answerElements = document.querySelectorAll('#answers .answer-box');
    answerElements.forEach(function (answerElement) {
        answerElement.addEventListener('click', function () {
            selectAnswer(answerElement);
        });
    });
});

function selectAnswer(answerElement) {
    if (answerAlreadySelected) {
        return;
    }
    answerAlreadySelected = true;

    const uid = answerElement.dataset.uid; // Ditto UID

    fetch(`/answers/${uid}/count`, {
        method: 'POST'
    });

    //überflüssige logik
    const isCorrect = answerElement.dataset.correct === 'true';

    if (isCorrect) {
        answerElement.classList.add('correct');
    } else {
        answerElement.classList.add('wrong');

        document.querySelectorAll('#answers .answer-box').forEach(function (box) {
            if (box.dataset.correct === 'true') {
                box.classList.add('correct');
            }
        });
    }
}

document.addEventListener("DOMContentLoaded", () => {
    const nextButton = document.querySelector(".navigation-right");

    if (nextButton) {
        setTimeout(() => {
            nextButton.style.visibility = "visible";
        }, 1000); // 1 Sekunden noch ANPASSEN
    }
});

document.addEventListener("DOMContentLoaded", () => {
    const submissionElement = document.getElementById("submittedCount")
    if(!submissionElement) return;

    const uids = Array.from(document.querySelectorAll('#answers .answer-box'))
        .map(el => el.dataset.uid)
        .filter(Boolean);

    async function refreshSubmissions(){
        try{
            const response = await fetch("/answers/submissions", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(uids)
            });
            const data = await response.json();
            submissionElement.textContent = String(data.total ?? 0);
        } catch (e) {
        }
    }
    setInterval(refreshSubmissions, 1000);
})