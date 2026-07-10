/* ============================================================
   MOHSIN STUDIO — SHARED SCRIPT
   ============================================================ */

const WHATSAPP_NUMBER = "923456751380";

/* 0. DEVELOPER SIGNATURE */
console.log(
  '%cMohsin Studio %c— Site by BehindTheCyber',
  'color:#c9a04e; font-size:20px; font-weight:bold; font-family:Georgia,serif;',
  'color:#8a8a8a; font-size:13px; font-family:monospace;'
);
console.log('%cbehindthecyber.netlify.app', 'color:#c9a04e; font-size:12px; font-family:monospace;');

/* 1. CAMERA-IRIS PAGE TRANSITION */
const overlay = document.querySelector('.page-transition-overlay');
function irisOpen() {
  if (!overlay) return;
  overlay.style.transition = 'clip-path 0.72s cubic-bezier(0.16,1,0.3,1)';
  overlay.style.clipPath = 'circle(0% at 50% 50%)';
}
function irisClose(cb) {
  if (!overlay) { if (cb) cb(); return; }
  overlay.style.transition = 'clip-path 0.56s cubic-bezier(0.7,0,0.84,0)';
  overlay.style.clipPath = 'circle(150% at 50% 50%)';
  setTimeout(() => { if (cb) cb(); }, 600);
}
window.addEventListener('DOMContentLoaded', () => { setTimeout(irisOpen, 60); });
document.addEventListener('click', e => {
  const link = e.target.closest('a[href]');
  if (!link) return;
  const href = link.getAttribute('href');
  if (!href || href.startsWith('#') || href.startsWith('mailto:')
      || href.startsWith('tel:') || href.startsWith('http')
      || href.startsWith('wa.me') || link.target === '_blank') return;
  e.preventDefault();
  irisClose(() => { window.location.href = href; });
});

/* 2. CUSTOM CURSOR */
if (window.matchMedia('(pointer: fine)').matches) {
  document.body.classList.add('custom-cursor');
  const dot  = document.createElement('div'); dot.className  = 'cursor-dot';
  const ring = document.createElement('div'); ring.className = 'cursor-ring';
  document.body.append(dot, ring);
  let mx = 0, my = 0, rx = 0, ry = 0;
  document.addEventListener('mousemove', e => { mx = e.clientX; my = e.clientY; });
  function animateCursor() {
    dot.style.left  = mx + 'px'; dot.style.top  = my + 'px';
    rx += (mx - rx) * 0.12;  ry += (my - ry) * 0.12;
    ring.style.left = rx + 'px'; ring.style.top = ry + 'px';
    requestAnimationFrame(animateCursor);
  }
  animateCursor();
  document.querySelectorAll('a, button, .gallery-item, .service-card, .testimonial-card, .filter-btn, .service-icon-card, .addon-card, .team-card')
    .forEach(el => {
      el.addEventListener('mouseenter', () => ring.classList.add('hover'));
      el.addEventListener('mouseleave', () => ring.classList.remove('hover'));
    });
}

/* 3. MOBILE NAV BURGER */
const burgerBtn  = document.getElementById('burgerBtn');
const mobileMenu = document.getElementById('mobileMenu');
if (burgerBtn && mobileMenu) {
  burgerBtn.addEventListener('click', () => {
    const open = mobileMenu.classList.toggle('open');
    burgerBtn.classList.toggle('open', open);
    burgerBtn.setAttribute('aria-expanded', open);
  });
  mobileMenu.querySelectorAll('a').forEach(a => a.addEventListener('click', () => {
    mobileMenu.classList.remove('open');
    burgerBtn.classList.remove('open');
    burgerBtn.setAttribute('aria-expanded', 'false');
  }));
}

/* 4. ACTIVE NAV LINK */
const currentPage = window.location.pathname.split('/').pop() || 'index.html';
document.querySelectorAll('.nav-links a').forEach(a => {
  const aPage = a.getAttribute('href') || '';
  if (aPage === currentPage || (currentPage === '' && aPage === 'index.html')) {
    a.classList.add('active');
  }
});

/* 5. FOOTER YEAR */
const yr = document.getElementById('year');
if (yr) yr.textContent = new Date().getFullYear();

/* 6. WHATSAPP FLOATING BUTTON */
const waFloat = document.getElementById('waFloat');
if (waFloat) {
  waFloat.href = 'https://wa.me/' + WHATSAPP_NUMBER + '?text=' + encodeURIComponent("Hi, I am from website mohsinstudiopk.netlify.app");
}

/* 7. SCROLL REVEAL */
const revealEls = document.querySelectorAll('.reveal');
const revealIO  = new IntersectionObserver((entries) => {
  entries.forEach(entry => { if (entry.isIntersecting) entry.target.classList.add('in'); });
}, { threshold: 0.13 });
revealEls.forEach(el => revealIO.observe(el));

/* 8. HERO WORD-BY-WORD TITLE REVEAL */
const heroTitle = document.querySelector('.hero-title');
if (heroTitle) {
  function wrapWords(node) {
    if (node.nodeType === Node.TEXT_NODE) {
      const words = node.textContent.split(/(\s+)/);
      const frag  = document.createDocumentFragment();
      words.forEach(part => {
        if (/^\s+$/.test(part)) {
          frag.appendChild(document.createTextNode(part));
        } else if (part) {
          const outer = document.createElement('span'); outer.className = 'word-wrap';
          const inner = document.createElement('span'); inner.className = 'word';
          inner.textContent = part;
          outer.appendChild(inner);
          frag.appendChild(outer);
        }
      });
      node.parentNode.replaceChild(frag, node);
    } else if (node.nodeType === Node.ELEMENT_NODE) {
      [...node.childNodes].forEach(wrapWords);
    }
  }
  wrapWords(heroTitle);
  requestAnimationFrame(() => requestAnimationFrame(() => heroTitle.classList.add('in')));
}
['hero-lead','hero-actions'].forEach(id => {
  const el = document.getElementById(id);
  if (el) requestAnimationFrame(() => requestAnimationFrame(() => el.classList.add('in')));
});
const heroFrame = document.querySelector('.hero-frame');
if (heroFrame) requestAnimationFrame(() => requestAnimationFrame(() => heroFrame.classList.add('in')));

/* 9. CARD TILT-ON-HOVER */
if (window.matchMedia('(pointer: fine)').matches) {
  document.querySelectorAll('.service-card, .testimonial-card, .addon-card, .service-icon-card, .team-card').forEach(card => {
    card.addEventListener('mousemove', e => {
      const r = card.getBoundingClientRect();
      const x = e.clientX - r.left - r.width  / 2;
      const y = e.clientY - r.top  - r.height / 2;
      const tiltX = -(y / (r.height / 2)) * 7;
      const tiltY =  (x / (r.width  / 2)) * 7;
      card.style.transform = 'perspective(600px) rotateX(' + tiltX + 'deg) rotateY(' + tiltY + 'deg) translateY(-6px)';
    });
    card.addEventListener('mouseleave', () => {
      card.style.transition = 'transform .7s cubic-bezier(0.16,1,0.3,1)';
      card.style.transform  = '';
      setTimeout(() => card.style.transition = '', 700);
    });
    card.addEventListener('mouseenter', () => {
      card.style.transition = 'transform .15s linear';
    });
  });
}

/* 10. SCROLL PARALLAX */
if (!window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
  const parallaxEls = [...document.querySelectorAll('.parallax-img')];
  let ticking = false;
  function updateParallax() {
    const vh = window.innerHeight;
    parallaxEls.forEach(el => {
      const rect = el.parentElement.getBoundingClientRect();
      if (rect.bottom < -200 || rect.top > vh + 200) return;
      const center = (rect.top + rect.height / 2) - vh / 2;
      const t = Math.max(-60, Math.min(60, -center * parseFloat(el.dataset.speed || '0.08')));
      el.style.transform = 'translateY(' + t.toFixed(1) + 'px)';
    });
    ticking = false;
  }
  window.addEventListener('scroll', () => { if (!ticking) { requestAnimationFrame(updateParallax); ticking = true; } }, { passive: true });
  window.addEventListener('resize', updateParallax);
  updateParallax();
}

/* 11. ANIMATED COUNTERS */
function animateCounter(el, target, suffix) {
  const start = performance.now();
  function step(now) {
    const p = Math.min((now - start) / 1800, 1);
    const eased = p < 0.5 ? 2*p*p : -1+(4-2*p)*p;
    el.textContent = Math.floor(eased * target) + suffix;
    if (p < 1) requestAnimationFrame(step);
  }
  requestAnimationFrame(step);
}
const statsIO = new IntersectionObserver((entries) => {
  entries.forEach(entry => {
    if (!entry.isIntersecting) return;
    animateCounter(entry.target, parseInt(entry.target.dataset.target, 10), entry.target.dataset.suffix || '');
    statsIO.unobserve(entry.target);
  });
}, { threshold: 0.5 });
document.querySelectorAll('.stat-value[data-target]').forEach(el => statsIO.observe(el));

/* ============================================================
   12. GALLERY
   ── HOW TO ADD PHOTOS ────────────────────────────────────────
   Format: [caption, featured, imgPath]
   featured = true → shows "Featured" ribbon
   Add image to assets/images/ then add a new row below.
   Example: ["Walima Reception", false, "assets/images/gallery-07.webp"],
   ============================================================ */
(function(){
  const galleryGrid = document.getElementById('galleryGrid');
  if (!galleryGrid) return;

  // Format: [caption, featured, imgPath, category]
  // Category must be one of: Barat, Walima, Mehandi, Ubtan, Drone, Pre-Wedding, Portraits, Wedding, Commercial
  const galleryItems = [
    ["Mehndi Night",      true,  "assets/images/gallery-01.webp", "Mehandi"],
    ["Baraat Procession", false, "assets/images/gallery-02.webp", "Barat"],
    ["Walima Ceremony",   true,  "assets/images/gallery-03.webp", "Walima"],
    ["Portrait Session",  false, "assets/images/gallery-04.webp", "Portraits"],
    ["Event Coverage",    false, "assets/images/gallery-05.webp", "Wedding"],
    ["Mehndi Ceremony",   false, "assets/images/gallery-06.webp", "Mehandi"],
    ["Bridal Portrait",   true,  "assets/images/gallery-07.webp", "Portraits"],
    ["Corporate Event",   false, "assets/images/gallery-08.webp", "Commercial"],
    ["Baraat Entry",      false, "assets/images/gallery-09.webp", "Barat"],
    ["Shoot",      false, "assets/images/gallery-10.webp", "Portrait"],
    // ADD MORE BELOW:
    // ["Walima Reception", false, "assets/images/gallery-07.webp", "Walima"],
  ];

  galleryItems.forEach(function(item, idx) {
    var caption  = item[0];
    var featured = item[1];
    var imgPath  = item[2];
    var category = item[3] || 'Wedding';
    var num      = String(idx + 1).padStart(3, '0');
    var speed    = [0.05, 0.09, 0.13][idx % 3];
    var el       = document.createElement('div');
    el.className = 'gallery-item' + (featured ? ' is-featured' : '');
    el.setAttribute('data-category', category);
    el.innerHTML =
      (featured ? '<span class="featured-ribbon">Featured</span>' : '') +
      '<span class="gallery-num">No. ' + num + '</span>' +
      '<div class="parallax-img" data-speed="' + speed + '">' +
        (imgPath
          ? '<img src="' + imgPath + '" alt="' + caption + '" style="width:100%;height:100%;object-fit:cover;" loading="lazy">'
          : '<div class="frame-ph"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.4"><path d="M4 7h3l2-2h6l2 2h3a1 1 0 0 1 1 1v10a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1V8a1 1 0 0 1 1-1z"/><circle cx="12" cy="13" r="3.5"/></svg></div>') +
      '</div>' +
      '<div class="gallery-meta">' + caption + ' · ' + category + '</div>';
    el.addEventListener('click', function(){ openLightbox(imgPath, caption); });
    galleryGrid.appendChild(el);
  });

  // Photo filter buttons
  var galleryFilterRow = document.getElementById('galleryFilterRow');
  if (galleryFilterRow) {
    galleryFilterRow.addEventListener('click', function(e) {
      var btn = e.target.closest('.filter-btn');
      if (!btn) return;
      galleryFilterRow.querySelectorAll('.filter-btn').forEach(function(b){ b.classList.remove('active'); });
      btn.classList.add('active');
      var filter = btn.getAttribute('data-filter');
      galleryGrid.querySelectorAll('.gallery-item').forEach(function(item) {
        var match = (filter === 'all' || item.getAttribute('data-category') === filter);
        item.style.display = match ? '' : 'none';
      });
    });
  }
})();

/* 13. LIGHTBOX */
var lightbox    = document.getElementById('lightbox');
var lightboxNum = document.getElementById('lightboxNum');

function openLightbox(imgSrc, caption) {
  if (!lightbox) return;
  if (lightboxNum) lightboxNum.textContent = caption || '';
  var lbImg = lightbox.querySelector('.lightbox-frame img');
  if (lbImg && imgSrc) { lbImg.src = imgSrc; lbImg.alt = caption || ''; }
  lightbox.style.display = 'flex';
  requestAnimationFrame(function(){ requestAnimationFrame(function(){ lightbox.classList.add('open','show'); }); });
}
function closeLightbox() {
  if (!lightbox) return;
  lightbox.classList.remove('show');
  setTimeout(function(){ lightbox.classList.remove('open'); lightbox.style.display = ''; }, 400);
}
var lbClose = document.getElementById('lightboxClose');
if (lbClose) lbClose.addEventListener('click', closeLightbox);
if (lightbox) lightbox.addEventListener('click', function(e){ if (e.target === lightbox) closeLightbox(); });
document.addEventListener('keydown', function(e){ if (e.key === 'Escape') closeLightbox(); });

/* 14. BOOKING FORM */
var bookingForm = document.getElementById('bookingForm');
var formStatus  = document.getElementById('formStatus');
function showStatus(msg, type) {
  if (!formStatus) return;
  formStatus.textContent = msg;
  formStatus.className = 'form-status show ' + type;
}
if (bookingForm) {
  bookingForm.addEventListener('submit', function(e) {
    e.preventDefault();
    function get(id) { var el = document.getElementById(id); return el ? el.value : ''; }
    var name = get('name'), phone = get('phone'), wa = get('whatsapp');
    var email = get('email'), date = get('eventDate'), pkg = get('package'), msg = get('message');
    var waText = 'New Booking — Mohsin Studio\nName: ' + name + '\nPhone: ' + phone +
      '\nWhatsApp: ' + wa + '\nEmail: ' + (email || '-') + '\nEvent date: ' + date +
      '\nPackage: ' + pkg + '\nDetails: ' + (msg || '-');
    window.open('https://wa.me/' + WHATSAPP_NUMBER + '?text=' + encodeURIComponent(waText), '_blank');
    fetch('/', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: Object.keys({name:name,phone:phone,whatsapp:wa,email:email,eventDate:date,package:pkg,message:msg,'form-name':'booking'})
        .map(function(k){ return encodeURIComponent(k) + '=' + encodeURIComponent(({name:name,phone:phone,whatsapp:wa,email:email,eventDate:date,package:pkg,message:msg,'form-name':'booking'})[k]); }).join('&')
    })
    .then(function(){ showStatus('Sent! WhatsApp opened.', 'ok'); bookingForm.reset(); })
    .catch(function(){ showStatus('WhatsApp opened. (Backup failed)', 'err'); });
  });
}

/* ============================================================
   16. TESTIMONIALS
   ── HOW TO ADD ───────────────────────────────────────────────
   Copy a { name, event, quote } block and paste inside items array.
   ============================================================ */
(function(){
  var grid = document.getElementById('testimonialGrid');
  if (!grid) return;

  var items = [
    { name: "Ahmad Raza",     event: "Baraat · Gujranwala",          quote: "Mohsin Studio ne hamari shadi ko yadgar bana diya. Har lamha itna khoobsoorat capture kiya ke dil khush ho gaya. Photography aur videography dono kamaal ki thi." },
    { name: "Sana Malik",     event: "Mehndi · Sialkot",             quote: "Bohot professional team hai. Mehndi ki raat ki saari photos itni natural aur colorful aayi hain. Hum ne jo expect kiya tha usse kaafi zyada mila." },
    { name: "Usman Butt",     event: "Walima · Pasrur",              quote: "Delivery time bohot fast thi aur editing style unique hai. Doston ne bhi photos dekh ke Mohsin Studio ka number maanga. Highly recommended!" },
    { name: "Fatima Nawaz",   event: "Portrait Session · Sialkot",   quote: "Pehli baar portrait session karwaya aur experience bohot acha raha. Team ne comfortable feel karaaya aur results dekh ke khud hairaan ho gayi." },
    { name: "Bilal Chaudhry", event: "Baraat + Walima · Gujranwala", quote: "Poori wedding coverage li Mohsin Studio se. Drone shots especially bohot shandar aayi hain. Budget mein itni quality milna mushkil hai — yeh log sach mein best hain." },
    { name: "Ayesha Tariq",   event: "Mehndi + Baraat · Pasrur",     quote: "Mohsin bhai aur unki team ne dono ceremonies cover ki. Ek baar bhi koi moment miss nahi hua. Album dekh ke aankhon mein aansu aa gaye — shukria Mohsin Studio!" }
  ];

  items.forEach(function(t) {
    var card = document.createElement('div');
    card.className = 'testimonial-card';
    card.innerHTML =
      '<div class="stars">★★★★★</div>' +
      '<p class="quote">"' + t.quote + '"</p>' +
      '<div class="testimonial-who">' +
        '<span class="who-name">' + t.name + '</span>' +
        '<span class="who-event">' + t.event + '</span>' +
      '</div>';
    grid.appendChild(card);
  });
})();

/* WOW-FACTOR INTERACTIONS */

(function injectMorphBlob(){
  var hero = document.querySelector('.hero');
  if (!hero || window.innerWidth < 900) return;
  var blob = document.createElement('div');
  blob.className = 'morph-blob';
  hero.insertBefore(blob, hero.firstChild);
})();

(function init3DTilt(){
  if (window.matchMedia('(pointer: coarse)').matches) return;
  document.querySelectorAll('.service-card, .video-card, .cat-card').forEach(function(el) {
    el.addEventListener('mousemove', function(e) {
      var rect = el.getBoundingClientRect();
      var px = (e.clientX - rect.left) / rect.width - 0.5;
      var py = (e.clientY - rect.top) / rect.height - 0.5;
      el.style.transform = 'perspective(800px) rotateX(' + (-py * 6).toFixed(2) + 'deg) rotateY(' + (px * 6).toFixed(2) + 'deg) translateY(-4px)';
    });
    el.addEventListener('mouseleave', function() { el.style.transform = ''; });
  });
})();

(function initMagneticButtons(){
  if (window.matchMedia('(pointer: coarse)').matches) return;
  document.querySelectorAll('.btn-primary, .nav-cta').forEach(function(btn) {
    btn.addEventListener('mousemove', function(e) {
      var rect = btn.getBoundingClientRect();
      btn.style.transform = 'translate(' + ((e.clientX - rect.left - rect.width/2) * 0.25) + 'px,' + ((e.clientY - rect.top - rect.height/2) * 0.35) + 'px)';
    });
    btn.addEventListener('mouseleave', function() { btn.style.transform = ''; });
  });
})();

(function initCursorTrail(){
  if (window.matchMedia('(pointer: coarse)').matches) return;
  var trail = document.createElement('div');
  trail.className = 'cursor-trail';
  document.body.appendChild(trail);
  var tx = 0, ty = 0, mx = 0, my = 0;
  window.addEventListener('mousemove', function(e) { mx = e.clientX; my = e.clientY; });
  function animateTrail() {
    tx += (mx - tx) * 0.12; ty += (my - ty) * 0.12;
    trail.style.transform = 'translate(' + tx + 'px,' + ty + 'px) translate(-50%,-50%)';
    requestAnimationFrame(animateTrail);
  }
  animateTrail();
})();

(function initRevealMask(){
  var maskTargets = document.querySelectorAll('.hero-frame, .gallery-item');
  maskTargets.forEach(function(el) { el.classList.add('reveal-mask'); });
  var maskIO = new IntersectionObserver(function(entries) {
    entries.forEach(function(entry) {
      if (entry.isIntersecting) { entry.target.classList.add('wipe-in'); maskIO.unobserve(entry.target); }
    });
  }, { threshold: 0.35 });
  maskTargets.forEach(function(el) { maskIO.observe(el); });
})();

(function markActiveNav(){
  var current = location.pathname.split('/').pop() || 'index.html';
  document.querySelectorAll('.nav-links a').forEach(function(a) {
    if (a.getAttribute('href') === current) a.classList.add('active');
  });
})();