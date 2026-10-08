/* Pieces of UI used on more than one page. */

function orderCard(o) {
  const done = /deliver/i.test(o.status);
  return `<article class="order-card">
    <div class="thumb">${img(o.imageUrl, o.productName)}</div>
    <div class="order-info">
      <h3>${esc(o.productName)}</h3>
      <p>Quantity: ${o.quantity}</p>
      <p>Price: ${money(o.price)}</p>
      <p>Ordered on: ${esc(o.orderDate)}</p>
    </div>
    <div class="order-side">
      <span class="status ${done ? 'done' : ''}">${esc(o.status)}</span>
      <strong>${money(o.price * o.quantity)}</strong>
      <button class="btn small" data-order="${o.id}" type="button">View order</button>
    </div>
  </article>`;
}

function bindOrderButtons(root, orders) {
  root.querySelectorAll('[data-order]').forEach((b) =>
    b.addEventListener('click', () => showOrder(orders.find((o) => String(o.id) === b.dataset.order))));
}

function showOrder(o) {
  const dlg = document.createElement('dialog');
  dlg.className = 'modal';
  const hasMap = window.ME && ME.latitude != null && ME.longitude != null;
  dlg.innerHTML = `
    <h2>Order #${o.id}</h2>
    <dl class="info" style="margin-top:1rem">
      <dt>Product</dt><dd>${esc(o.productName)}</dd>
      <dt>Quantity</dt><dd>${o.quantity}</dd>
      <dt>Price</dt><dd>${money(o.price)}</dd>
      <dt>Total</dt><dd>${money(o.price * o.quantity)}</dd>
      <dt>Order date</dt><dd>${esc(o.orderDate)}</dd>
      <dt>Status</dt><dd>${esc(o.status)}</dd>
      ${window.ME && ME.address ? `<dt>Deliver to</dt><dd>${esc(ME.address)}</dd>` : ''}
    </dl>
    ${hasMap ? `
      <div style="margin-top:1rem">
        <h3 style="font-size:0.95rem;margin-bottom:0.4rem;color:var(--muted);">Delivery Location</h3>
        <iframe class="map" title="Delivery location map"
          src="https://www.openstreetmap.org/export/embed.html?bbox=${(ME.longitude - 0.008).toFixed(6)}%2C${(ME.latitude - 0.005).toFixed(6)}%2C${(ME.longitude + 0.008).toFixed(6)}%2C${(ME.latitude + 0.005).toFixed(6)}&layer=mapnik&marker=${ME.latitude}%2C${ME.longitude}"
          style="width:100%;height:220px;border-radius:10px;border:1.5px solid var(--line);display:block;"
          loading="lazy">
        </iframe>
        <div style="margin-top:0.35rem;font-size:0.8rem;text-align:right;">
          <a href="https://www.openstreetmap.org/?mlat=${ME.latitude}&mlon=${ME.longitude}#map=16/${ME.latitude}/${ME.longitude}" target="_blank" rel="noopener noreferrer" style="color:var(--primary);text-decoration:none;font-weight:500;">
            Open in OpenStreetMap ↗
          </a>
        </div>
      </div>
    ` : ''}
    <p style="margin:1.2rem 0 0"><button class="btn small" type="button" id="close-order">Close</button></p>`;
  document.body.appendChild(dlg);
  dlg.querySelector('#close-order').onclick = () => dlg.close();
  dlg.addEventListener('close', () => dlg.remove());
  dlg.showModal();
}
