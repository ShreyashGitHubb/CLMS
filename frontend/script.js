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
let selectedEquipment = null;

function categoryClass(category) { return category.toLowerCase().replace(' ', ''); }
function equipmentIcon(category) { return category === 'Measurement' ? 'M' : category === 'Microcontroller' ? 'U' : category === 'Workshop' ? 'S' : 'R'; }
function statusFor(item) { return item.available === 0 ? 'Unavailable' : item.available <= 3 ? 'Low stock' : 'Available'; }

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
  const titles = { overview: 'Good morning, Aarav', equipment: 'Equipment catalog', requests: 'Requests and returns', maintenance: 'Maintenance queue' };
  $('#pageTitle').textContent = titles[viewName];
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
$('#roleSelect').addEventListener('change', (event) => {
  const staff = event.target.value === 'staff';
  $('#pageTitle').textContent = staff ? 'Good morning, Meera' : 'Good morning, Aarav';
  showToast(staff ? 'Lab assistant view selected' : 'Student view selected');
});
requestForm.addEventListener('submit', (event) => {
  event.preventDefault();
  const dueDate = $('#dueDateInput').value;
  if (!dueDate) { $('#formError').textContent = 'Please choose a return date.'; return; }
  if (new Date(dueDate) <= new Date()) { $('#formError').textContent = 'Return date must be after today.'; return; }
  requestDialog.close();
  $('#activeStat').textContent = '2';
  $('#requestBadge').textContent = '2';
  showToast(`${selectedEquipment.name} request submitted`);
});

renderRecommended();
renderCatalog();
