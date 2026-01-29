
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

        const maxCount = Math.max(1, ...answers.map(a => a.count));

        answers.forEach(answer => {
            const isCorrect = String(answer.uid) === String(correctUid);
            renderBar(answer.content, answer.count, maxCount, isCorrect);
        })
    });
});

function renderBar(content, count, maxCount, isCorrect) {
    const ul = document.getElementById('resultList');
    const li = document.createElement('li');
    li.className = 'result-row' + (isCorrect ? ' correct' : '');

    const label = document.createElement('div');
    label.className = 'result-label';
    label.textContent = `${content}: ${count}`;

    const barWrap = document.createElement('div');
    barWrap.className = 'result-bar-wrap';

    const bar = document.createElement('div');
    bar.className = 'result-bar';

    const pct = Math.round((count / maxCount) * 100);
    bar.style.width = `${pct}%`;

    barWrap.appendChild(bar);
    li.appendChild(label);
    li.appendChild(barWrap);

    ul.appendChild(li);
}
