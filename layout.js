/* Sidebar + top bar for every page after login. Pages call: Layout.mount('shop').then(content => ...) */

const NAV = [
  ['/dashboard.html', 'Dashboard', 'dashboard'],
  ['/orders.html', 'My Orders', 'orders'],
  ['/shop.html', 'Shop', 'shop'],
  ['/cart.html', 'Cart', 'cart'],
  ['/assistant.html', 'AI Assistant', 'assistant'],
  ['/profile.html', 'My Profile', 'profile'],
  ['/profile.html#update', 'Update Profile', 'update'],
  ['/profile.html#password', 'Change Password', 'password'],
];

const Layout = {
  refreshCartBadge() {
    const b = document.getElementById('cart-badge');
    if (b) b.textContent = Cart.all().length;
  },

  async mount(active) {
    const me = await API.get('/api/auth/me');
    window.ME = me;
    const dark = document.documentElement.dataset.theme === 'dark';

    const links = NAV.map(([href, label, key]) =>
      `<a class="nav-link${key === active ? ' active' : ''}" href="${href}">${label}` +
      (key === 'cart' ? `<span class="badge" id="cart-badge">${Cart.all().length}</span>` : '') + '</a>').join('');

    const shell = document.getElementById('shell');
    shell.className = 'shell';
    shell.innerHTML = `
      <aside class="sidebar">
        <div class="brand">Customer<small>Management System</small></div>
        <nav>${links}</nav>
        <div class="side-foot">
          <a class="nav-link danger" href="/profile.html#danger">Delete Account</a>
          <button class="nav-link" id="logout" type="button">Log out</button>
        </div>
      </aside>
      <div class="main">
        <header class="topbar">
          <strong>Customer Management System</strong>
          <div class="top-actions">
            <button class="btn ghost small" id="theme" type="button">${dark ? 'Light mode' : 'Dark mode'}</button>
            <span class="user">${esc(me.name)}</span>
          </div>
        </header>
        <main class="content" id="content"></main>
      </div>`;

    document.getElementById('theme').onclick = (e) => {
      const next = document.documentElement.dataset.theme === 'dark' ? 'light' : 'dark';
      document.documentElement.dataset.theme = next;
      localStorage.setItem('cms_theme', next);
      e.target.textContent = next === 'dark' ? 'Light mode' : 'Dark mode';
    };
    document.getElementById('logout').onclick = async () => {
      if (!confirm('Do you want to log out?')) return;
      try { await API.post('/api/auth/logout'); } catch (e) { /* already signed out */ }
      Cart.clear();
      location.href = '/index.html';
    };
    return document.getElementById('content');
  },
};
