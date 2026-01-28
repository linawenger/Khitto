
document.addEventListener('DOMContentLoaded', () => {

    const correctUid = document.getElementById('correctAnswer').dataset.correctUid;

    const answerElements = document.querySelectorAll('.answerData');

    const answers = Array.from(answerElements).map(element => ({
        uid: element.dataset.uid,
        content: element.dataset.content,
        count: 0
    }));

    const fetches = answers.map(answer =>
        fetch(`/answers/${answer.uid}/count`)
            .then(response => response.text())
            .then(count => {
                answer.count = parseInt(count, 10) || 0;
            })
    );

    // have to wait for all to be fetched to be able to sort
    Promise.all(fetches).then(() => {
        answers.sort((a,b) => b.count - a.count || a.content.localeCompare(b.content)); //localCompare = alphabetical sorting

        answers.forEach(answer => {
            render(answer.content, answer.count, String(answer.uid) === String(correctUid));
        })
    });
});

function render(content, count, isCorrect) {
    const ul = document.getElementById('resultList');
    const li = document.createElement('li');
    li.textContent = `${content}: ${count}`;

    if (isCorrect) {
        li.style.fontWeight = 'bold';
    }

    ul.appendChild(li);
}


