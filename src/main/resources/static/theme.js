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
        <span style="font-size: 1.2rem; filter: drop-shadow(0 0 8px rgba(255,200,0,0.6)); margin-right: 8px;">☀️</span>
        <span style="color: #f59e0b">Light</span>`;
    } else {
      btn.innerHTML = `
        <span style="font-size: 1.2rem; filter: drop-shadow(0 0 8px rgba(0,212,255,0.6)); margin-right: 8px;">🌙</span>
        <span style="color: #00d4ff">Dark</span>`;
    }
  }
});
