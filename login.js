const form = document.getElementById('login-form');
const err = document.getElementById('err');

if (new URLSearchParams(location.search).has('registered'))
  document.getElementById('notice').textContent = 'Account created. Log in with your email or phone number.';

form.addEventListener('submit', async (e) => {
  e.preventDefault();
  err.textContent = '';
  const identifier = document.getElementById('identifier').value.trim();
  const password = document.getElementById('password').value;
  if (!identifier || !password) { err.textContent = 'Enter your email or phone number and your password.'; return; }
  const btn = form.querySelector('button');
  btn.disabled = true;
  try {
    await API.post('/api/auth/login', { identifier, password });
    location.href = '/dashboard.html';
  } catch (ex) {
    err.textContent = ex.message;
    btn.disabled = false;
  }
});
