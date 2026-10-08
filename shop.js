Layout.mount('shop').then(async (content) => {
  content.innerHTML = `
    <h1>Shop products</h1>
    <p class="lead">Browse and add items to your cart.</p>
    <div class="filters">
      <input id="q" type="search" placeholder="Search products" aria-label="Search products">
      <select id="cat" aria-label="Category"><option value="">All categories</option></select>
    </div>
    <div class="grid" id="grid"></div>
    <div class="pager" id="pager"></div>`;

  const q = document.getElementById('q');
  const cat = document.getElementById('cat');
  const grid = document.getElementById('grid');
  const pager = document.getElementById('pager');
  let page = 0;
  let items = [];

  (await API.get('/api/products/categories')).forEach((c) => cat.insertAdjacentHTML('beforeend', `<option>${esc(c)}</option>`));

  const card = (p) => `
    <article class="product">
      <div class="thumb">${img(p.imageUrl, p.name)}</div>
      <h3>${esc(p.name)}</h3>
      <p class="muted clamp">${esc(p.description)}</p>
      <p class="price">${money(p.price)}</p>
      <p class="muted">${p.stock > 0 ? 'In stock: ' + p.stock : 'Out of stock'}</p>
      <div class="buy">
        <input type="number" min="1" max="${p.stock}" value="1" ${p.stock <= 0 ? 'disabled' : ''} aria-label="Quantity for ${esc(p.name)}">
        <button class="btn small" data-add="${p.id}" type="button" ${p.stock <= 0 ? 'disabled' : ''}>Add to cart</button>
      </div>
    </article>`;

  async function load() {
    const params = new URLSearchParams({ q: q.value, category: cat.value, page, size: 24 });
    const r = await API.get('/api/products?' + params);
    items = r.items;
    grid.innerHTML = items.length ? items.map(card).join('')
      : '<p class="empty">No products match. Try a different search or category.</p>';
    pager.innerHTML = r.totalPages > 1
      ? `<button class="btn ghost small" id="prev" ${page <= 0 ? 'disabled' : ''}>Previous</button>
         <span>Page ${r.page + 1} of ${r.totalPages}</span>
         <button class="btn ghost small" id="next" ${page >= r.totalPages - 1 ? 'disabled' : ''}>Next</button>` : '';
    const prev = document.getElementById('prev');
    const next = document.getElementById('next');
    if (prev) prev.onclick = () => { page--; load(); window.scrollTo(0, 0); };
    if (next) next.onclick = () => { page++; load(); window.scrollTo(0, 0); };
  }

  grid.addEventListener('click', (e) => {
    const b = e.target.closest('[data-add]');
    if (!b) return;
    const p = items.find((x) => String(x.id) === b.dataset.add);
    const qty = Math.max(1, Math.min(parseInt(b.parentElement.querySelector('input').value, 10) || 1, p.stock));
    Cart.add(p, qty);
    Layout.refreshCartBadge();
    toast(qty + ' \u00D7 ' + p.name + ' added to cart.');
  });

  let timer;
  q.addEventListener('input', () => { clearTimeout(timer); timer = setTimeout(() => { page = 0; load().catch((e) => toast(e.message, true)); }, 300); });
  cat.addEventListener('change', () => { page = 0; load().catch((e) => toast(e.message, true)); });
  await load();
}).catch((e) => toast(e.message, true));
