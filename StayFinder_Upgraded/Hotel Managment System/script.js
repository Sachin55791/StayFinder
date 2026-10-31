
const API_BASE_URL = 'http://localhost:8080';

let hotelsDatabase = [];
let currentAuthRole = 'USER';
let currentUser = JSON.parse(localStorage.getItem('stayfinderUser') || 'null');
let isOtpVerified = true;

const authToken = () => localStorage.getItem('stayfinderToken');

async function api(path, options = {}) {
    const headers = {
        'Content-Type': 'application/json',
        ...(options.headers || {})
    };

    if (authToken()) {
        headers.Authorization = 'Bearer ' + authToken();
    }

    const response = await fetch(API_BASE_URL + path, {
        ...options,
        headers
    });

    const body = await response.json().catch(() => null);

    if (!response.ok) {
        throw new Error(body?.message || `Request failed (${response.status})`);
    }

    return body;
}

function safeText(v) {
    return String(v ?? '').replace(/[&<>"']/g, c => ({
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#39;'
    }[c]));
}

function hotelImage(h) {
    return h.imageUrl || h.img ||
        'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=900';
}

function hotelPrice(h) {
    return Number(h.startingPrice || h.price || 0);
}

// LOAD HOTELS
async function loadHotels(city = '') {
    const grid = document.getElementById('hotels-grid');

    if (grid) {
        grid.innerHTML = '<p class="text-xs text-slate-500">Loading verified hotels...</p>';
    }

    try {
        hotelsDatabase = await api(
            '/api/hotels' + (city ? '?city=' + encodeURIComponent(city) : '')
        );

        renderHotelsGrid(hotelsDatabase);
    } catch (e) {
        if (grid) {
            grid.innerHTML =
                '<p class="text-xs text-rose-600 col-span-4">Cannot load hotels. Please start the Spring Boot backend and MySQL, then refresh.</p>';
        }

        console.error(e);
    }
}

// RENDER HOTELS
function renderHotelsGrid(items = hotelsDatabase) {
    const grid = document.getElementById('hotels-grid');

    if (!grid) return;

    grid.innerHTML = '';

    if (!items || !items.length) {
        grid.innerHTML =
            '<p class="text-xs text-slate-500 col-span-4">No approved hotels found for this search.</p>';
        return;
    }

    items.forEach(h => {
        const card = document.createElement('div');

        card.className =
            'bg-white rounded-2xl overflow-hidden border border-slate-200 shadow-sm flex flex-col justify-between hover:shadow-md transition-all';

        card.innerHTML = `
            <div class="h-44 overflow-hidden relative">
                <img src="${safeText(hotelImage(h))}"
                     class="w-full h-full object-cover"
                     alt="${safeText(h.name)}">

                <span class="absolute top-3 left-3 text-[10px] font-bold px-2.5 py-1 rounded bg-amber-500 text-slate-950">
                    Verified
                </span>
            </div>

            <div class="p-4 space-y-3">
                <h3 class="font-bold text-base text-slate-900">
                    ${safeText(h.name)}
                </h3>

                <p class="text-xs text-slate-500">
                    📍 ${safeText(h.location || h.city)}
                </p>

                <div class="flex justify-between items-center pt-2 border-t border-slate-100">
                    <span class="font-extrabold text-sm text-slate-900">
                        ${hotelPrice(h) ? '₹ ' + hotelPrice(h).toLocaleString() : 'Price on request'}
                        <small class="text-slate-400 font-normal">/ night*</small>
                    </span>

                    <button class="px-3.5 py-1.5 btn-brand-blue text-xs font-bold rounded-xl shadow">
                        View Details
                    </button>
                </div>
            </div>
        `;

        card.querySelector('button').addEventListener('click', () => {
            openHotelDetails(h.id);
        });

        grid.appendChild(card);
    });
}

// USER SESSION
function checkUserSession() {
    const name = currentUser?.name ||
        localStorage.getItem('loggedUser') || '';

    const badge = document.getElementById('user-logged-badge');
    const nameEl = document.getElementById('logged-user-name');
    const dash = document.getElementById('dash-user-name');

    const login = document.getElementById('login-header-btn');
    const signup = document.getElementById('signup-header-btn');
    const logoutBtn = document.getElementById('logout-header-btn');

    if (name) {
        badge?.classList.remove('hidden');
        badge?.classList.add('flex');

        if (nameEl) nameEl.textContent = name;
        if (dash) dash.textContent = name;

        login?.classList.add('hidden');
        signup?.classList.add('hidden');
        logoutBtn?.classList.remove('hidden');
    } else {
        badge?.classList.add('hidden');
        login?.classList.remove('hidden');
        signup?.classList.remove('hidden');
        logoutBtn?.classList.add('hidden');
    }
}

// USER BOOKINGS
async function loadMyBookings() {
    const box = document.getElementById('my-bookings-list');

    if (!box || !authToken()) return;

    try {
        const rows = await api('/api/bookings/mine');

        box.innerHTML = rows.length
            ? rows.map(b => `
                <div class="p-3 bg-slate-50 rounded-2xl border space-y-1">
                    <p class="font-bold text-slate-900">
                        ${safeText(b.location || 'Hotel booking')}
                    </p>

                    <p class="text-slate-500">
                        ${safeText(b.checkIn)} to ${safeText(b.checkOut)}
                    </p>

                    <span class="inline-block px-2 py-0.5 bg-amber-100 text-amber-800 text-[10px] font-bold rounded">
                        ${safeText(b.bookingStatus)} · Payment ${safeText(b.paymentStatus)}
                    </span>
                </div>
            `).join('')
            : '<p class="p-3 bg-slate-50 rounded-2xl border text-slate-500">No bookings yet.</p>';

    } catch (e) {
        box.textContent = 'Could not load bookings: ' + e.message;
    }
}

// REGISTRATION ROLE
function setAuthRole(role) {
    currentAuthRole = role;

    ['USER', 'OWNER'].forEach(r => {
        document.getElementById('reg-role-' + r.toLowerCase())
            ?.classList.toggle('role-btn-active', r === role);
    });

    document.getElementById('owner-fields')
        ?.classList.toggle('hidden', role !== 'OWNER');
}

// REGISTER
async function handleRegisterSubmit(e) {
    e.preventDefault();

    const name = document.getElementById('reg-name-field')?.value.trim();
    const email = document.getElementById('reg-email-field')?.value.trim();
    const phone = document.getElementById('reg-phone-field')?.value.trim();
    const password = document.getElementById('reg-password-field')?.value;

    if (!name || !email || !password) {
        alert('Please enter name, email and password.');
        return;
    }

    try {
        const data = await api('/api/users/signup', {
            method: 'POST',
            body: JSON.stringify({
                name,
                email,
                phone,
                password,
                role: currentAuthRole
            })
        });

        saveSession(data);
        closeRegisterModal();

        alert('Account created successfully.');
        showSection('user-dashboard');

    } catch (err) {
        alert(err.message);
    }
}

// LOGIN
async function handleLoginSubmit(e) {
    e.preventDefault();

    const email = document.getElementById('login-user-field')?.value.trim();
    const password = document.getElementById('login-password-field')?.value;

    if (!email || !password) {
        alert('Enter your email and password.');
        return;
    }

    try {
        const data = await api('/api/users/login', {
            method: 'POST',
            body: JSON.stringify({ email, password })
        });

        saveSession(data);
        closeLoginModal();

        alert('Login successful.');
        showSection('user-dashboard');

    } catch (err) {
        alert(err.message);
    }
}

// SAVE LOGIN SESSION
function saveSession(data) {
    localStorage.setItem('stayfinderToken', data.token);

    currentUser = {
        id: data.id,
        name: data.name,
        email: data.email,
        phone: data.phone,
        role: data.role
    };

    localStorage.setItem('stayfinderUser', JSON.stringify(currentUser));
    localStorage.setItem('loggedUser', data.name);

    checkUserSession();
}

// LOGOUT
function logout() {
    localStorage.removeItem('stayfinderToken');
    localStorage.removeItem('stayfinderUser');
    localStorage.removeItem('loggedUser');

    currentUser = null;
    location.reload();
}

// HOTEL SEARCH
async function triggerSearch() {
    const val =
        document.getElementById('main-search-city')?.value.trim() || '';

    await loadHotels(val);

    document.getElementById('section-hotels-wrapper')
        ?.scrollIntoView({ behavior: 'smooth' });
}

// AI CHAT
async function sendAiMessage() {
    const input = document.getElementById('ai-input');
    const prompt = input?.value.trim();

    if (!prompt) return;

    const chat = document.getElementById('ai-chat-body');

    chat.innerHTML += `
        <div class="bg-blue-50 p-2.5 rounded-2xl text-blue-900 font-semibold text-right">
            ${safeText(prompt)}
        </div>
    `;

    input.value = '';

    try {
        const city =
            (prompt.match(/\b(?:in|at|for)\s+([a-z][a-z ]*?)(?=\s+(?:under|below|within|budget|with)|$)/i) || [])[1]?.trim() || '';

        const budgetMatch =
            prompt.match(/(?:under|below|budget(?: of)?|₹)\s*([\d,]+)/i);

        const payload = {
            prompt,
            city,
            maxBudget: budgetMatch
                ? Number(budgetMatch[1].replace(/,/g, ''))
                : 10000
        };

        const data = await api('/api/ai/recommend', {
            method: 'POST',
            body: JSON.stringify(payload)
        });

        chat.innerHTML += `
            <div class="bg-slate-100 p-3 rounded-2xl text-slate-800">
                ${safeText(data.message)}
            </div>
        `;

        if (data.hotels?.length) {
            renderHotelsGrid(data.hotels);
            showSection('hotels');
        }

    } catch (err) {
        chat.innerHTML += `
            <div class="bg-rose-50 p-3 rounded-2xl text-rose-700">
                AI service unavailable: ${safeText(err.message)}
            </div>
        `;
    }
}

// HOTEL DETAILS
async function openHotelDetails(id) {
    try {
        const h = await api('/api/hotels/' + id);

        const container =
            document.getElementById('modal-details-container');

        if (!container) return;

        container.innerHTML = `
            <div class="space-y-4">

                <div class="h-48 rounded-2xl overflow-hidden">
                    <img src="${safeText(hotelImage(h))}"
                         class="w-full h-full object-cover">
                </div>

                <h2 class="text-2xl font-bold">
                    ${safeText(h.name)}
                </h2>

                <p class="text-xs text-slate-500">
                    ${safeText(h.location || h.city)}
                </p>

                <p class="text-sm">
                    Facilities: ${safeText(h.facilities || 'Please contact property for details')}
                </p>

                <p class="text-xs text-slate-500">
                    *Room price and availability must be confirmed by the property.
                </p>

                <div class="grid grid-cols-2 gap-2">
                    <input id="booking-checkin" type="date"
                           class="border p-2 rounded-lg">

                    <input id="booking-checkout" type="date"
                           class="border p-2 rounded-lg">
                </div>

                <input id="booking-guest-name"
                       placeholder="Guest name"
                       class="border p-2 rounded-lg w-full"
                       value="${safeText(currentUser?.name || '')}">

                <input id="booking-guest-phone"
                       placeholder="Phone"
                       class="border p-2 rounded-lg w-full"
                       value="${safeText(currentUser?.phone || '')}">

                <button id="booking-submit"
                        class="w-full py-3 bg-emerald-600 text-white font-bold rounded-xl">
                    Request Booking
                </button>

            </div>
        `;

        container.querySelector('#booking-submit')
            .addEventListener('click', () => createBooking(h));

        document.getElementById('hotel-details-modal')
            .classList.add('modal-active');

    } catch (e) {
        alert(e.message);
    }
}

// CREATE BOOKING
async function createBooking(h) {
    if (!authToken()) {
        alert('Please login before booking.');
        closeDetailsModal();
        openLoginModal();
        return;
    }

    const checkIn =
        document.getElementById('booking-checkin').value;

    const checkOut =
        document.getElementById('booking-checkout').value;

    if (!checkIn || !checkOut) {
        alert('Select check-in and check-out dates.');
        return;
    }

    if (checkOut <= checkIn) {
        alert('Check-out date must be after check-in date.');
        return;
    }

    try {
        const result = await api('/api/bookings', {
            method: 'POST',
            body: JSON.stringify({
                hotelId: h.id,
                guestName: document.getElementById('booking-guest-name').value,
                guestPhone: document.getElementById('booking-guest-phone').value,
                guestEmail: currentUser?.email,
                location: h.location || h.city,
                roomNumber: 'To be assigned',
                roomType: 'Standard',
                checkIn,
                checkOut,
                totalPrice: 0
            })
        });

        alert('Booking request submitted. Status: ' + result.bookingStatus);

        closeDetailsModal();
        loadMyBookings();

    } catch (e) {
        alert(e.message);
    }
}

// OWNER: LOAD HOTEL LIST
async function loadOwnerHotels() {
    const box = document.getElementById('owner-hotels-list');

    if (!box) return;

    try {
        const rows = await api('/api/hotels/owner/mine');

        box.innerHTML = rows.length
            ? rows.map(h => `
                <div class="border rounded-xl p-3 flex justify-between">
                    <span>
                        <strong>${safeText(h.name)}</strong>
                        · ${safeText(h.city)}
                    </span>

                    <span class="font-bold">
                        ${safeText(h.status)}
                    </span>
                </div>
            `).join('')
            : 'No properties submitted yet.';

    } catch (e) {
        box.textContent = e.message;
    }
}

// OWNER: ADD HOTEL
async function createOwnerHotel(e) {
    e.preventDefault();

    const payload = {
        name: document.getElementById('owner-hotel-name').value.trim(),
        city: document.getElementById('owner-hotel-city').value.trim(),
        location: document.getElementById('owner-hotel-location').value.trim(),
        startingPrice: Number(document.getElementById('owner-hotel-price').value),
        imageUrl: document.getElementById('owner-hotel-image').value.trim(),
        facilities: document.getElementById('owner-hotel-facilities').value.trim()
    };

    try {
        await api('/api/hotels/owner', {
            method: 'POST',
            body: JSON.stringify(payload)
        });

        e.target.reset();

        alert('Property submitted for admin approval.');
        loadOwnerHotels();

    } catch (err) {
        alert(err.message);
    }
}

// OWNER: LOAD PENDING BOOKING REQUESTS
async function loadOwnerBookingRequests() {
    const box = document.getElementById('owner-booking-requests');

    if (!box || !authToken()) return;

    try {
        const bookings = await api('/api/bookings/owner/pending');

        if (!bookings || bookings.length === 0) {
            box.innerHTML = '<p>No pending booking requests.</p>';
            return;
        }

        box.innerHTML = bookings.map(b => `
            <div class="booking-card">
                <h3 class="font-bold text-base">
                    ${safeText(b.guestName)}
                </h3>

                <p>Hotel ID: ${safeText(b.hotelId)}</p>
                <p>Room: ${safeText(b.roomType)}</p>
                <p>Check-in: ${safeText(b.checkIn)}</p>
                <p>Check-out: ${safeText(b.checkOut)}</p>
                <p>Phone: ${safeText(b.guestPhone)}</p>
                <p>Status: ${safeText(b.bookingStatus)}</p>

                <div class="flex gap-2 mt-3">
                    <button
                        onclick="decideBooking(${Number(b.id)}, 'APPROVED')"
                        class="bg-emerald-600 text-white rounded-lg px-4 py-2 font-bold">
                        Accept
                    </button>

                    <button
                        onclick="decideBooking(${Number(b.id)}, 'REJECTED')"
                        class="bg-rose-600 text-white rounded-lg px-4 py-2 font-bold">
                        Reject
                    </button>
                </div>
            </div>
        `).join('');

    } catch (e) {
        box.innerHTML = `<p class="text-rose-600">${safeText(e.message)}</p>`;
    }
}

// OWNER: ACCEPT OR REJECT BOOKING
async function decideBooking(id, status) {
    if (!confirm('Are you sure you want to ' + status.toLowerCase() + ' this booking?')) {
        return;
    }

    try {
        await api(
            `/api/bookings/owner/${id}/decision?status=${status}`,
            { method: 'PUT' }
        );

        alert('Booking ' + status.toLowerCase() + ' successfully.');

        await loadOwnerBookingRequests();

    } catch (e) {
        alert(e.message);
    }
}

// ADMIN: LOAD PENDING HOTELS
async function loadAdminPending() {
    const box = document.getElementById('admin-pending-list');

    if (!box) return;

    try {
        const rows = await api('/api/hotels/admin/pending');

        box.innerHTML = rows.length
            ? rows.map(h => `
                <div class="border rounded-xl p-3 flex flex-wrap justify-between gap-2 items-center">

                    <span>
                        <strong>${safeText(h.name)}</strong>
                        · ${safeText(h.city)}
                        · ₹${Number(h.startingPrice).toLocaleString()}
                    </span>

                    <span class="flex gap-2">
                        <button
                            onclick="decideHotel(${Number(h.id)}, 'APPROVED')"
                            class="bg-emerald-600 text-white rounded-lg px-3 py-2">
                            Approve
                        </button>

                        <button
                            onclick="decideHotel(${Number(h.id)}, 'REJECTED')"
                            class="bg-rose-600 text-white rounded-lg px-3 py-2">
                            Reject
                        </button>
                    </span>

                </div>
            `).join('')
            : 'No pending hotel approvals.';

    } catch (e) {
        box.textContent = e.message;
    }
}

// ADMIN: DECIDE HOTEL
async function decideHotel(id, status) {
    try {
        await api(
            `/api/hotels/admin/${id}/decision?status=${status}`,
            { method: 'PUT' }
        );

        await loadAdminPending();

        alert('Hotel status updated: ' + status);

    } catch (e) {
        alert(e.message);
    }
}

// NAVIGATION
function showSection(sectionName) {
    const sections = [
        'home',
        'hotels',
        'user-dashboard',
        'about',
        'contact'
    ];

    sections.forEach(name => {
        const el = document.getElementById(`section-${name}-wrapper`);
        if (el) el.classList.add('hidden');
    });

    const target =
        document.getElementById(`section-${sectionName}-wrapper`);

    if (target) target.classList.remove('hidden');

    if (sectionName === 'home') {
        document.getElementById('section-hotels-wrapper')
            ?.classList.remove('hidden');
    }

    const navMap = {
        home: 'nav-home',
        hotels: 'nav-hotels',
        'user-dashboard': 'nav-dash',
        about: 'nav-about',
        contact: 'nav-contact'
    };

    Object.values(navMap).forEach(id => {
        const el = document.getElementById(id);
        if (el) {
            el.className = 'hover:text-slate-900';
        }
    });

    const active = document.getElementById(navMap[sectionName]);

    if (active) {
        active.className =
            'text-slate-900 border-b-2 border-amber-500 pb-1 font-bold';
    }

    if (sectionName === 'user-dashboard' && !currentUser) {
        openLoginModal();
        return;
    }

    if (sectionName === 'user-dashboard') {
        loadMyBookings();

        document.getElementById('owner-dashboard-panel')
            ?.classList.toggle('hidden', currentUser?.role !== 'OWNER');

        document.getElementById('admin-dashboard-panel')
            ?.classList.toggle('hidden', currentUser?.role !== 'ADMIN');

        if (currentUser?.role === 'OWNER') {
            loadOwnerHotels();
            loadOwnerBookingRequests();
        }

        if (currentUser?.role === 'ADMIN') {
            loadAdminPending();
        }
    }
}

// LOGIN MODAL
function openLoginModal() {
    document.getElementById('login-modal')
        ?.classList.add('modal-active');
}

function closeLoginModal() {
    document.getElementById('login-modal')
        ?.classList.remove('modal-active');
}

// REGISTER MODAL
function openRegisterModal() {
    document.getElementById('register-modal')
        ?.classList.add('modal-active');
}

function closeRegisterModal() {
    document.getElementById('register-modal')
        ?.classList.remove('modal-active');
}

function switchToRegister() {
    closeLoginModal();
    openRegisterModal();
}

function switchToLogin() {
    closeRegisterModal();
    openLoginModal();
}

// HOTEL DETAILS MODAL
function closeDetailsModal() {
    document.getElementById('hotel-details-modal')
        ?.classList.remove('modal-active');
}

// LOCATION MODAL
function openLocationModal() {
    document.getElementById('location-modal')
        ?.classList.add('modal-active');
}

function closeLocationModal() {
    document.getElementById('location-modal')
        ?.classList.remove('modal-active');
}

function searchModalAddress() {
    const value =
        document.getElementById('modal-address-input')?.value || '';

    const main = document.getElementById('main-search-city');

    if (main) main.value = value;

    const badge =
        document.getElementById('selected-address-badge');

    if (badge && value) badge.textContent = value;

    const map = document.getElementById('google-map-embed');

    if (map && value) {
        map.src =
            'https://maps.google.com/maps?q=' +
            encodeURIComponent(value) +
            '&t=&z=13&ie=UTF8&iwloc=&output=embed';
    }
}

function confirmMapLocation() {
    searchModalAddress();
    closeLocationModal();
    triggerSearch();
}

function detectUserGPS() {
    alert('GPS location lookup is not configured in this version. Please enter a city manually.');
}

function openHotelDetailsFallback(id) {
    openHotelDetails(id);
}

// INITIAL PAGE LOAD
document.addEventListener('DOMContentLoaded', () => {
    checkUserSession();
    loadHotels();
});
