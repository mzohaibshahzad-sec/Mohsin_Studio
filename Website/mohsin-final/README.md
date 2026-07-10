# Mohsin Studio — Website

A photography & videography studio website for Mohsin Studio (Gujranwala).
6 pages, fully static — no build tools, no backend server required.

**Live site:** https://mohsinstudiopk.netlify.app/

---

## Project structure

```
mohsin-studio-website/
├── index.html              ← Home
├── about.html                ← About (history, founders, values)
├── services.html             ← Services (what we offer — icon list)
├── packages.html              ← Packages (pricing & coverage)
├── gallery.html                ← Gallery (with category filters)
├── contact.html                 ← Contact info + Booking form
├── css/
│   └── style.css            ← all styling (shared across pages)
├── js/
│   └── script.js             ← all behaviour (shared across pages)
├── assets/
│   └── images/
│       └── README.md         ← guide for adding real photos
├── netlify.toml               ← Netlify deploy config
├── .gitignore
└── README.md                  ← you are here
```

Every page loads the same `css/style.css` and `js/script.js`, so editing
either file updates the whole site at once.

---

## Pages

| Page | What's on it |
|---|---|
| **Home** (`index.html`) | Hero, stats counter, marquee, gallery teaser, packages preview, testimonials, CTA |
| **About** (`about.html`) | Studio history timeline, founder/team cards, values, stats |
| **Services** (`services.html`) | Icon-based list of everything offered: Photography, Videography, Drone Coverage, DJ & Sound System, Wedding Decor, Album Design, Same-Day Edit, Commercial Shoots, Event Coordination |
| **Packages** (`packages.html`) | The 4 priced bundles (Essential / Signature / Cinematic / Commercial) + add-ons + testimonials |
| **Gallery** (`gallery.html`) | 9-photo contact-sheet gallery with category filter and lightbox |
| **Contact** (`contact.html`) | 3 contact-info cards + the booking form |

### Why Services and Packages are separate pages
**Services** answers "what can you do for me" — a fast, scannable list
(good for someone still deciding what they need). **Packages** answers
"how much does it cost" — the priced bundles people compare once
they're ready to book. Keeping these separate means each page can stay
focused and not overwhelm a first-time visitor with prices before
they've understood what's included.

---

## Icons vs. real photos — where to use which

- **Services page:** uses icons, intentionally. This page is about
  scanning a list quickly — icons read faster than photos here, and
  they look consistent even before every service has its own portfolio
  photos. Avoid stock/internet photos here — generic stock images can
  make a service list feel less trustworthy, not more.
- **Gallery & About page:** these need **real photos** — they're your
  proof of work and your human story. Stock photos here would
  undermine trust the moment a client realizes a photo wasn't actually
  yours.
- **Founder/team cards:** placeholder silhouette icons for now: swap
  in real photos as soon as you have them (see
  `assets/images/README.md`).

---

## Animation & interaction systems

All of this lives in `js/script.js`, organized into numbered sections:

1. **Camera-iris page transition** — circular wipe between pages.
2. **Custom cursor** — dot + ring that follows the mouse on desktop.
3. **Word-by-word hero title reveal** — staggered on homepage load.
4. **3D tilt-on-hover** — service, package, testimonial, team, and
   add-on cards all tilt subtly toward the cursor (desktop only).
5. **Scroll parallax** — hero photo and gallery photos move at a
   slightly different speed than the page scroll.
6. **Animated number counters** — stats strip counts up once visible.
7. **Gallery filter animation** — fade out/in with stagger on filter click.
8. **Scroll-reveal** — sections fade/slide into view, staggered for
   grid items (cards).
9. **WhatsApp button pulse** — soft glow draws the eye to the floating button.

All animations respect `prefers-reduced-motion` and disable automatically
if a visitor has that accessibility setting on.

---

## Booking form: WhatsApp + Netlify Forms backup

The form on `contact.html` does two things on submit:

1. **Opens WhatsApp** with a pre-filled message containing all the
   visitor's details.
2. **Saves a backup copy** to Netlify Forms automatically — no backend
   code needed.

### Viewing submissions
Netlify dashboard → your site → **Forms** tab. Turn on email
notifications: **Forms → Settings → Form notifications → Add
notification → Email notification.**

---

## Things to edit before going fully live

| What | Where | How |
|---|---|---|
| WhatsApp number | `js/script.js`, top of file | Replace `WHATSAPP_NUMBER = "923000000000"` |
| Real photos | `assets/images/` | See `assets/images/README.md` |
| Studio history | `about.html`, `.timeline` section | Replace the 4 "EDIT ME — Year" entries with real milestones |
| Founders/team | `about.html`, `.team-grid` section | Replace names, roles, bios, and photos for each person |
| Stats (years/events/rating) | `index.html` and `about.html`, `<section class="stats-strip">` | Update the `data-target` numbers |
| Services list | `services.html`, `.service-list-grid` | Edit/add/remove `.service-icon-card` blocks |
| Pricing/packages | `packages.html` (and the preview copy on `index.html`) | Edit `service-price` / `service-list` text |
| Add-ons | `packages.html`, `.addons-grid` | Edit the 4 `addon-card` blocks |
| Testimonials | Home, Packages pages | Replace placeholder quotes/names/events |
| Social links | Footer on every page | Update the four `href="#"` links |
| Address/phone/email | Footer on every page + `contact.html` info cards | Replace placeholder text |

**Tip:** since the footer and nav repeat on all 6 pages, use
find-and-replace across files for anything site-wide (like the phone
number), so you don't miss a page.

---

## How to deploy updates

Since the live site is already connected at
`https://mohsinstudiopk.netlify.app/`:

- **If connected to a Git repo:** push your changes, Netlify redeploys
  automatically.
- **If using drag-and-drop:** zip this whole folder and drag the `.zip`
  itself onto your site's **Deploys** tab in the Netlify dashboard.

## Local preview (optional)

No install needed — open `index.html` directly in a browser. The
WhatsApp button, gallery, filters, and animations all work locally;
Netlify Forms backup-saving only works once deployed on Netlify.

---

## Feature wishlist — what else could be added

### Easy additions (mostly content/config)
- **Google Maps embed** on the Contact page
- **FAQ accordion** on the Packages page — turnaround time, advance %, travel charges
- **"As featured in" / client logos strip**
- **Open Graph meta tags** for nicer WhatsApp/Facebook link previews
- **Multi-language toggle** (English / Urdu)

### Medium effort (new UI, still no backend)
- **Masonry-style gallery** for natural photo aspect ratios
- **Video showcase section** — embed a highlight reel
- **Before/after slider** for editing comparisons
- **Sticky "Book Now" mini-bar** after scrolling past the hero

### Bigger projects (Stage 2 — needs a backend)
- **Admin panel** for managing bookings and availability
- **Bookings saved to MySQL** (matching your desktop app's database)
- **Client photo delivery portal**
- **Online payments** for booking advances
- **Availability calendar** clients can self-check

If/when you want a "bigger project," that's worth planning as its own
task — a small Node.js or PHP backend with MySQL, hosted free on
Render or Railway, with this site's frontend staying mostly as-is.
