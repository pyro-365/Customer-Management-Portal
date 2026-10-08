Layout.mount('dashboard').then(async (content) => {
  const d = await API.get('/api/dashboard');
  const stat = (label, value) => `<div class="stat"><span>${label}</span><strong>${value}</strong></div>`;

  content.innerHTML = `
    <h1>Dashboard</h1>
    <p class="lead">Welcome back, ${esc(ME.name)}!</p>
    <section class="stats">
      ${stat('Total customers', d.totalCustomers)}
      ${stat('My orders', d.myOrders)}
      ${stat('Total spent', money(d.totalSpent))}
    </section>
    <section class="card">
      <h2>My information</h2>
      <dl class="info">
        <dt>Name</dt><dd>${esc(ME.name)}</dd>
        <dt>Email</dt><dd>${esc(ME.email)}</dd>
        <dt>Phone</dt><dd>${esc(ME.phone)}</dd>
        <dt>Address</dt><dd>${ME.address ? esc(ME.address) : '<span class="muted">Not added yet</span>'}</dd>
      </dl>
      <a class="btn small" href="/profile.html#update">Edit profile</a>
    </section>
    <section class="card">
      <h2>My recent orders</h2>
      <p class="muted">Your latest orders</p>
      <div class="stack" id="recent" style="margin:1rem 0">
        ${d.recentOrders.length ? d.recentOrders.map(orderCard).join('')
          : '<p class="empty">You have no orders yet. <a href="/shop.html">Browse the shop</a> to place your first one.</p>'}
      </div>
      <a class="btn ghost small" href="/orders.html">View all orders</a>
    </section>`;
  bindOrderButtons(content, d.recentOrders);
}).catch((e) => toast(e.message, true));
