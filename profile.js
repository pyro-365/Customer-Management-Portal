Layout.mount('profile').then((content) => {
  let geo = null;   // verified map position of the address in the form
  let leafletMap = null;
  let leafletMarker = null;

  const pinSvg = `
    <svg width="34" height="46" viewBox="0 0 34 46" fill="none" xmlns="http://www.w3.org/2000/svg" style="filter:drop-shadow(0 4px 6px rgba(0,0,0,0.35));cursor:grab;">
      <path d="M17 0C7.61 0 0 7.61 0 17C0 29.75 17 46 17 46C17 46 34 29.75 34 17C34 7.61 26.39 0 17 0Z" fill="#70C028"/>
      <circle cx="17" cy="16" r="6.5" fill="white"/>
    </svg>`;

  function render() {
    geo = ME.latitude != null ? { lat: ME.latitude, lng: ME.longitude } : null;
    leafletMap = null;
    leafletMarker = null;

    content.innerHTML = `
      <h1>My profile</h1>
      <p class="lead">Customer ID ${ME.id}</p>

      <section class="card">
        <h2>My information</h2>
        <dl class="info">
          <dt>Name</dt><dd>${esc(ME.name)}</dd>
          <dt>Email</dt><dd>${esc(ME.email)}</dd>
          <dt>Phone</dt><dd>${esc(ME.phone)}</dd>
          <dt>Address</dt><dd>${ME.address ? esc(ME.address) : '<span class="muted">Not added yet</span>'}</dd>
          <dt>Coordinates</dt><dd>${geo ? `<span class="map-coords">${geo.lat.toFixed(5)}, ${geo.lng.toFixed(5)}</span>` : '<span class="muted">Not pinned</span>'}</dd>
        </dl>
      </section>

      <section class="card" id="update">
        <h2>Update profile</h2>
        <form id="update-form" novalidate>
          <div class="two">
            <div><label for="u-name">Name</label><input id="u-name" value="${esc(ME.name)}"></div>
            <div><label for="u-phone">Phone</label><input id="u-phone" value="${esc(ME.phone)}"></div>
          </div>
          <label for="u-email">Email</label><input id="u-email" type="email" value="${esc(ME.email)}">
          <label for="u-address">Delivery address</label>
          <div class="row-inline">
            <input id="u-address" value="${esc(ME.address || '')}" placeholder="House number, street, area, city, PIN">
            <button class="btn ghost" id="verify" type="button">Verify address</button>
          </div>
          <p class="hint" id="geo-note">${geo ? '📍 Drag the green pin or click on the map to pinpoint your exact gate/doorstep.' : 'Verify the address to view the map and adjust your pin.'}</p>
          
          <div class="map-wrap" id="map-wrap" style="${geo ? '' : 'display:none;'}">
            <div id="profile-map" class="interactive-map"></div>
            <div class="map-toolbar">
              <span>📍 Drag the green pin or click on the map to adjust doorstep.</span>
              <span class="map-coords" id="coords-text">${geo ? geo.lat.toFixed(5) + ', ' + geo.lng.toFixed(5) : ''}</span>
            </div>
          </div>

          <p class="form-error" id="u-err" role="alert"></p>
          <button class="btn" type="submit" style="margin-top:1.2rem">Save changes</button>
        </form>
      </section>

      <section class="card" id="password">
        <h2>Change password</h2>
        <form id="pw-form" novalidate>
          <label for="p-cur">Current password</label><input id="p-cur" type="password" autocomplete="current-password">
          <label for="p-new">New password</label><input id="p-new" type="password" autocomplete="new-password">
          <label for="p-conf">Confirm new password</label><input id="p-conf" type="password" autocomplete="new-password">
          <p class="form-error" id="p-err" role="alert"></p>
          <button class="btn" type="submit">Change password</button>
        </form>
      </section>

      <section class="card" id="danger">
        <h2>Delete account</h2>
        <p class="muted">This removes your account and your orders. It cannot be undone.</p>
        <button class="btn danger" id="delete" type="button" style="width:auto">Delete my account</button>
      </section>`;
    bind();
    if (location.hash) document.querySelector(location.hash)?.scrollIntoView();
  }

  function ensureLeaflet() {
    if (window.L) return Promise.resolve(window.L);
    return new Promise((resolve, reject) => {
      if (!document.querySelector('link[href*="leaflet"]')) {
        const link = document.createElement('link');
        link.rel = 'stylesheet';
        link.href = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.css';
        document.head.appendChild(link);
      }
      const script = document.createElement('script');
      script.src = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.js';
      script.onload = () => resolve(window.L);
      script.onerror = () => reject(new Error('Failed to load Leaflet map library'));
      document.head.appendChild(script);
    });
  }

  function updateCoordsDisplay() {
    const el = document.getElementById('coords-text');
    if (el && geo) el.textContent = geo.lat.toFixed(5) + ', ' + geo.lng.toFixed(5);
  }

  function initOrUpdateMap(coords, zoom = 16) {
    ensureLeaflet().then((L) => {
      const container = document.getElementById('profile-map');
      if (!container) return;

      const customPin = L.divIcon({
        className: 'map-pin-icon',
        html: pinSvg,
        iconSize: [34, 46],
        iconAnchor: [17, 46],
        popupAnchor: [0, -42]
      });

      if (!leafletMap) {
        leafletMap = L.map('profile-map').setView([coords.lat, coords.lng], zoom);
        L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
          maxZoom: 19,
          attribution: '&copy; <a href="https://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>'
        }).addTo(leafletMap);

        leafletMarker = L.marker([coords.lat, coords.lng], {
          draggable: true,
          icon: customPin,
          title: 'Drag me to your exact doorstep'
        }).addTo(leafletMap);

        leafletMarker.bindPopup('<b>Exact Doorstep Pin</b><br>Drag this green pin to your gate or doorstep.').openPopup();

        leafletMarker.on('dragend', () => {
          const pos = leafletMarker.getLatLng();
          geo = { lat: pos.lat, lng: pos.lng };
          updateCoordsDisplay();
          const note = document.getElementById('geo-note');
          if (note) note.textContent = '📍 Doorstep pin moved! Click "Save changes" to save this position.';
        });

        leafletMap.on('click', (e) => {
          leafletMarker.setLatLng(e.latlng);
          geo = { lat: e.latlng.lat, lng: e.latlng.lng };
          updateCoordsDisplay();
          leafletMarker.openPopup();
          const note = document.getElementById('geo-note');
          if (note) note.textContent = '📍 Doorstep pin placed! Click "Save changes" to save this position.';
        });
      } else {
        leafletMap.setView([coords.lat, coords.lng], zoom);
        leafletMarker.setLatLng([coords.lat, coords.lng]);
        leafletMarker.openPopup();
      }

      setTimeout(() => {
        if (leafletMap) leafletMap.invalidateSize();
      }, 150);
    }).catch((err) => console.warn('Leaflet error:', err));
  }

  function bind() {
    const addr = document.getElementById('u-address');
    const note = document.getElementById('geo-note');
    const wrap = document.getElementById('map-wrap');

    if (geo) {
      initOrUpdateMap(geo, 16);
    }

    addr.addEventListener('input', () => {
      note.textContent = 'Address changed. Click "Verify address" to locate on the map.';
    });

    document.getElementById('verify').onclick = async (e) => {
      if (!addr.value.trim()) {
        toast('Please enter an address first.', true);
        return;
      }
      e.target.disabled = true;
      try {
        const g = await API.post('/api/maps/geocode', { address: addr.value });
        addr.value = g.formattedAddress;
        geo = { lat: g.lat, lng: g.lng };
        wrap.style.display = 'block';
        initOrUpdateMap(geo, 16);
        updateCoordsDisplay();
        note.textContent = '📍 Location found! Drag the green pin or click on the map to pinpoint your exact gate/doorstep.';
        toast('Address found. You can now drag the pin.');
      } catch (ex) {
        toast(ex.message, true);
      }
      e.target.disabled = false;
    };

    document.getElementById('update-form').onsubmit = async (e) => {
      e.preventDefault();
      const err = document.getElementById('u-err');
      err.textContent = '';
      try {
        const updated = await API.put('/api/profile', {
          name: document.getElementById('u-name').value,
          email: document.getElementById('u-email').value,
          phone: document.getElementById('u-phone').value,
          address: addr.value,
          latitude: geo ? geo.lat : null,
          longitude: geo ? geo.lng : null,
        });
        Object.assign(ME, updated);
        render();
        toast('Profile and map pin location saved.');
      } catch (ex) { err.textContent = ex.message; }
    };

    document.getElementById('pw-form').onsubmit = async (e) => {
      e.preventDefault();
      const err = document.getElementById('p-err');
      err.textContent = '';
      const next = document.getElementById('p-new').value;
      if (next !== document.getElementById('p-conf').value) { err.textContent = 'New passwords do not match.'; return; }
      try {
        await API.put('/api/profile/password', { current: document.getElementById('p-cur').value, next });
        e.target.reset();
        toast('Password changed.');
      } catch (ex) { err.textContent = ex.message; }
    };

    document.getElementById('delete').onclick = async () => {
      if (!confirm('This cannot be undone. Delete your account?')) return;
      try {
        await API.del('/api/profile');
        Cart.clear();
        location.href = '/index.html';
      } catch (ex) { toast(ex.message, true); }
    };
  }

  render();
}).catch((e) => toast(e.message, true));
