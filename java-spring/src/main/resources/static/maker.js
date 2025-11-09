let questionCount = 0;

document.addEventListener('DOMContentLoaded', () => {
    const addQuestionButton = document.getElementById('addQuestionButton');
    if (addQuestionButton) {
        addQuestionButton.addEventListener('click', addQuestionFromTemplate);
    }
});

function addQuestionFromTemplate() {
    questionCount++;

    const questionList = document.getElementById('questionList');
    const template = document.getElementById('questionTemplate');

    const clone = template.content.cloneNode(true);

    const questionLabel = clone.querySelector('.question-label');
    if (questionLabel) {
        questionLabel.textContent = `Frage ${questionCount}`;
    }

    const radioButtons = clone.querySelectorAll('input[type="radio"][name="correct"]');
    const hiddenCorrectInput = clone.querySelector('.hidden-correct-answer');

    const radioGroupName = `correctGroup${questionCount}`;

    radioButtons.forEach((radioButton) => {
        radioButton.name = radioGroupName;

        radioButton.addEventListener('change', () => {
            hiddenCorrectInput.value = radioButton.value;
        });
    });

    questionList.appendChild(clone);
}
