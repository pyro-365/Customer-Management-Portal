/* Shared helpers: theme, fetch wrapper, formatting, toast, cart. Loaded on every page. */

(function () {
  const t = localStorage.getItem('cms_theme');
  if (t) document.documentElement.dataset.theme = t;
})();

const PUBLIC_PAGES = ['/', '/index.html', '/register.html'];

async function request(method, url, body) {
  const res = await fetch(url, {
    method,
    credentials: 'same-origin',
    headers: body ? { 'Content-Type': 'application/json' } : {},
    body: body ? JSON.stringify(body) : undefined,
  });
  const data = await res.json().catch(() => null);
  if (res.status === 401 && !PUBLIC_PAGES.includes(location.pathname)) {
    location.href = '/index.html';
    throw new Error('Please sign in.');
  }
  if (!res.ok) throw new Error((data && data.message) || 'Something went wrong (' + res.status + ').');
  return data;
}

const API = {
  get: (u) => request('GET', u),
  post: (u, b) => request('POST', u, b || {}),
  put: (u, b) => request('PUT', u, b || {}),
  del: (u) => request('DELETE', u),
};

const esc = (s) => String(s ?? '').replace(/[&<>"']/g, (c) =>
  ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));

const money = (n) => '\u20B9 ' + Number(n).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });

const PLACEHOLDER = '/img/placeholder.svg';
const img = (url, alt) =>
  `<img src="${esc(url || PLACEHOLDER)}" alt="${esc(alt || '')}" loading="lazy" referrerpolicy="no-referrer" onerror="this.onerror=null;this.src='${PLACEHOLDER}'">`;

function toast(message, isError) {
  const t = document.createElement('div');
  t.className = 'toast' + (isError ? ' error' : '');
  t.setAttribute('role', 'status');
  t.textContent = message;
  document.body.appendChild(t);
  setTimeout(() => t.remove(), 3500);
}

/* The cart lives in the browser. The server re-reads prices from the database when you place the order. */
const Cart = {
  KEY: 'cms_cart',
  all() { try { return JSON.parse(localStorage.getItem(this.KEY)) || []; } catch { return []; } },
  save(items) { localStorage.setItem(this.KEY, JSON.stringify(items)); },
  add(p, qty) {
    const items = this.all();
    const hit = items.find((i) => i.id === p.id);
    if (hit) hit.quantity = Math.min(hit.quantity + qty, p.stock);
    else items.push({ id: p.id, name: p.name, price: p.price, imageUrl: p.imageUrl, stock: p.stock, quantity: qty });
    this.save(items);
  },
  setQty(id, q) {
    const items = this.all();
    const hit = items.find((i) => i.id === id);
    if (hit) hit.quantity = Math.max(1, Math.min(q, hit.stock));
    this.save(items);
  },
  remove(id) { this.save(this.all().filter((i) => i.id !== id)); },
  clear() { localStorage.removeItem(this.KEY); },
  total() { return this.all().reduce((n, i) => n + i.price * i.quantity, 0); },
};
