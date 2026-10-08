Layout.mount('assistant').then((content) => {
  content.innerHTML = `
    <h1>AI assistant</h1>
    <p class="lead">Ask about products, prices, stock or your orders.</p>
    <div class="chips" id="chips"></div>
    <section class="chat">
      <div class="messages" id="messages" aria-live="polite"></div>
      <form class="chat-form" id="chat-form">
        <input id="msg" autocomplete="off" placeholder="Ask a question" aria-label="Your question">
        <button class="btn" type="submit" id="send">Send</button>
      </form>
    </section>`;

  const box = document.getElementById('messages');
  const input = document.getElementById('msg');
  const send = document.getElementById('send');
  const history = [];

  const add = (cls, text) => {
    const d = document.createElement('div');
    d.className = 'msg ' + cls;
    d.textContent = text;
    box.appendChild(d);
    box.scrollTop = box.scrollHeight;
    return d;
  };

  add('bot', 'Hi ' + ME.name.split(' ')[0] + '! I can search the catalogue and look up your orders. What do you need?');

  ['What is in stock today?', 'What is the status of my latest order?', 'Suggest a gift under 2000 rupees'].forEach((t) => {
    const c = document.createElement('button');
    c.type = 'button';
    c.className = 'chip';
    c.textContent = t;
    c.onclick = () => ask(t);
    document.getElementById('chips').appendChild(c);
  });

  async function ask(text) {
    text = text.trim();
    if (!text) return;
    input.value = '';
    add('me', text);
    const wait = add('bot', 'Thinking...');
    send.disabled = true;
    try {
      const r = await API.post('/api/assistant', { message: text, history: history.slice(-6) });
      wait.textContent = r.reply;
      history.push({ role: 'user', text }, { role: 'model', text: r.reply });
    } catch (ex) {
      wait.textContent = ex.message;
    }
    send.disabled = false;
    input.focus();
  }

  document.getElementById('chat-form').onsubmit = (e) => { e.preventDefault(); ask(input.value); };
}).catch((e) => toast(e.message, true));
