'use strict';

const API = '/api';
const KEYS = ['accessToken', 'refreshToken', 'username', 'role'];
const $ = s => document.querySelector(s);
const esc = s => String(s).replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
const saveSession = d => KEYS.forEach(k => localStorage.setItem(k, d[k]));
const clearSession = () => KEYS.forEach(k => localStorage.removeItem(k));
const role = () => localStorage.getItem('role');

let gliderCache = [], flightCache = [], hoursChart, refreshing = null, msgTimer;

/*ui stvari*/
function notify(text, isErr = false) {
  const el = $('#msg');
  el.textContent = text;
  el.className = isErr ? 'err' : 'ok';
  clearTimeout(msgTimer);
  msgTimer = setTimeout(() => { el.textContent = ''; el.className = ''; }, 4000);
}

function showTab(tab) {
  $('#loginForm').hidden = tab !== 'login';
  $('#registerForm').hidden = tab !== 'register';
  document.querySelectorAll('.tabs button').forEach(b => b.classList.toggle('active', b.dataset.tab === tab));
}

function expire() { clearSession(); render(); }

async function render() {
  const logged = !!localStorage.getItem('accessToken');
  $('#auth').hidden = logged;
  $('#dash').hidden = !logged;
  if (!logged) return;

  $('#who').textContent = `${localStorage.getItem('username')} (${role()})`;
  $('#admin-section').style.display = role() === 'ADMIN' ? 'block' : 'none';

  try {
    await loadGliders();
    await loadFlights();
  } catch (e) { notify(e.message, true); }
}

/*Auth pozivi*/
async function authCall(path, payload) {
  const res = await fetch(`${API}/auth/${path}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  });
  const body = await res.json().catch(() => ({}));
  if (!res.ok) throw new Error(body.message || `HTTP ${res.status}`);
  return body;
}

/* One in-flight refresh shared by all concurrent 401s (refresh tokens are single-use). */
function refreshTokens() {
  refreshing ??= authCall('refresh', { refreshToken: localStorage.getItem('refreshToken') })
    .then(saveSession)
    .finally(() => { refreshing = null; });
  return refreshing;
}

/* presretanje */
async function api(path, opts = {}, retried = false) {
  const headers = { 'Content-Type': 'application/json' };
  const token = localStorage.getItem('accessToken');
  if (token) headers.Authorization = `Bearer ${token}`;

  const res = await fetch(API + path, { ...opts, headers });

  if (res.status === 401) {
    if (!retried && localStorage.getItem('refreshToken')) {
      try { await refreshTokens(); }
      catch { expire(); throw new Error('Session expired, please log in again'); }
      return api(path, opts, true);
    }
    expire();
    throw new Error('Session expired, please log in again');
  }
  if (!res.ok) {
    const body = await res.json().catch(() => ({}));
    throw new Error(body.message || (res.status === 403 ? 'Forbidden: insufficient role' : `HTTP ${res.status}`));
  }
  return res.status === 204 ? null : res.json();
}

/* jedrilice - dropdown, chart i admin tabela*/
async function loadGliders() {
  gliderCache = await api('/gliders');

  $('#fGlider').innerHTML = '<option value="">Unesi jedrilicu…</option>' +
    gliderCache.map(g => `<option value="${g.id}">${esc(g.registrationMarks)} — ${esc(g.model)}</option>`).join('');

  if (role() === 'ADMIN') {
    $('#tbody').innerHTML = gliderCache.map(g => `
      <tr>
        <td>${g.id}</td>
        <td>${esc(g.registrationMarks)}</td>
        <td>${esc(g.model)}</td>
        <td>${g.year}</td>
        <td><span class="badge ${esc(g.status)}">${g.status === 'ACTIVE' ? 'AKTIVNA' : g.status === 'MAINTENANCE' ? 'ODRŽAVANJE' : esc(g.status)}</span></td>
        <td><button data-edit="${g.id}">Edit</button> <button class="danger" data-del="${g.id}">Delete</button></td>
      </tr>`).join('') || '<tr><td colspan="6">Nema registrovanih jedrilica</td></tr>';
  }
}

function resetGliderForm() {
  $('#gForm').reset();
  $('#gId').value = '';
  $('#formTitle').textContent = 'Add glider';
}

$('#tbody').addEventListener('click', async e => {
  const { edit, del } = e.target.dataset;
  if (edit) {
    const g = gliderCache.find(x => x.id == edit);
    if (!g) return;
    $('#gId').value = g.id;
    $('#gReg').value = g.registrationMarks;
    $('#gModel').value = g.model;
    $('#gYear').value = g.year;
    $('#gStatus').value = g.status;
    $('#formTitle').textContent = `Edit glider #${g.id}`;
    $('#gForm').scrollIntoView({ behavior: 'smooth' });
  }
  if (del && confirm('Delete this glider?')) {
    try { await api(`/gliders/${del}`, { method: 'DELETE' }); notify('Glider deleted'); await loadGliders(); renderChart(); }
    catch (err) { notify(err.message, true); }
  }
});

$('#gForm').addEventListener('submit', async e => {
  e.preventDefault();
  const id = $('#gId').value;
  const body = JSON.stringify({
    registrationMarks: $('#gReg').value.trim(),
    model: $('#gModel').value.trim(),
    year: Number($('#gYear').value),
    status: $('#gStatus').value
  });
  try {
    await api(id ? `/gliders/${id}` : '/gliders', { method: id ? 'PUT' : 'POST', body });
    resetGliderForm(); notify('Glider saved'); await loadGliders();
  } catch (err) { notify(err.message, true); }
});
$('#gCancel').addEventListener('click', resetGliderForm);

/* letovi logovanje, tabela i chart*/
async function loadFlights() {
  const allFlights = await api('/flights');
  const userFlights = allFlights.filter(f => f.username === localStorage.getItem('username'));

  if (role() === 'ADMIN') {
    flightCache = allFlights;
  } else {
    flightCache = userFlights;
  }

  $('#flightTbody').innerHTML = flightCache.map(f => `
    <tr>
      <td>${esc(f.date)}</td>
      <td>${esc(f.registrationMarks)}</td>
      <td>${f.durationMinutes} min</td>
      <td><span class="badge ${esc(f.launchType)}">${f.launchType === 'WINCH' ? 'VITLO' : f.launchType === 'TOW' ? 'AEROZAPREGA' : esc(f.launchType)}</span></td>
      <td>${esc(f.username)}</td>
    </tr>`).join('') || '<tr><td colspan="5">Nema registrovanih letova</td></tr>';
  renderChart();
}

function renderChart() {
  const minutesByReg = {};
  flightCache.forEach(f => { minutesByReg[f.registrationMarks] = (minutesByReg[f.registrationMarks] || 0) + f.durationMinutes; });
  const labels = Object.keys(minutesByReg);
  const hours = labels.map(l => +(minutesByReg[l] / 60).toFixed(2));

  if (hoursChart) hoursChart.destroy();
  hoursChart = new Chart($('#hoursChart'), {
    type: 'bar',
    data: { labels, datasets: [{ label: 'Ukupno sati', data: hours, backgroundColor: '#1e6fd9' }] },
    options: { responsive: true, maintainAspectRatio: false, scales: { y: { beginAtZero: true, title: { display: true, text: 'Sati naleta' } } }, plugins: { legend: { display: false } } }
  });
}

$('#fForm').addEventListener('submit', async e => {
  e.preventDefault();
  const body = JSON.stringify({
    date: $('#fDate').value,
    durationMinutes: Number($('#fDuration').value),
    launchType: $('#fLaunch').value,
    gliderId: Number($('#fGlider').value)
  });
  try {
    await api('/flights', { method: 'POST', body });
    e.target.reset(); notify('Flight logged'); await loadFlights();
  } catch (err) { notify(err.message, true); }
});

/*Auth forme?*/
document.querySelectorAll('.tabs button').forEach(b => b.addEventListener('click', () => showTab(b.dataset.tab)));

$('#loginForm').addEventListener('submit', async e => {
  e.preventDefault();
  try {
    saveSession(await authCall('login', { username: $('#lUser').value.trim(), password: $('#lPass').value }));
    e.target.reset(); render();
  } catch (err) { notify(err.message, true); }
});

$('#registerForm').addEventListener('submit', async e => {
  e.preventDefault();
  try {
    await authCall('register', {
      username: $('#rUser').value.trim(), email: $('#rEmail').value.trim(),
      password: $('#rPass').value, role: $('#rRole').value
    });
    e.target.reset(); showTab('login'); notify('Registered – please log in');
  } catch (err) { notify(err.message, true); }
});

$('#logout').addEventListener('click', async () => {
  try { await api('/auth/logout', { method: 'POST' }); } catch { /* clear locally regardless */ }
  clearSession(); render();
});

render();