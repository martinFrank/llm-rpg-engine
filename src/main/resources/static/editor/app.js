'use strict';

/*
 * The editor page.
 *
 * Nothing here names a field of the adventure model. The forms are built from /api/schema, the
 * links between entities are found by walking the document for ids that resolve, and a save hands
 * the whole document back. That is what keeps a change to the model from reaching this file: a new
 * component on Person shows up because the schema grew, not because this code was told about it.
 *
 * The exception is the handful of overviews further down. Those talk about chapters, persons and
 * places on purpose, because that is the vocabulary the author thinks in - and they change when
 * the game changes, not when a field is added.
 *
 * Editing writes straight into `state.doc` and re-renders only on structural changes (adding a
 * row, switching a type), never on a keystroke - otherwise the field being typed into would lose
 * focus on every character.
 */

const state = {
    schema: null,
    doc: null,
    validation: null,
    source: null,
    dirty: false,
    message: null,   // {kind, lines} shown in the save bar
    byId: {},        // id -> { entry, sectionName, typeName, builtin }
    backlinks: {},   // id -> [{ from, path }]
};

const VIEWS = [
    {route: 'personen', label: 'Wer ist wann wo', render: renderPersonMatrix},
    {route: 'kapitel', label: 'Kapitel', render: renderChapters},
    {route: 'orte', label: 'Ortsnetz', render: renderLocationNetwork},
    {route: 'pruefung', label: 'Prüfung', render: renderValidation},
];

const SECTION_LABELS = {
    locations: 'Orte', persons: 'Personen', items: 'Gegenstände', flags: 'Flags',
    triggers: 'Trigger', dialogs: 'Dialoge', investigations: 'Untersuchungen',
    conditions: 'Bedingungen', chapters: 'Kapitel',
};

/* ============================ boot ============================ */

async function boot() {
    try {
        const [schema, doc, validation, source] = await Promise.all([
            getJson('../api/schema'),
            getJson('../api/adventure'),
            getJson('../api/validation'),
            getJson('../api/source'),
        ]);
        Object.assign(state, {schema, doc, validation, source});
        window.addEventListener('hashchange', () => render());
        window.addEventListener('beforeunload', event => {
            if (state.dirty) event.preventDefault();
        });
        render();
    } catch (error) {
        document.getElementById('main').innerHTML =
            `<h2 class="view-title">Der Editor kommt nicht an das Abenteuer</h2>
             <div class="msg error">${escapeHtml(error.message)}</div>
             <p class="view-hint">Läuft die Anwendung? Der Fehler steht sonst im Log des Servers.</p>`;
    }
}

async function getJson(url) {
    const response = await fetch(url);
    if (!response.ok) {
        throw new Error(`${url} antwortet mit ${response.status} ${response.statusText}`);
    }
    return response.json();
}

/** Everything that has to happen after the document changed shape. */
function render() {
    buildIndex();
    renderNav();
    renderSaveBar();
    route();
}

/* ============================ index ============================ */

function buildIndex() {
    state.byId = {};
    for (const section of state.schema.sections) {
        for (const entry of state.doc[section.name] || []) {
            state.byId[entry.id] = {
                entry,
                sectionName: section.name,
                typeName: entry.type || section.type,
            };
        }
    }
    for (const entry of state.doc.builtinConditions || []) {
        state.byId[entry.id] = {entry, sectionName: 'conditions', typeName: entry.type, builtin: true};
    }

    // Which entity mentions which - the adventure's own "find usages", and what makes renaming
    // and deleting safe.
    state.backlinks = {};
    for (const [id, node] of Object.entries(state.byId)) {
        walkReferences(node.entry, (target, path) => {
            if (target === id) return;
            (state.backlinks[target] ||= []).push({from: id, path});
        });
    }
}

/** Every string in the tree that names something, with the path it was found at. */
function walkReferences(node, visit, path = '') {
    if (Array.isArray(node)) {
        node.forEach((value, i) => walkReferences(value, visit, `${path}[${i}]`));
    } else if (node && typeof node === 'object') {
        for (const [key, value] of Object.entries(node)) {
            if (key === 'id' || key === 'type') continue;   // an entity's own identity, not a mention
            walkReferences(value, visit, path ? `${path}.${key}` : key);
        }
    } else if (typeof node === 'string' && state.byId[node]) {
        visit(node, path);
    }
}

/* ============================ paths into the document ============================ */

/*
 * A control carries the path of the value it edits, as a JSON array: ["persons",0,"name"]. That
 * keeps binding to one mechanism for every kind of field, however deeply the model nests.
 */

function valueAt(path) {
    return path.reduce((node, step) => (node == null ? node : node[step]), state.doc);
}

function setValueAt(path, value) {
    const parent = path.slice(0, -1).reduce((node, step) => node[step], state.doc);
    parent[path.at(-1)] = value;
    markDirty();
}

function markDirty() {
    if (!state.dirty) {
        state.dirty = true;
    }
    state.message = null;
    renderSaveBar();
}

/* ============================ saving ============================ */

async function save() {
    const button = document.getElementById('save-button');
    if (button) button.disabled = true;
    try {
        const response = await fetch('../api/adventure', {
            method: 'PUT',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(state.doc),
        });
        const result = await response.json();
        if (result.saved) {
            // The server normalises what it stores, so take its version rather than ours.
            state.doc = await getJson('../api/adventure');
            state.validation = await getJson('../api/validation');
            state.dirty = false;
            state.message = {
                kind: 'ok',
                lines: [`Gespeichert in ${state.source.file}.`
                + (result.warnings.length ? ` ${result.warnings.length} Warnung(en) offen.` : '')],
            };
            render();
        } else {
            state.message = {kind: 'error', lines: result.errors};
            renderSaveBar();
        }
    } catch (error) {
        state.message = {kind: 'error', lines: ['Speichern fehlgeschlagen: ' + error.message]};
        renderSaveBar();
    } finally {
        if (button) button.disabled = false;
    }
}

async function reloadFromFile() {
    state.doc = await (await fetch('../api/reload', {method: 'POST'})).json();
    state.validation = await getJson('../api/validation');
    state.dirty = false;
    state.message = {kind: 'ok', lines: ['Datei neu gelesen, nicht gespeicherte Änderungen sind weg.']};
    render();
}

function renderSaveBar() {
    const bar = document.getElementById('save-bar');
    const readOnly = !state.source.writable;
    const message = state.message
        ? `<div class="bar-messages">${state.message.lines
            .map(line => `<div class="msg ${state.message.kind}">${escapeHtml(line)}</div>`).join('')}</div>`
        : '';

    bar.className = state.dirty ? 'dirty' : '';
    bar.innerHTML = `
        <div class="bar-row">
            <span class="bar-state">${readOnly
        ? '<span class="badge">nur lesen</span> rpg.adventure.file ist nicht gesetzt'
        : state.dirty ? 'Nicht gespeicherte Änderungen' : 'Alles gespeichert'}</span>
            <span class="bar-actions">
                ${readOnly ? '' : `
                <button id="save-button" class="primary" ${state.dirty ? '' : 'disabled'}
                        onclick="save()">Speichern</button>
                <button onclick="reloadFromFile()">Datei neu lesen</button>`}
            </span>
        </div>${message}`;
}

/* ============================ navigation ============================ */

function renderNav() {
    document.getElementById('adventure-title').textContent = state.doc.metadata?.title || 'Abenteuer';

    const problems = state.validation.errors.length + state.validation.warnings.length;
    const overview = VIEWS.map(view => {
        const badge = view.route === 'pruefung' && problems
            ? `<span class="pill ${state.validation.errors.length ? 'error' : 'warn'}">${problems}</span>`
            : '';
        return navLink(`#/${view.route}`, view.label, badge);
    }).join('');

    const sections = state.schema.sections.map(section => navLink(
        `#/liste/${section.name}`,
        sectionLabel(section.name),
        `<span class="count">${(state.doc[section.name] || []).length}</span>`,
    )).join('');

    document.getElementById('nav-sections').innerHTML =
        `<div class="nav-group"><h2>Übersicht</h2>${overview}</div>
         <div class="nav-group"><h2>Bausteine</h2>${sections}</div>
         <div class="nav-group"><h2>Abenteuer</h2>${navLink('#/kopf', 'Titel und Handlung')}</div>`;
}

function navLink(href, label, trailing = '') {
    return `<a href="${href}" data-href="${href}">${escapeHtml(label)}${trailing}</a>`;
}

/** A German label where one is known, and the section's own name where it is not. */
function sectionLabel(name) {
    return SECTION_LABELS[name] || name;
}

/* ============================ routing ============================ */

function route() {
    const hash = location.hash || '#/personen';
    const main = document.getElementById('view');
    const [, kind, argument] = hash.split('/');

    if (kind === 'liste') {
        main.innerHTML = renderSectionList(argument);
    } else if (kind === 'id') {
        main.innerHTML = renderEntity(decodeURIComponent(argument));
    } else if (kind === 'kopf') {
        main.innerHTML = renderHead();
    } else {
        const view = VIEWS.find(candidate => candidate.route === kind) || VIEWS[0];
        main.innerHTML = view.render();
    }

    for (const link of document.querySelectorAll('#nav a')) {
        link.classList.toggle('active', link.dataset.href === hash);
    }
    bindEditors();
    window.scrollTo(0, 0);
}

/**
 * Wires every control the current view produced. Typing writes into the document without a
 * re-render, so the field keeps focus; anything that changes the shape re-renders explicitly.
 */
function bindEditors() {
    for (const control of document.querySelectorAll('[data-path]')) {
        const path = JSON.parse(control.dataset.path);
        const event = control.type === 'checkbox' || control.tagName === 'SELECT' ? 'change' : 'input';
        control.addEventListener(event, () => {
            setValueAt(path, readControl(control));
            if (control.tagName === 'TEXTAREA') growTextarea(control);
        });
        if (control.tagName === 'TEXTAREA') growTextarea(control);
    }
}

function readControl(control) {
    if (control.type === 'checkbox') return control.checked;
    if (control.type === 'number') return control.value === '' ? null : Number(control.value);
    if (control.dataset.nullable === 'true' && control.value === '') return null;
    return control.value;
}

/** How tall a text field may grow before it scrolls instead. */
const TEXTAREA_MAX_HEIGHT = 600;

/**
 * Text fields grow with their content so a description is readable without scrolling. Past the
 * cap they stop growing and scroll instead - the long plotSummary would otherwise have its tail
 * clipped off with no way to reach it.
 */
function growTextarea(area) {
    area.style.height = 'auto';
    const needed = area.scrollHeight + 2;
    const capped = needed > TEXTAREA_MAX_HEIGHT;
    area.style.height = (capped ? TEXTAREA_MAX_HEIGHT : needed) + 'px';
    area.style.overflowY = capped ? 'auto' : 'hidden';
}

/* ============================ the adventure's own fields ============================ */

function renderHead() {
    return `<h2 class="view-title">Titel und Handlung</h2>
        <p class="view-hint">Der Klappentext und die Zusammenfassung, die die Agenten als
           Hintergrund bekommen.</p>
        <div class="card">
            ${textField('Titel', ['metadata', 'title'])}
            ${textField('Autor', ['metadata', 'author'])}
            ${textField('Handlung', ['plotSummary'])}
        </div>`;
}

function textField(label, path) {
    const value = valueAt(path) ?? '';
    return `<div class="field"><label>${escapeHtml(label)}</label>
        <textarea data-path='${JSON.stringify(path)}'>${escapeHtml(value)}</textarea></div>`;
}

/* ============================ section lists ============================ */

function renderSectionList(sectionName) {
    const section = state.schema.sections.find(candidate => candidate.name === sectionName);
    if (!section) return notFound(`Unbekannte Sektion '${sectionName}'`);

    const entries = state.doc[sectionName] || [];
    const cards = entries.map(entry => `
        <a href="#/id/${encodeURIComponent(entry.id)}">
            <div class="name">${escapeHtml(displayName(entry))}</div>
            <div class="sub"><code>${escapeHtml(entry.id)}</code>${
        entry.type ? ` · ${escapeHtml(entry.type)}` : ''}${usageSummary(entry.id)}</div>
        </a>`).join('');

    const variants = state.schema.types[section.type]?.variants || [];
    const create = section.polymorphic
        ? variants.map(variant => `<button onclick="createEntity('${sectionName}','${variant}')">
              + ${escapeHtml(variant)}</button>`).join(' ')
        : `<button onclick="createEntity('${sectionName}')">
              + ${escapeHtml(sectionLabel(sectionName))}</button>`;

    return `<h2 class="view-title">${escapeHtml(sectionLabel(sectionName))}</h2>
            <p class="view-hint">${entries.length} Einträge.</p>
            <div class="toolbar">${create}</div>
            <div class="entity-list">${cards || '<p class="view-hint">noch nichts definiert</p>'}</div>`;
}

function usageSummary(id) {
    const count = (state.backlinks[id] || []).length;
    return count ? ` · von ${count} Stelle${count === 1 ? '' : 'n'} benutzt`
        : ' · <span class="hint-warn">von nichts benutzt</span>';
}

/* ============================ one entity, as a form ============================ */

function renderEntity(id) {
    const node = state.byId[id];
    if (!node) return notFound(`Unbekannte Id '${id}'`);

    const index = (state.doc[node.sectionName] || []).indexOf(node.entry);
    if (node.builtin || index < 0) {
        return renderBuiltin(node, id);
    }
    const path = [node.sectionName, index];
    const type = state.schema.types[node.typeName];

    const variants = state.schema.types[state.schema.sections
        .find(section => section.name === node.sectionName).type]?.variants || [];
    const typePicker = variants.length ? `
        <div class="field"><label>Art</label>
            <select onchange="changeVariant('${node.sectionName}',${index},this.value)">
                ${variants.map(variant => `<option value="${variant}"
                    ${variant === node.typeName ? 'selected' : ''}>${escapeHtml(variant)}</option>`).join('')}
            </select>
            <div class="hint">Die Art bestimmt, welche Felder es gibt.</div>
        </div>` : '';

    const fields = type.fields
        .filter(field => field.kind !== 'ID')
        .map(field => renderFieldEditor(field, [...path, field.name]))
        .join('');

    return `<h2 class="view-title">${escapeHtml(displayName(node.entry))}</h2>
            ${renderIdRow(id, node)}
            <div class="card">${typePicker}${fields}</div>
            ${renderUsages(id)}
            ${renderDangerZone(id, node.sectionName, index)}`;
}

function renderBuiltin(node, id) {
    const type = state.schema.types[node.typeName];
    const fields = (type?.fields || [])
        .filter(field => field.kind !== 'ID')
        .map(field => `<div class="field"><label>${escapeHtml(field.name)}</label>
            <div class="readonly">${escapeHtml(JSON.stringify(node.entry[field.name]))}</div></div>`)
        .join('');
    return `<h2 class="view-title">${escapeHtml(displayName(node.entry))}</h2>
        <p class="view-hint"><code>${escapeHtml(id)}</code> · von der Engine mitgeliefert,
           nicht bearbeitbar</p>
        <div class="card">${fields}</div>${renderUsages(id)}`;
}

/**
 * The id, and renaming it properly. Editing it in place would silently break every reference to
 * it, so renaming is its own action that rewrites them - the adventure's version of a rename
 * refactoring.
 */
function renderIdRow(id, node) {
    const uses = (state.backlinks[id] || []).length;
    return `<p class="view-hint">
        <code>${escapeHtml(id)}</code> · ${escapeHtml(node.typeName)}
        <button class="link" onclick="startRename('${escapeAttribute(id)}')">umbenennen</button>
        </p>
        <div id="rename-box" class="card hidden">
            <h3>Umbenennen</h3>
            <p class="hint">${uses
        ? `${uses} Verweis${uses === 1 ? '' : 'e'} ${uses === 1 ? 'wird' : 'werden'} mitgeändert.`
        : 'Auf diese Id verweist nichts.'}</p>
            <input id="rename-input" value="${escapeAttribute(id)}">
            <div id="rename-hint" class="hint"></div>
            <button class="primary" onclick="applyRename('${escapeAttribute(id)}')">Übernehmen</button>
            <button onclick="fillIdFromName('${escapeAttribute(id)}')">aus dem Namen erzeugen</button>
            <button onclick="document.getElementById('rename-box').classList.add('hidden')">Abbrechen</button>
        </div>`;
}

function renderDangerZone(id, sectionName, index) {
    const uses = (state.backlinks[id] || []).length;
    return `<div class="card">
        <h3>Löschen</h3>
        ${uses
        ? `<div class="msg warn">Erst die ${uses} Verweis${uses === 1 ? '' : 'e'} oben entfernen —
             sonst zeigen sie ins Leere.</div>`
        : `<button class="danger" onclick="deleteEntity('${sectionName}',${index})">
             Endgültig löschen</button>`}
    </div>`;
}

/** Where this entity is mentioned. The adventure's answer to "find usages". */
function renderUsages(id) {
    const usages = state.backlinks[id] || [];
    if (!usages.length) {
        return `<div class="card"><h3>Verwendet von</h3>
                <div class="msg warn">Nichts verweist hierauf.</div></div>`;
    }
    const rows = usages.map(usage => `
        <tr>
            <td>${idLink(usage.from)}</td>
            <td>${escapeHtml(displayName(state.byId[usage.from].entry))}</td>
            <td><code>${escapeHtml(usage.path)}</code></td>
        </tr>`).join('');
    return `<div class="card"><h3>Verwendet von <span class="count">(${usages.length})</span></h3>
            <div class="scroll-x"><table>
                <tr><th>Id</th><th>Name</th><th>Stelle</th></tr>${rows}
            </table></div></div>`;
}

/* ============================ field editors ============================ */

function renderFieldEditor(field, path) {
    const value = valueAt(path);
    return `<div class="field">
        <label>${escapeHtml(field.name)}${field.list ? ' <span class="count">(Liste)</span>' : ''}</label>
        ${field.list ? renderListEditor(field, path, value) : renderOneEditor(field, path, value)}
    </div>`;
}

function renderListEditor(field, path, value) {
    const entries = value || [];
    const rows = entries.map((entry, i) => `
        <div class="list-row">
            <div class="list-row-body">${renderOneEditor(field, [...path, i], entry)}</div>
            <div class="list-row-tools">
                <button title="nach oben" onclick="moveInList(${json(path)},${i},-1)"
                    ${i === 0 ? 'disabled' : ''}>↑</button>
                <button title="nach unten" onclick="moveInList(${json(path)},${i},1)"
                    ${i === entries.length - 1 ? 'disabled' : ''}>↓</button>
                <button title="entfernen" class="danger" onclick="removeFromList(${json(path)},${i})">✕</button>
            </div>
        </div>`).join('');
    return `<div class="list">${rows}
        <button onclick="addToList(${json(path)},'${field.type || ''}','${field.kind}')">
            + hinzufügen</button></div>`;
}

function renderOneEditor(field, path, value) {
    const bind = `data-path='${JSON.stringify(path)}'`;
    switch (field.kind) {
        case 'REF':
            return renderRefEditor(field, path, value);
        case 'ENUM':
            return `<select ${bind} data-nullable="true">
                <option value="">—</option>
                ${field.options.map(option => `<option value="${option}"
                    ${option === value ? 'selected' : ''}>${escapeHtml(option)}</option>`).join('')}
            </select>`;
        case 'BOOLEAN':
            return `<input type="checkbox" ${bind} ${value ? 'checked' : ''}>`;
        case 'NUMBER':
            return `<input type="number" step="any" ${bind} value="${value ?? ''}">`;
        case 'EMBEDDED':
            return renderEmbeddedEditor(field, path, value);
        case 'ID':
            return `<div class="readonly">${escapeHtml(String(value ?? ''))}</div>`;
        default:
            return `<textarea ${bind}>${escapeHtml(String(value ?? ''))}</textarea>`;
    }
}

function renderRefEditor(field, path, value) {
    const candidates = candidatesFor(field.type);
    const missing = value && !state.byId[value];
    return `<select data-path='${JSON.stringify(path)}' data-nullable="true"
                    class="${missing ? 'broken' : ''}">
            <option value="">—</option>
            ${missing ? `<option value="${escapeAttribute(value)}" selected>
                ${escapeHtml(value)} (gibt es nicht)</option>` : ''}
            ${candidates.map(candidate => `<option value="${escapeAttribute(candidate.id)}"
                ${candidate.id === value ? 'selected' : ''}>${
        escapeHtml(optionLabel(candidate))}</option>`).join('')}
        </select>${value ? ` ${idLink(value)}` : ''}`;
}

/** Name and id, unless the thing has no name of its own and the two would read the same twice. */
function optionLabel(candidate) {
    const name = displayName(candidate);
    return name === candidate.id ? candidate.id : `${name} — ${candidate.id}`;
}

/**
 * What may go in a reference field. Built-in conditions belong in the list for a condition, and a
 * field typed only as Identifiable - what a chapter lets the player investigate - offers
 * everything, because which of those actually fit is a rule the validator owns.
 */
function candidatesFor(typeName) {
    if (typeName === 'Identifiable') {
        return state.schema.sections.flatMap(section => state.doc[section.name] || []);
    }
    const section = state.schema.sections.find(candidate => candidate.type === typeName);
    const own = section ? state.doc[section.name] || [] : [];
    return typeName === 'Condition' ? own.concat(state.doc.builtinConditions || []) : own;
}

function renderEmbeddedEditor(field, path, value) {
    if (value == null) {
        return `<div class="empty">— <button onclick="createEmbedded(${json(path)},'${field.type}')">
            anlegen</button></div>`;
    }
    const typeName = value.type || field.type;
    const inner = (state.schema.types[typeName]?.fields || [])
        .filter(nested => nested.kind !== 'ID')
        .map(nested => renderFieldEditor(nested, [...path, nested.name]))
        .join('');
    // Inside a list the row's own ✕ removes the entry. Offering "entfernen" as well would empty
    // it in place and leave a hole in the list, which is never what was meant.
    const inList = typeof path.at(-1) === 'number';
    const clear = inList ? ''
        : `<button class="danger" onclick="clearValue(${json(path)})">entfernen</button>`;
    return `<div class="embedded">${inner}${clear}</div>`;
}

/* ============================ structural edits ============================ */

function addToList(path, typeName, kind) {
    const list = valueAt(path) || [];
    list.push(kind === 'EMBEDDED' ? skeletonOf(typeName) : null);
    setValueAt(path, list);
    route();
}

function removeFromList(path, index) {
    const list = valueAt(path);
    list.splice(index, 1);
    setValueAt(path, list);
    render();
}

function moveInList(path, index, by) {
    const list = valueAt(path);
    const target = index + by;
    [list[index], list[target]] = [list[target], list[index]];
    setValueAt(path, list);
    route();
}

function clearValue(path) {
    setValueAt(path, null);
    route();
}

function createEmbedded(path, typeName) {
    setValueAt(path, skeletonOf(typeName));
    route();
}

/** An empty value of a type, from the schema - so a new record has exactly the fields it should. */
function skeletonOf(typeName, variant) {
    const type = state.schema.types[variant || typeName];
    const fresh = {};
    if (variant) fresh.type = variant;
    for (const field of type?.fields || []) {
        if (field.list) fresh[field.name] = [];
        else if (field.kind === 'TEXT' || field.kind === 'ID') fresh[field.name] = '';
        else if (field.kind === 'BOOLEAN') fresh[field.name] = false;
        else fresh[field.name] = null;
    }
    return fresh;
}

function createEntity(sectionName, variant) {
    const section = state.schema.sections.find(candidate => candidate.name === sectionName);
    const fresh = skeletonOf(section.type, variant);
    // Random rather than sequential: two ids made moments apart would differ only in their last
    // characters, which is exactly the near-collision the distance rule rejects.
    fresh.id = `${section.namespace}.neu-${Math.random().toString(36).slice(2, 8)}`;
    (state.doc[sectionName] ||= []).push(fresh);
    markDirty();
    buildIndex();
    location.hash = `#/id/${encodeURIComponent(fresh.id)}`;
    render();
}

function deleteEntity(sectionName, index) {
    const removed = state.doc[sectionName][index];
    if ((state.backlinks[removed.id] || []).length) {
        return;     // the button is not offered in that case; this is the belt to the braces
    }
    state.doc[sectionName].splice(index, 1);
    markDirty();
    location.hash = `#/liste/${sectionName}`;
    render();
}

/* ============================ renaming ============================ */

function startRename(id) {
    document.getElementById('rename-box').classList.remove('hidden');
    const input = document.getElementById('rename-input');
    input.focus();
    input.select();
    input.oninput = () => suggestFromName(id);
}

/**
 * Turns the name into an id, server side. The umlaut spelling and the minimum distance between
 * ids are rules of the model, so they are applied where they are defined rather than repeated
 * here - and a name that would produce a confusable id is reported with the id in the way.
 */
async function fillIdFromName(oldId) {
    const entry = state.byId[oldId]?.entry;
    const hint = document.getElementById('rename-hint');
    const name = entry?.name || entry?.topic || entry?.trigger || '';
    if (!name.trim()) {
        hint.className = 'hint hint-warn';
        hint.textContent = 'Erst einen Namen eintragen, dann lässt sich eine Id daraus machen.';
        return;
    }
    const response = await fetch('../api/id-suggestion', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({namespace: oldId.split('.')[0], name}),
    });
    const suggestion = await response.json();
    if (!suggestion.id) {
        hint.className = 'hint hint-warn';
        hint.textContent = `Aus „${name}" lässt sich keine Id bilden.`;
        return;
    }
    document.getElementById('rename-input').value = suggestion.id;
    if (suggestion.taken) {
        hint.className = 'hint hint-warn';
        hint.textContent = 'Diese Id gibt es schon — der Name müsste sich unterscheiden.';
    } else if (suggestion.tooClose.length) {
        hint.className = 'hint hint-warn';
        hint.textContent = 'Zu nah an: ' + suggestion.tooClose.join(', ')
            + ' — ein Agent könnte die beiden verwechseln.';
    } else {
        hint.className = 'hint';
        hint.textContent = 'Frei und weit genug von allen anderen entfernt.';
    }
}

/** Says what is wrong with the id being typed, before it is applied. */
function suggestFromName(oldId) {
    const proposed = document.getElementById('rename-input').value;
    const hint = document.getElementById('rename-hint');
    const namespace = oldId.split('.')[0];
    if (!/^[a-z]+\.[a-z0-9][a-z0-9-]*$/.test(proposed)) {
        hint.className = 'hint hint-warn';
        hint.textContent = 'Form muss namespace.slug sein, kleingeschrieben, ohne Umlaute.';
    } else if (!proposed.startsWith(namespace + '.')) {
        hint.className = 'hint hint-warn';
        hint.textContent = `Der Namespace muss '${namespace}.' bleiben.`;
    } else if (proposed !== oldId && state.byId[proposed]) {
        hint.className = 'hint hint-warn';
        hint.textContent = 'Diese Id gibt es schon.';
    } else {
        hint.className = 'hint';
        hint.textContent = 'Der Mindestabstand zu anderen Ids wird beim Speichern geprüft.';
    }
}

/**
 * Renames an entity and every reference to it. The references are the ones the index found, so
 * only actual mentions are rewritten - a matching word inside a description is left alone.
 */
function applyRename(oldId) {
    const newId = document.getElementById('rename-input').value.trim();
    if (!newId || newId === oldId) {
        document.getElementById('rename-box').classList.add('hidden');
        return;
    }
    const node = state.byId[oldId];
    node.entry.id = newId;
    for (const section of state.schema.sections) {
        for (const entry of state.doc[section.name] || []) {
            rewriteReferences(entry, oldId, newId);
        }
    }
    markDirty();
    location.hash = `#/id/${encodeURIComponent(newId)}`;
    render();
}

function rewriteReferences(node, oldId, newId) {
    if (Array.isArray(node)) {
        node.forEach((value, i) => {
            if (value === oldId) node[i] = newId;
            else rewriteReferences(value, oldId, newId);
        });
    } else if (node && typeof node === 'object') {
        for (const [key, value] of Object.entries(node)) {
            if (key === 'id' || key === 'type') continue;
            if (value === oldId) node[key] = newId;
            else rewriteReferences(value, oldId, newId);
        }
    }
}

/* ============================ overviews ============================ */

/**
 * Rows are persons, columns are chapters. The screen that says who can be met where, in one
 * place - the thing that is otherwise spread over one personCondition block per chapter.
 */
function renderPersonMatrix() {
    const chapters = state.doc.chapters || [];
    const persons = state.doc.persons || [];
    if (!chapters.length) return `<h2 class="view-title">Wer ist wann wo</h2>
        <div class="msg warn">Das Abenteuer hat noch keine Kapitel.</div>`;

    const head = chapters.map(chapter => `<th>${escapeHtml(displayName(chapter))}</th>`).join('');

    const rows = persons.map(person => {
        const cells = chapters.map(chapter => {
            const places = (chapter.personConditions || [])
                .filter(condition => condition.person === person.id)
                .map(condition => `${idLink(condition.location)} <span class="badge">${
                    escapeHtml(conditionLabel(condition.condition))}</span>`);
            const topics = (chapter.dialogConditions || [])
                .filter(condition => condition.person === person.id)
                .map(condition => idLink(condition.dialog));

            if (!places.length) {
                return `<td class="empty-cell">nirgends${topics.length
                    ? '<br><span class="hint-warn">hat aber Themen</span>' : ''}</td>`;
            }
            return `<td>${places.join('<br>')}${
                topics.length ? `<div style="margin-top:6px">${topics.join(' ')}</div>` : ''}</td>`;
        }).join('');
        return `<tr><td><strong>${escapeHtml(displayName(person))}</strong><br>${
            idLink(person.id)}</td>${cells}</tr>`;
    }).join('');

    return `<h2 class="view-title">Wer ist wann wo</h2>
            <p class="view-hint">Eine Person ohne Ort in einem Kapitel ist dort nicht anzutreffen.
               Themen unter dem Ort sind die Dialoge, die sie in diesem Kapitel führen kann.</p>
            <div class="card scroll-x"><table>
                <tr><th>Person</th>${head}</tr>${rows}
            </table></div>`;
}

function renderChapters() {
    const chapters = state.doc.chapters || [];
    if (!chapters.length) return `<h2 class="view-title">Kapitel</h2>
        <div class="msg warn">Das Abenteuer hat noch keine Kapitel.</div>`;

    const cards = chapters.map((chapter, index) => `
        <div class="card">
            <h3>${index + 1}. ${escapeHtml(displayName(chapter))} ${idLink(chapter.id)}</h3>
            <div class="field"><label>Start</label>
                ${idLink(chapter.intro?.startLocation)}
                <span class="badge">${escapeHtml(chapter.intro?.startTime || '?')}</span></div>
            ${conditionTable('Offene Orte', ['Ort', 'wenn'],
        (chapter.locationConditions || []).map(entry =>
            [idLink(entry.location), conditionLabel(entry.condition)]))}
            ${conditionTable('Personen', ['Person', 'Ort', 'wenn'],
        (chapter.personConditions || []).map(entry =>
            [idLink(entry.person), idLink(entry.location), conditionLabel(entry.condition)]))}
            ${conditionTable('Gespräche', ['Person', 'Thema', 'wenn'],
        (chapter.dialogConditions || []).map(entry =>
            [idLink(entry.person), idLink(entry.dialog), conditionLabel(entry.condition)]))}
            ${conditionTable('Zu untersuchen', ['bei', 'Untersuchung', 'wenn'],
        (chapter.investigateConditions || []).map(entry =>
            [idLink(entry.subject), idLink(entry.investigation), conditionLabel(entry.condition)]))}
            <div class="field"><label>Kapitel endet, wenn</label>
                ${idLink(chapter.chapterFinishedCondition)}
                ${renderFinishFlags(chapter.chapterFinishedCondition)}</div>
        </div>`).join('');

    return `<h2 class="view-title">Kapitel</h2>
            <p class="view-hint">Was in jedem Kapitel gilt. Jedes Kapitel listet vollständig —
               es erbt nichts vom vorigen. Zum Ändern das Kapitel unter „Bausteine" öffnen.</p>${cards}`;
}

/** Whether the flags a chapter waits for can be raised at all - the "chapter can never end" trap. */
function renderFinishFlags(conditionId) {
    const condition = state.byId[conditionId]?.entry;
    const flags = condition?.consideredFlags || [];
    if (!flags.length) return '';

    const raisedFlags = new Set();
    for (const trigger of state.doc.triggers || []) {
        for (const flag of trigger.event?.raisedFlags || []) raisedFlags.add(flag);
    }
    const rows = flags.map(flag => raisedFlags.has(flag)
        ? `<div class="msg ok">${flag} — wird gesetzt</div>`
        : `<div class="msg error">${flag} — von keinem Trigger gesetzt, das Kapitel kann nicht enden</div>`);
    return `<div style="margin-top:8px">${rows.join('')}</div>`;
}

function conditionTable(title, headers, rows) {
    if (!rows.length) {
        return `<div class="field"><label>${escapeHtml(title)}</label><div class="empty">—</div></div>`;
    }
    const head = headers.map(header => `<th>${escapeHtml(header)}</th>`).join('');
    const body = rows.map(cells => `<tr>${cells.map(cell => `<td>${cell}</td>`).join('')}</tr>`).join('');
    return `<div class="field"><label>${escapeHtml(title)}</label>
            <div class="scroll-x"><table><tr>${head}</tr>${body}</table></div></div>`;
}

/**
 * The map, and the trap that goes with it: a destination is a one-way street unless the other
 * side lists the way back.
 */
function renderLocationNetwork() {
    const locations = state.doc.locations || [];
    const backTo = new Map(locations.map(location => [location.id, new Set(location.destinationIds || [])]));

    const rows = locations.map(location => {
        const ways = (location.destinationIds || []).map(destination => {
            const mutual = backTo.get(destination)?.has(location.id);
            return `${idLink(destination)}${mutual ? '' : ' <span class="badge warn-badge">nur hin</span>'}`;
        });
        const openedBy = (state.doc.chapters || [])
            .filter(chapter => (chapter.locationConditions || [])
                .some(condition => condition.location === location.id))
            .map(chapter => escapeHtml(displayName(chapter)));

        return `<tr>
            <td><strong>${escapeHtml(displayName(location))}</strong><br>${idLink(location.id)}</td>
            <td>${ways.join('<br>') || '<span class="empty">Sackgasse</span>'}</td>
            <td>${openedBy.join('<br>') || '<span class="hint-warn">von keinem Kapitel geöffnet</span>'}</td>
        </tr>`;
    }).join('');

    return `<h2 class="view-title">Ortsnetz</h2>
            <p class="view-hint">Wege sind nicht automatisch beidseitig — „nur hin" heißt, dass der
               Zielort den Rückweg nicht listet.</p>
            <div class="card scroll-x"><table>
                <tr><th>Ort</th><th>führt nach</th><th>geöffnet in</th></tr>${rows}
            </table></div>`;
}

function renderValidation() {
    const {errors, warnings} = state.validation;
    const list = (messages, kind) => messages.length
        ? messages.map(message => `<div class="msg ${kind}">${escapeHtml(message)}</div>`).join('')
        : '<div class="msg ok">nichts zu melden</div>';

    return `<h2 class="view-title">Prüfung</h2>
            <p class="view-hint">Dieselben Regeln, die auch beim Start des Spiels laufen — auf dem
               <em>gespeicherten</em> Stand. Fehler verhindern den Start und auch das Speichern,
               Warnungen sind die Arbeitsliste.</p>
            <div class="card"><h3>Fehler <span class="count">(${errors.length})</span></h3>
                ${list(errors, 'error')}</div>
            <div class="card"><h3>Warnungen <span class="count">(${warnings.length})</span></h3>
                ${list(warnings, 'warn')}</div>`;
}

/* ============================ helpers ============================ */

/**
 * The name an author would call this thing, whatever the type happens to call the field. The
 * description is the last resort before the id, which is what gives a condition a readable label -
 * it is the only kind with something to say and no name to say it in.
 */
function displayName(entry) {
    if (!entry) return '?';
    const described = entry.description?.trim().split('\n')[0];
    return entry.name || entry.topic || entry.trigger
        || (described && described.length <= 70 ? described : null)
        || entry.id || '?';
}

function conditionLabel(id) {
    const entry = state.byId[id]?.entry;
    if (!entry) return id || '—';
    const description = entry.description?.trim().split('\n')[0];
    return description || (id.startsWith('condition.') ? id.slice('condition.'.length) : id);
}

function idLink(id) {
    if (!id) return '<span class="empty">—</span>';
    const known = state.byId[id];
    if (!known) {
        return `<span class="id broken" title="diese Id ist nirgends definiert">${escapeHtml(id)}</span>`;
    }
    return `<a class="id" href="#/id/${encodeURIComponent(id)}"
               title="${escapeAttribute(displayName(known.entry))}">${escapeHtml(id)}</a>`;
}

function notFound(message) {
    return `<h2 class="view-title">Nicht gefunden</h2><div class="msg error">${escapeHtml(message)}</div>`;
}

function json(value) {
    return escapeAttribute(JSON.stringify(value));
}

function escapeHtml(value) {
    return String(value).replace(/[&<>"']/g, character => ({
        '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;',
    }[character]));
}

/** For a value going into a single-quoted attribute, including inline onclick arguments. */
function escapeAttribute(value) {
    return String(value).replace(/&/g, '&amp;').replace(/'/g, '&#39;').replace(/"/g, '&quot;');
}

boot();
