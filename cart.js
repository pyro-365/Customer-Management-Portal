Layout.mount('cart').then((content) => {
  const row = (i) => `
    <div class="cart-row">
      <div class="thumb" style="width:70px;height:70px">${img(i.imageUrl, i.name)}</div>
      <div class="grow">${esc(i.name)}</div>
      <input type="number" min="1" max="${i.stock}" value="${i.quantity}" data-qty="${i.id}" aria-label="Quantity for ${esc(i.name)}">
      <strong>${money(i.price * i.quantity)}</strong>
      <button class="btn ghost small" data-remove="${i.id}" type="button">Remove</button>
    </div>`;

  function render() {
    const items = Cart.all();
    content.innerHTML = '<h1>My cart</h1><p class="lead">Check your items, then place the order.</p>' + (items.length
      ? `<div class="stack">${items.map(row).join('')}</div>
         <div class="cart-total"><span>Total</span><strong>${money(Cart.total())}</strong></div>
         <button class="btn" id="place" type="button">Place order</button>`
      : '<p class="empty">Your cart is empty. <a href="/shop.html">Browse the shop</a> to add items.</p>');
    Layout.refreshCartBadge();
  }

  content.addEventListener('change', (e) => {
    if (!e.target.dataset.qty) return;
    Cart.setQty(Number(e.target.dataset.qty), parseInt(e.target.value, 10) || 1);
    render();
  });

  content.addEventListener('click', async (e) => {
    const rm = e.target.closest('[data-remove]');
    if (rm) { Cart.remove(Number(rm.dataset.remove)); render(); return; }
    if (e.target.id !== 'place') return;
    e.target.disabled = true;
    try {
      await API.post('/api/orders', { items: Cart.all().map((i) => ({ productId: i.id, quantity: i.quantity })) });
      Cart.clear();
      toast('Your order has been placed.');
      location.href = '/orders.html';
    } catch (ex) {
      toast(ex.message, true);
      e.target.disabled = false;
    }
  });

  render();
}).catch((e) => toast(e.message, true));
