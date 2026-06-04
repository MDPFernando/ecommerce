// Apply saved theme immediately (before DOM paints)
(function() {
  const t = localStorage.getItem('theme') || 'dark';
  document.documentElement.setAttribute('data-theme', t);
})();

document.addEventListener('DOMContentLoaded', () => {
  const html = document.documentElement;

  document.querySelectorAll('.theme-toggle').forEach(btn => {
    updateBtn(btn, html.getAttribute('data-theme'));

    btn.addEventListener('click', () => {
      const current = html.getAttribute('data-theme');
      const next = current === 'dark' ? 'light' : 'dark';
      html.setAttribute('data-theme', next);
      localStorage.setItem('theme', next);
      document.querySelectorAll('.theme-toggle').forEach(b => updateBtn(b, next));
    });
  });

  function updateBtn(btn, theme) {
    if (theme === 'dark') {
      btn.innerHTML = `
        <span style="display: inline-flex; align-items: center; justify-content: center; width: 26px; height: 26px; border-radius: 50%; background: rgba(255, 159, 28, 0.15); box-shadow: 0 0 12px rgba(255, 159, 28, 0.4); margin-right: 10px; font-size: 1.1rem; filter: drop-shadow(0 0 3px rgba(255, 159, 28, 0.8));">☀️</span>
        <span style="color: #ffa500; font-weight: 700; font-family: 'Orbitron', sans-serif; letter-spacing: 1px; font-size: 0.82rem; text-transform: uppercase;">Light</span>`;
    } else {
      btn.innerHTML = `
        <span style="display: inline-flex; align-items: center; justify-content: center; width: 26px; height: 26px; border-radius: 50%; background: rgba(0, 212, 255, 0.15); box-shadow: 0 0 12px rgba(0, 212, 255, 0.4); margin-right: 10px; font-size: 1.1rem; filter: drop-shadow(0 0 3px rgba(0, 212, 255, 0.8));">🌙</span>
        <span style="color: #00d4ff; font-weight: 700; font-family: 'Orbitron', sans-serif; letter-spacing: 1px; font-size: 0.82rem; text-transform: uppercase;">Dark</span>`;
    }
  }
});

