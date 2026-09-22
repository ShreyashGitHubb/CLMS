const equipment = [
  { id: 1, name: 'Digital Multimeter', category: 'Measurement', location: 'Electronics Lab', available: 14, total: 18, description: 'Handheld digital multimeter for electronics experiments.' },
  { id: 2, name: 'Arduino Uno Kit', category: 'Microcontroller', location: 'Embedded Systems Lab', available: 3, total: 12, description: 'Arduino board with sensors, jumper wires, and USB cable.' },
  { id: 3, name: 'Oscilloscope', category: 'Measurement', location: 'Electronics Lab', available: 6, total: 6, description: 'Two-channel digital oscilloscope for signal analysis.' },
  { id: 4, name: 'Soldering Station', category: 'Workshop', location: 'Workshop A', available: 0, total: 8, description: 'Temperature-controlled soldering station.' },
  { id: 5, name: 'Raspberry Pi 4 Kit', category: 'Computer', location: 'Computer Lab 2', available: 7, total: 10, description: 'Raspberry Pi board with power supply and case.' }
];

const $ = (selector) => document.querySelector(selector);
const views = document.querySelectorAll('.view');
const navItems = document.querySelectorAll('.nav-item');
const requestDialog = $('#requestDialog');
const requestForm = $('#requestForm');
const API_BASE = 'http://localhost:8080/api';
const loginForm = $('#loginForm');
const loginScreen = $('#loginScreen');
const appShell = $('#appShell');
let selectedEquipment = null;
let currentRole = 'student';
let transactions = [];
let session = JSON.parse(localStorage.getItem('clmsSession') || 'null');

function categoryClass(category) { return category.toLowerCase().replace(' ', ''); }
function equipmentIcon(category) { return category === 'Measurement' ? 'M' : category === 'Microcontroller' ? 'U' : category === 'Workshop' ? 'S' : 'R'; }
function statusFor(item) { return item.available === 0 ? 'Unavailable' : item.available <= 3 ? 'Low stock' : 'Available'; }
function equipmentName(id) { return equipment.find((item) => item.id === id)?.name || `Equipment #${id}`; }
function statusPill(status) { const normalized = status.toLowerCase(); return `<span class="pill ${normalized}">${status}</span>`; }
function authHeaders() { return session ? { Authorization: `Bearer ${session.token}` } : {}; }

function equipmentRow(item) {
  return `<div class="equipment-row"><div class="equipment-icon ${categoryClass(item.category)}">${equipmentIcon(item.category)}</div><div class="equipment-info"><strong>${item.name}</strong><span>${item.location}</span></div><span class="availability ${item.available <= 3 ? 'low' : ''}">${item.available}/${item.total} available</span></div>`;
}

function catalogCard(item) {
  const disabled = item.available === 0 ? 'disabled' : '';
  return `<article class="catalog-card"><div class="equipment-icon ${categoryClass(item.category)}">${equipmentIcon(item.category)}</div><p class="section-kicker">${item.category}</p><h3>${item.name}</h3><p>${item.description}</p><div class="card-meta"><div><small>Location</small><strong>${item.location}</strong></div><div><small>Status</small><strong>${statusFor(item)}</strong></div></div><button class="request-button" data-equipment-id="${item.id}" ${disabled}>${item.available === 0 ? 'In maintenance' : 'Request item'}</button></article>`;
}

function renderRecommended() { $('#recommendedList').innerHTML = equipment.filter((item) => item.available > 0).slice(0, 3).map(equipmentRow).join(''); }
function renderCatalog() {
  const search = $('#searchInput').value.toLowerCase();
  const category = $('#categoryFilter').value;
  const filtered = equipment.filter((item) => item.name.toLowerCase().includes(search) && (category === 'all' || item.category === category));
  $('#catalogGrid').innerHTML = filtered.length ? filtered.map(catalogCard).join('') : '<p class="muted">No equipment matches this search.</p>';
  document.querySelectorAll('.request-button').forEach((button) => button.addEventListener('click', () => openRequest(Number(button.dataset.equipmentId))));
}

function switchView(viewName) {
  views.forEach((view) => view.classList.toggle('active', view.id === `${viewName}View`));
  navItems.forEach((item) => item.classList.toggle('active', item.dataset.view === viewName));
  const firstName = session?.user?.name?.split(' ')[0] || '';
  const titles = { overview: `Good morning, ${firstName}`, equipment: 'Equipment catalog', requests: currentRole === 'staff' ? 'Review requests' : 'Requests and returns', maintenance: 'Maintenance queue' };
  $('#pageTitle').textContent = titles[viewName];
}

function applyRoleDashboard() {
  const staff = currentRole === 'staff';
  $('#overviewHeading').textContent = staff ? 'Keep every lab request moving.' : 'Keep practical work moving.';
  $('#overviewDescription').textContent = staff
    ? 'Review pending requests, protect inventory accuracy, and keep equipment ready for the next practical.'
    : 'Find what you need, request it in a few seconds, and keep every return accountable.';
  $('#overviewAction').innerHTML = staff ? 'Review requests <span>-></span>' : 'Browse equipment <span>-></span>';
  $('#overviewAction').dataset.viewTarget = staff ? 'requests' : 'equipment';
  $('#activeLabel').textContent = staff ? 'Pending approvals' : 'My active requests';
  $('#activeHint').textContent = staff ? 'Needs your review' : 'Across your borrowing history';
  $('#dueLabel').textContent = staff ? 'Items in circulation' : 'Due this week';
  $('#dueHint').textContent = staff ? 'Currently issued to users' : 'Return by Sep 28';
  $('#activityKicker').textContent = staff ? 'Operations log' : 'Your log';
  $('#activityHeading').textContent = staff ? 'Team activity' : 'Recent activity';
}

function renderRequests() {
  const body = $('#requestsBody');
  if (!transactions.length) {
    body.innerHTML = '<tr><td colspan="6" class="empty-cell">No borrowing records found.</td></tr>';
    return;
  }
  body.innerHTML = transactions.map((transaction) => {
    const action = currentRole === 'staff' && transaction.status === 'PENDING'
      ? `<button class="table-action" data-action="approve" data-id="${transaction.id}">Approve</button>`
      : currentRole === 'student' && transaction.status === 'APPROVED'
        ? `<button class="table-action" data-action="return" data-id="${transaction.id}">Return</button>` : '-';
    return `<tr><td><strong>${equipmentName(transaction.equipmentId)}</strong><small>Transaction #${transaction.id}</small></td><td>${transaction.issueDate || 'Pending approval'}</td><td>${transaction.dueDate}</td><td>${statusPill(transaction.status)}</td><td>${Number(transaction.fine) ? `Rs ${transaction.fine}` : '-'}</td><td>${action}</td></tr>`;
  }).join('');
  body.querySelectorAll('.table-action').forEach((button) => button.addEventListener('click', () => updateTransaction(button.dataset.action, button.dataset.id)));
}

async function loadRequests() {
  try {
    const query = currentRole === 'staff' ? '' : '?userId=1';
    const response = await fetch(`${API_BASE}/requests${query}`, { headers: authHeaders() });
    if (!response.ok) throw new Error('Request data could not be loaded.');
    transactions = await response.json();
    $('#requestBadge').textContent = transactions.filter((item) => item.status === 'PENDING').length;
    $('#activeStat').textContent = transactions.filter((item) => ['PENDING', 'APPROVED', 'OVERDUE'].includes(item.status)).length;
    renderRequests();
  } catch (error) {
    $('#requestsBody').innerHTML = '<tr><td colspan="6" class="empty-cell">Start the Java API to load live requests.</td></tr>';
  }
}

async function updateTransaction(action, id) {
  try {
    const response = await fetch(`${API_BASE}/requests/${id}/${action}`, { method: 'POST', headers: authHeaders() });
    const result = await response.json();
    if (!response.ok) throw new Error(result.error || 'Request update failed.');
    showToast(action === 'approve' ? 'Request approved and inventory updated' : 'Equipment returned successfully');
    await loadEquipment();
    await loadRequests();
  } catch (error) {
    showToast(error.message);
  }
}

function openRequest(id) {
  selectedEquipment = equipment.find((item) => item.id === id);
  $('#dialogEquipmentInput').value = selectedEquipment.name;
  $('#dialogEquipmentName').textContent = `${selectedEquipment.available} units available at ${selectedEquipment.location}.`;
  $('#formError').textContent = '';
  const minimumDate = new Date();
  minimumDate.setDate(minimumDate.getDate() + 1);
  $('#dueDateInput').min = minimumDate.toISOString().split('T')[0];
  requestDialog.showModal();
}

function showToast(message) {
  const toast = $('#toast');
  toast.textContent = message;
  toast.classList.add('show');
  setTimeout(() => toast.classList.remove('show'), 2800);
}

navItems.forEach((item) => item.addEventListener('click', () => switchView(item.dataset.view)));
document.querySelectorAll('[data-view-target]').forEach((button) => button.addEventListener('click', () => switchView(button.dataset.viewTarget)));
$('#searchInput').addEventListener('input', renderCatalog);
$('#categoryFilter').addEventListener('change', renderCatalog);
requestForm.addEventListener('submit', (event) => {
  event.preventDefault();
  const dueDate = $('#dueDateInput').value;
  if (!dueDate) { $('#formError').textContent = 'Please choose a return date.'; return; }
  if (new Date(dueDate) <= new Date()) { $('#formError').textContent = 'Return date must be after today.'; return; }
  submitRequest(dueDate);
});

async function submitRequest(dueDate) {
  try {
    const response = await fetch(`${API_BASE}/requests`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', ...authHeaders() },
      body: JSON.stringify({ equipmentId: selectedEquipment.id, dueDate })
    });
    const result = await response.json();
    if (!response.ok) throw new Error(result.error || 'Request could not be submitted.');
    requestDialog.close();
    $('#activeStat').textContent = '2';
    $('#requestBadge').textContent = '2';
    showToast(`${selectedEquipment.name} request submitted`);
    loadRequests();
  } catch (error) {
    $('#formError').textContent = error.message.includes('Failed to fetch')
      ? 'API is offline. Start the Java API server and try again.'
      : error.message;
  }
}

async function loadEquipment() {
  try {
    const response = await fetch(`${API_BASE}/equipment`);
    if (!response.ok) throw new Error('Equipment API unavailable');
    const liveEquipment = await response.json();
    equipment.splice(0, equipment.length, ...liveEquipment);
    renderRecommended();
    renderCatalog();
    showToast('Live inventory connected');
    loadRequests();
  } catch (error) {
    renderRecommended();
    renderCatalog();
  }
}

function applySession() {
  if (!session) return;
  const staff = session.user.role !== 'STUDENT';
  currentRole = staff ? 'staff' : 'student';
  loginScreen.hidden = true;
  appShell.hidden = false;
  $('#pageTitle').textContent = staff ? `Good morning, ${session.user.name.split(' ')[0]}` : `Good morning, ${session.user.name.split(' ')[0]}`;
  $('#requestsHeading').textContent = staff ? 'Review and manage requests.' : 'Requests and returns.';
  applyRoleDashboard();
  $('#userName').textContent = session.user.name;
  $('#userRole').textContent = session.user.role.replace('_', ' ').toLowerCase();
  $('#logoutButton').textContent = session.user.name.split(' ').map((part) => part[0]).join('').slice(0, 2);
  loadEquipment();
  loadRequests();
}

loginForm.addEventListener('submit', async (event) => {
  event.preventDefault();
  $('#loginError').textContent = '';
  try {
    const response = await fetch(`${API_BASE}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email: $('#loginEmail').value, password: $('#loginPassword').value })
    });
    const result = await response.json();
    if (!response.ok) throw new Error(result.error || 'Sign in failed.');
    session = result;
    localStorage.setItem('clmsSession', JSON.stringify(session));
    applySession();
    showToast(`Signed in as ${session.user.role.replace('_', ' ').toLowerCase()}`);
  } catch (error) {
    $('#loginError').textContent = error.message.includes('Failed to fetch')
      ? 'The Java API is offline. Start the API and try again.' : error.message;
  }
});

$('#logoutButton').addEventListener('click', () => {
  session = null;
  localStorage.removeItem('clmsSession');
  appShell.hidden = true;
  loginScreen.hidden = false;
  loginForm.reset();
});

if (session) applySession();
