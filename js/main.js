/**
 * ColorfulSound - Main JavaScript
 * Particle system, scroll animations, and interactivity
 */

document.addEventListener('DOMContentLoaded', () => {
    initParticles();
    initScrollAnimations();
    initNavbar();
    initPlatformTabs();
    initWaveformAnimation();
});

/* ============================================
   Particle System
   ============================================ */
function initParticles() {
    const container = document.getElementById('particles');
    if (!container) return;

    const colors = [
        '#ff0000', '#ff4400', '#ff8800', '#ffbb00', '#ffff00',
        '#88ff00', '#00ff00', '#00ff88', '#00ffff', '#0088ff',
        '#0000ff', '#4400ff', '#8800ff', '#ff00ff', '#ff0088'
    ];

    const particleCount = window.innerWidth < 768 ? 30 : 60;

    for (let i = 0; i < particleCount; i++) {
        createParticle(container, colors);
    }
}

function createParticle(container, colors) {
    const particle = document.createElement('div');
    particle.className = 'particle';

    const size = Math.random() * 6 + 2;
    const color = colors[Math.floor(Math.random() * colors.length)];
    const left = Math.random() * 100;
    const top = Math.random() * 100;
    const delay = Math.random() * 15;
    const duration = Math.random() * 10 + 10;

    particle.style.cssText = `
        width: ${size}px;
        height: ${size}px;
        background: ${color};
        left: ${left}%;
        top: ${top}%;
        animation-delay: -${delay}s;
        animation-duration: ${duration}s;
        box-shadow: 0 0 ${size * 2}px ${color}, 0 0 ${size * 4}px ${color};
        opacity: ${Math.random() * 0.5 + 0.3};
    `;

    container.appendChild(particle);
}

/* ============================================
   Scroll Animations (AOS Alternative)
   ============================================ */
function initScrollAnimations() {
    const observerOptions = {
        root: null,
        rootMargin: '0px',
        threshold: 0.1
    };

    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const delay = entry.target.dataset.aosDelay || 0;
                setTimeout(() => {
                    entry.target.classList.add('aos-animate');
                }, parseInt(delay));
            }
        });
    }, observerOptions);

    document.querySelectorAll('[data-aos]').forEach(el => {
        observer.observe(el);
    });
}

/* ============================================
   Navbar
   ============================================ */
function initNavbar() {
    const navbar = document.querySelector('.navbar');
    const navToggle = document.querySelector('.nav-toggle');
    const navMenu = document.querySelector('.nav-menu');

    // Scroll effect
    let lastScroll = 0;
    window.addEventListener('scroll', () => {
        const currentScroll = window.pageYOffset;

        if (currentScroll > 50) {
            navbar.classList.add('scrolled');
        } else {
            navbar.classList.remove('scrolled');
        }

        lastScroll = currentScroll;
    });

    // Mobile menu toggle
    if (navToggle && navMenu) {
        navToggle.addEventListener('click', () => {
            navMenu.classList.toggle('active');
            navToggle.classList.toggle('active');
        });

        // Close menu on link click
        navMenu.querySelectorAll('.nav-link').forEach(link => {
            link.addEventListener('click', () => {
                navMenu.classList.remove('active');
                navToggle.classList.remove('active');
            });
        });
    }

    // Smooth scroll for anchor links
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function(e) {
            const href = this.getAttribute('href');
            if (href !== '#') {
                e.preventDefault();
                const target = document.querySelector(href);
                if (target) {
                    const navHeight = navbar.offsetHeight;
                    const targetPosition = target.offsetTop - navHeight;
                    window.scrollTo({
                        top: targetPosition,
                        behavior: 'smooth'
                    });
                }
            }
        });
    });
}

/* ============================================
   Platform Tabs
   ============================================ */
function initPlatformTabs() {
    const tabs = document.querySelectorAll('.platform-tab');
    const contents = document.querySelectorAll('.platform-content');

    tabs.forEach(tab => {
        tab.addEventListener('click', () => {
            const platform = tab.dataset.platform;

            // Update active tab
            tabs.forEach(t => t.classList.remove('active'));
            tab.classList.add('active');

            // Update active content
            contents.forEach(content => {
                content.classList.remove('active');
                if (content.id === platform) {
                    content.classList.add('active');
                }
            });
        });
    });
}

/* ============================================
   Waveform Animation Enhancement
   ============================================ */
function initWaveformAnimation() {
    const waveBars = document.querySelectorAll('.wave-bar');

    // Add random heights on beat
    function animateBeat() {
        waveBars.forEach(bar => {
            const randomHeight = Math.random() * 80 + 20;
            bar.style.height = `${randomHeight}px`;
        });
    }

    // Animate every 100ms for smooth effect
    setInterval(animateBeat, 100);
}

/* ============================================
   Interactive Effects
   ============================================ */

// Mouse glow effect on cards
document.addEventListener('mousemove', (e) => {
    const cards = document.querySelectorAll('.feature-card, .step-card, .demo-card');

    cards.forEach(card => {
        const rect = card.getBoundingClientRect();
        const x = e.clientX - rect.left;
        const y = e.clientY - rect.top;

        card.style.setProperty('--mouse-x', `${x}px`);
        card.style.setProperty('--mouse-y', `${y}px`);
    });
});

// Parallax effect for hero
window.addEventListener('scroll', () => {
    const scrolled = window.pageYOffset;
    const hero = document.querySelector('.hero-content');

    if (hero && scrolled < window.innerHeight) {
        hero.style.transform = `translateY(${scrolled * 0.3}px)`;
        hero.style.opacity = 1 - (scrolled / (window.innerHeight * 0.8));
    }
});

/* ============================================
   Beat Pulse Effect on Scroll
   ============================================ */
let lastScrollTime = 0;
const scrollThreshold = 100;

window.addEventListener('scroll', () => {
    const now = Date.now();

    if (now - lastScrollTime > scrollThreshold) {
        const waveBars = document.querySelectorAll('.wave-bar');
        waveBars.forEach(bar => {
            bar.style.animation = 'none';
            bar.offsetHeight; // Trigger reflow
            bar.style.animation = null;
        });
        lastScrollTime = now;
    }
});

/* ============================================
   Typing Effect for Hero (Optional)
   ============================================ */
function typeWriter(element, text, speed = 50) {
    let i = 0;
    element.innerHTML = '';

    function type() {
        if (i < text.length) {
            element.innerHTML += text.charAt(i);
            i++;
            setTimeout(type, speed);
        }
    }

    type();
}

/* ============================================
   Color Spectrum Animation
   ============================================ */
function initSpectrumAnimation() {
    const markers = document.querySelectorAll('.color-marker');

    markers.forEach((marker, index) => {
        marker.addEventListener('mouseenter', () => {
            marker.style.transform = 'scale(1.2)';
            marker.querySelector('.color-label').style.color = getComputedStyle(marker).getPropertyValue('--color');
        });

        marker.addEventListener('mouseleave', () => {
            marker.style.transform = 'scale(1)';
            marker.querySelector('.color-label').style.color = '';
        });
    });
}

// Initialize spectrum animation if elements exist
if (document.querySelector('.color-marker')) {
    initSpectrumAnimation();
}

/* ============================================
   Performance Optimization
   ============================================ */

// Debounce function for scroll events
function debounce(func, wait = 10) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// Lazy load images
document.addEventListener('DOMContentLoaded', () => {
    const images = document.querySelectorAll('img[loading="lazy"]');

    if ('loading' in HTMLImageElement.prototype) {
        // Browser supports lazy loading
        images.forEach(img => {
            if (img.dataset.src) {
                img.src = img.dataset.src;
            }
        });
    } else {
        // Fallback for older browsers
        const imageObserver = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    const img = entry.target;
                    if (img.dataset.src) {
                        img.src = img.dataset.src;
                    }
                    imageObserver.unobserve(img);
                }
            });
        });

        images.forEach(img => imageObserver.observe(img));
    }
});

/* ============================================
   Console Easter Egg
   ============================================ */
console.log(`
%c   ██████╗ ██████╗ ██╗      ██████╗ ██████╗ ███████╗██╗   ██╗██╗
%c  ██╔════╝██╔═══██╗██║     ██╔═══██╗██╔══██╗██╔════╝██║   ██║██║
%c  ██║     ██║   ██║██║     ██║   ██║██████╔╝█████╗  ██║   ██║██║
%c  ██║     ██║   ██║██║     ██║   ██║██╔══██╗██╔══╝  ██║   ██║██║
%c  ╚██████╗╚██████╔╝███████╗╚██████╔╝██║  ██║██║     ╚██████╔╝███████╗
%c   ╚═════╝ ╚═════╝ ╚══════╝ ╚═════╝ ╚═╝  ╚═╝╚═╝      ╚═════╝ ╚══════╝
%c   ███████╗ ██████╗ ██╗   ██╗███╗   ██╗██████╗
%c   ██╔════╝██╔═══██╗██║   ██║████╗  ██║██╔══██╗
%c   ███████╗██║   ██║██║   ██║██╔██╗ ██║██║  ██║
%c   ╚════██║██║   ██║██║   ██║██║╚██╗██║██║  ██║
%c   ███████║╚██████╔╝╚██████╔╝██║ ╚████║██████╔╝
%c   ╚══════╝ ╚═════╝  ╚═════╝ ╚═╝  ╚═══╝╚═════╝

%cTransform your music into mesmerizing light shows!
%chttps://github.com/ayushmaanbhav/ColorfulSound
`,
    'color: #ff0000',
    'color: #ff8800',
    'color: #ffff00',
    'color: #00ff00',
    'color: #0088ff',
    'color: #4400ff',
    'color: #8800ff',
    'color: #ff00ff',
    'color: #ff0088',
    'color: #00ffff',
    'color: #00ff88',
    'color: #88ff00',
    'color: #00ffff; font-size: 14px;',
    'color: #888888; font-size: 12px;'
);
