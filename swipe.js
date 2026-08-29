(() => {
  const path = location.pathname;
  const screens = [
    { test: p => p.endsWith('/app.html'), href: 'app.html' },
    { test: p => p.endsWith('/') || p.endsWith('/index.html'), href: './' },
    { test: p => p.endsWith('/admin.html'), href: 'admin.html' }
  ];
  let current = screens.findIndex(s => s.test(path));
  if (current < 0) return;

  const extra=document.createElement('script');
  if(path.endsWith('/app.html')) extra.src='backend-client.js?ts='+Date.now();
  else if(path.endsWith('/admin.html')) extra.src='admin-cloud.js?ts='+Date.now();
  if(extra.src) document.head.appendChild(extra);

  let startX = 0, startY = 0, startAt = 0;
  document.addEventListener('touchstart', e => {
    if (e.touches.length !== 1) return;
    startX = e.touches[0].clientX;
    startY = e.touches[0].clientY;
    startAt = Date.now();
  }, { passive: true });

  document.addEventListener('touchend', e => {
    if (!startAt || !e.changedTouches.length) return;
    const dx = e.changedTouches[0].clientX - startX;
    const dy = e.changedTouches[0].clientY - startY;
    const elapsed = Date.now() - startAt;
    startAt = 0;
    if (elapsed > 850 || Math.abs(dx) < 70 || Math.abs(dx) < Math.abs(dy) * 1.25) return;
    const next = dx < 0 ? (current + 1) % screens.length : (current - 1 + screens.length) % screens.length;
    document.body.style.transition = 'opacity .12s ease, transform .12s ease';
    document.body.style.opacity = '.72';
    document.body.style.transform = `translateX(${dx < 0 ? '-12px' : '12px'})`;
    setTimeout(() => { location.href = screens[next].href; }, 90);
  }, { passive: true });
})();
