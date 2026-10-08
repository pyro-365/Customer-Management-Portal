const form = document.getElementById('register-form');
const err = document.getElementById('err');
const pw = document.getElementById('password');

function strength(p) {
  if (!p) return 0;
  let s = 0;
  if (p.length >= 6) s++;
  if (p.length >= 10) s++;
  if (/[A-Z]/.test(p)) s++;
  if (/\d/.test(p)) s++;
  if (/[^A-Za-z0-9]/.test(p)) s++;
  return s;
}

pw.addEventListener('input', () => {
  const s = strength(pw.value);
  const label = !pw.value ? '-' : s <= 1 ? 'Weak' : s <= 3 ? 'Medium' : 'Strong';
  const bar = document.getElementById('meter-bar');
  bar.style.width = (s / 5) * 100 + '%';
  bar.style.background = s <= 1 ? 'var(--danger)' : s <= 3 ? 'var(--accent)' : 'var(--ok)';
  document.getElementById('meter-text').textContent = 'Password strength: ' + label;
});

form.addEventListener('submit', async (e) => {
  e.preventDefault();
  err.textContent = '';
  const v = (id) => document.getElementById(id).value.trim();
  const body = { name: v('name'), email: v('email'), phone: v('phone'), password: pw.value };

  if (!body.name || !body.email || !body.phone || !body.password) { err.textContent = 'Fill in every field.'; return; }
  if (!/^\d{10,15}$/.test(body.phone.replace(/[^0-9]/g, ''))) { err.textContent = 'Phone number must have 10 to 15 digits.'; return; }
  if (body.password !== document.getElementById('confirm').value) { err.textContent = 'Passwords do not match.'; return; }

  const btn = form.querySelector('button');
  btn.disabled = true;
  try {
    await API.post('/api/auth/register', body);
    location.href = '/index.html?registered=1';
  } catch (ex) {
    err.textContent = ex.message;
    btn.disabled = false;
  }
});
