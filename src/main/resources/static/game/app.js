'use strict';

/*
 * The game page.
 *
 * It holds no game state of its own. Every answer from /api/game is the whole picture - log,
 * place, time, who is here - and the page simply draws it. So a reload, a second tab or a turn
 * that failed can never leave the page showing something the session has moved past.
 *
 * The one exception is the turn being played: the player's line and the "denkt nach" placeholder
 * are drawn before the server has been asked, because a local model needs seconds and a page that
 * looks frozen for that long reads as broken.
 */

const el = {
    title: document.getElementById('title'),
    chapter: document.getElementById('chapter'),
    log: document.getElementById('log'),
    side: document.getElementById('side'),
    form: document.getElementById('input-form'),
    input: document.getElementById('input'),
    send: document.getElementById('send'),
    quit: document.getElementById('quit'),
    start: document.getElementById('start'),
    startForm: document.getElementById('start-form'),
    startError: document.getElementById('start-error'),
    player: document.getElementById('player'),
};

/** What the player can ask the game master without spending a turn on the story. */
const QUESTIONS = [
    'Wo sind wir?',
    'Wer ist hier?',
    'Wohin können wir gehen?',
    'Wie spät ist es?',
    'Was wissen wir?',
];

let busy = false;

/* ============================ boot ============================ */

async function boot() {
    el.form.addEventListener('submit', event => {
        event.preventDefault();
        play(el.input.value);
    });
    el.startForm.addEventListener('submit', event => {
        event.preventDefault();
        startGame();
    });
    el.quit.addEventListener('click', quitGame);

    const remembered = localStorage.getItem('rpg.player');
    if (remembered) {
        el.player.value = remembered;
    }

    try {
        const response = await fetch('../api/game/state');
        if (response.status === 204) {
            showStart();
            return;
        }
        if (!response.ok) {
            throw new Error(`${response.status} ${response.statusText}`);
        }
        render(await response.json());
    } catch (error) {
        showStart(`Die Anwendung antwortet nicht: ${error.message}`);
    }
}

/* ============================ the game ============================ */

async function startGame() {
    const player = el.player.value.trim();
    localStorage.setItem('rpg.player', player);
    el.startError.innerHTML = '';
    try {
        render(await postJson('../api/game/start', {player}));
        el.start.hidden = true;
        el.input.focus();
    } catch (error) {
        el.startError.innerHTML = `<div class="msg error">${escapeHtml(error.message)}</div>`;
    }
}

async function quitGame() {
    await fetch('../api/game/quit', {method: 'POST'});
    el.log.innerHTML = '';
    el.side.innerHTML = '';
    showStart();
}

/** One turn: show it as under way, hand it to the engine, draw whatever came back. */
async function play(text) {
    const said = text.trim();
    if (busy || !said) {
        return;
    }
    setBusy(true);
    el.input.value = '';
    appendTurn({actor: 'Player', statement: said, kind: 'STORY'});
    const stopWaiting = showWaiting();

    try {
        const view = await postJson('../api/game/input', {text: said});
        render(view);
    } catch (error) {
        // The turn never reached the engine, so nothing about the game changed - the player can
        // simply say it again.
        appendTurn({actor: 'Spielleiter', statement: error.message, kind: 'META'}, 'failed');
    } finally {
        stopWaiting();
        setBusy(false);
        el.input.focus();
    }
}

function setBusy(value) {
    busy = value;
    el.input.disabled = value;
    el.send.disabled = value;
}

/**
 * The placeholder while the model works, counting the seconds.
 * <p>
 * The count is not decoration: it is the only sign the player has that a turn taking half a minute
 * is still a turn being played and not a page that has died.
 */
function showWaiting() {
    const started = Date.now();
    const turn = appendTurn({actor: 'Spielleiter', statement: '', kind: 'META'}, 'pending');
    const text = turn.querySelector('.text');
    const tick = () => {
        const seconds = Math.round((Date.now() - started) / 1000);
        text.textContent = `denkt nach … (${seconds}s)`;
    };
    tick();
    const timer = setInterval(tick, 1000);
    return () => {
        clearInterval(timer);
        turn.remove();
    };
}

/* ============================ drawing ============================ */

function render(view) {
    el.title.textContent = view.title || 'Abenteuer';
    el.chapter.textContent = [view.chapter, view.author].filter(Boolean).join(' — ');
    el.quit.hidden = false;
    el.start.hidden = true;
    el.input.disabled = busy;
    el.send.disabled = busy;
    el.input.placeholder = 'Was tut ihr?';

    el.log.innerHTML = '';
    view.log.forEach(line => appendTurn(line));
    if (view.error) {
        appendTurn({actor: 'Spielleiter', statement: view.error, kind: 'META'}, 'failed');
    }
    renderSide(view);
}

/** Adds one line to the log and keeps the newest in view. Returns the element it made. */
function appendTurn(line, extraClass) {
    const turn = document.createElement('div');
    turn.className = ['turn', roleOf(line), extraClass].filter(Boolean).join(' ');
    turn.innerHTML = `<div class="actor">${escapeHtml(line.actor)}</div>`
        + `<div class="text">${escapeHtml(line.statement)}</div>`;
    el.log.appendChild(turn);
    el.log.scrollTop = el.log.scrollHeight;
    return turn;
}

/** How a line is shown: who said it, and whether it belongs to the story at all. */
function roleOf(line) {
    if (line.kind === 'META') {
        return 'meta';
    }
    if (line.actor === 'Player') {
        return 'player';
    }
    if (line.actor === 'Narrator') {
        return 'narrator';
    }
    return 'npc';
}

function renderSide(view) {
    const parts = [];
    parts.push('<h2>Hier seid ihr</h2>');
    parts.push(`<div class="place">${escapeHtml(view.location)}</div>`);
    if (view.locationDescription) {
        parts.push(`<p class="desc">${escapeHtml(view.locationDescription)}</p>`);
    }
    parts.push(`<p class="desc">Es ist ${escapeHtml(view.time)}.</p>`);

    parts.push('<h2>Bei euch</h2>');
    parts.push(view.persons.length
        ? chips(view.persons, person => `Ich spreche mit ${person.name}.`)
        : '<p class="empty">Niemand.</p>');

    parts.push('<h2>Wege von hier</h2>');
    parts.push(view.destinations.length
        ? chips(view.destinations, place => `Wir gehen zu: ${place.name}`)
        : '<p class="empty">Kein Weg führt weiter.</p>');

    parts.push('<h2>Den Spielleiter fragen</h2>');
    parts.push(chips(QUESTIONS.map(name => ({name, sub: null})), question => question.name));

    parts.push('<h2>Das wisst ihr</h2>');
    parts.push(view.knowledge.length
        ? view.knowledge.map(note =>
            `<div class="note"><b>${escapeHtml(note.name)}</b><span>${escapeHtml(note.text)}</span></div>`).join('')
        : '<p class="empty">Noch nichts herausgefunden.</p>');

    el.side.innerHTML = parts.join('');
    el.side.querySelectorAll('button[data-says]').forEach(button => {
        button.addEventListener('click', () => {
            el.input.value = button.dataset.says;
            el.input.focus();
        });
    });
}

/** Clickable suggestions: each writes a sentence into the input, which the player may edit. */
function chips(entries, sentence) {
    const buttons = entries.map(entry =>
        `<button type="button" data-says="${escapeHtml(sentence(entry))}">`
        + escapeHtml(entry.name)
        + (entry.sub ? ` <span class="sub">${escapeHtml(entry.sub)}</span>` : '')
        + '</button>').join('');
    return `<div class="chips">${buttons}</div>`;
}

function showStart(message) {
    el.start.hidden = false;
    el.quit.hidden = true;
    el.input.disabled = true;
    el.send.disabled = true;
    el.startError.innerHTML = message ? `<div class="msg error">${escapeHtml(message)}</div>` : '';
    el.player.focus();
}

/* ============================ plumbing ============================ */

async function postJson(url, body) {
    const response = await fetch(url, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(body),
    });
    if (!response.ok) {
        throw new Error(`${url} antwortet mit ${response.status} ${response.statusText}`);
    }
    return response.json();
}

function escapeHtml(value) {
    return String(value ?? '')
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;');
}

boot();
