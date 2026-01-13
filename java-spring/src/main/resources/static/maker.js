document.addEventListener("DOMContentLoaded", () => {
    const questionList = document.getElementById("questionList");
    const addBtn = document.getElementById("addQuestionButton");
    const template = document.getElementById("questionTemplate");
    const form = document.getElementById("makerForm");
    const publishBtn = document.getElementById("publishBtn");

    if (!questionList || !template) {
        return;
    }

    function wireQuestionBlock(block, correctIndex) {
        const hidden = block.querySelector(".hidden-correct-answer");
        const radios = block.querySelectorAll('input[type="radio"]');

        const groupName = "correct_radio_" + Date.now() + "_" + Math.random();
        let idxCorrect = correctIndex || 1;

        if (hidden) hidden.value = String(idxCorrect);

        radios.forEach((radio, index) => {
            const idx = index + 1;
            radio.name = groupName;
            radio.checked = (idx === idxCorrect);

            radio.addEventListener("change", () => {
                if (radio.checked && hidden) {
                    hidden.value = String(idx);
                }
            });
        });
    }

    function addEmptyQuestion() {
        const clone = template.content.firstElementChild.cloneNode(true);
        wireQuestionBlock(clone, 1);
        questionList.appendChild(clone);
    }

    function addQuestionFromData(q) {
        const clone = template.content.firstElementChild.cloneNode(true);

        const qInput = clone.querySelector('input[name="q[]"]');
        if (qInput) qInput.value = q.question || "";

        const a1 = clone.querySelector('input[name="a1[]"]');
        const a2 = clone.querySelector('input[name="a2[]"]');
        const a3 = clone.querySelector('input[name="a3[]"]');
        const a4 = clone.querySelector('input[name="a4[]"]');

        if (a1) a1.value = q.a1 || "";
        if (a2) a2.value = q.a2 || "";
        if (a3) a3.value = q.a3 || "";
        if (a4) a4.value = q.a4 || "";

        const correctIndex = q.correctIndex || 1;
        wireQuestionBlock(clone, correctIndex);

        questionList.appendChild(clone);
    }

    if (addBtn) {
        addBtn.addEventListener("click", addEmptyQuestion);
    }

    const existing = window.existingQuestions || [];
    if (existing.length > 0) {
        existing.forEach(addQuestionFromData);
    }

    if (publishBtn) {
        publishBtn.addEventListener("click", () => {
            if (validateMakerForm()) {
                form.submit();
            }
        });
    }
});
