
document.addEventListener('DOMContentLoaded', () => {

    const answerElements = document.querySelectorAll('.answerData');

    const answers = Array.from(answerElements).map(element => ({
        uid: element.dataset.uid,
        content: element.dataset.content
    }));

    answers.forEach(answer => {
        fetch(`/answers/${answer.uid}/count`)
            .then(response => response.text())
            .then(count => {
                render(answer.content, count);
            });
    });
});

function render(content, count) {
    const ul = document.getElementById('resultList');
    const li = document.createElement('li');
    li.textContent = `${content}: ${count}`;
    ul.appendChild(li);
}
