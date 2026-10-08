Layout.mount('orders').then(async (content) => {
  const orders = await API.get('/api/orders');
  content.innerHTML = `
    <h1>My orders</h1>
    <p class="lead">All orders on your account.</p>
    <div class="stack">
      ${orders.length ? orders.map(orderCard).join('')
        : '<p class="empty">You have no orders yet. <a href="/shop.html">Browse the shop</a> to place your first one.</p>'}
    </div>`;
  bindOrderButtons(content, orders);
}).catch((e) => toast(e.message, true));
