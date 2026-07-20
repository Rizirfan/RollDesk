/* ========================================
   RollDesk Landing — Interactions
   ======================================== */

document.addEventListener('DOMContentLoaded', () => {

    // ── Hero tagline typing cursor animation ──
    const tagline = document.querySelector('.tagline-text');
    const cursor = document.querySelector('.cursor');
    if (tagline && cursor) {
        const fullText = tagline.getAttribute('data-text');
        let charIndex = 0;

        // Show cursor blinking first, then start typing
        setTimeout(() => {
            cursor.classList.add('blink');

            const typeInterval = setInterval(() => {
                if (charIndex < fullText.length) {
                    tagline.textContent += fullText[charIndex];
                    charIndex++;
                } else {
                    clearInterval(typeInterval);
                    // Cursor keeps blinking after typing completes
                }
            }, 65);
        }, 2800);
    }

    // ── Navbar scroll effect ──
    const navbar = document.getElementById('navbar');
    let lastScroll = 0;

    window.addEventListener('scroll', () => {
        const scrollY = window.scrollY;
        if (scrollY > 20) {
            navbar.classList.add('scrolled');
        } else {
            navbar.classList.remove('scrolled');
        }
        lastScroll = scrollY;
    }, { passive: true });

    // ── Mobile menu toggle ──
    const navToggle = document.getElementById('navToggle');
    const navLinks = document.getElementById('navLinks');

    if (navToggle && navLinks) {
        navToggle.addEventListener('click', () => {
            navLinks.classList.toggle('open');
            navToggle.classList.toggle('active');
        });

        navLinks.querySelectorAll('a').forEach(link => {
            link.addEventListener('click', () => {
                navLinks.classList.remove('open');
                navToggle.classList.remove('active');
            });
        });
    }

    // ── Smooth scroll for anchor links ──
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', (e) => {
            const target = document.querySelector(anchor.getAttribute('href'));
            if (target) {
                e.preventDefault();
                const offset = navbar.offsetHeight + 16;
                const top = target.getBoundingClientRect().top + window.scrollY - offset;
                window.scrollTo({ top, behavior: 'smooth' });
            }
        });
    });

    // ── Feature cards reveal on scroll ──
    const featureCards = document.querySelectorAll('.feature-card');

    const observerOptions = {
        threshold: 0.1,
        rootMargin: '0px 0px -40px 0px'
    };

    const cardObserver = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const delay = entry.target.getAttribute('data-delay') || 0;
                setTimeout(() => {
                    entry.target.classList.add('visible');
                }, delay * 80);
                cardObserver.unobserve(entry.target);
            }
        });
    }, observerOptions);

    featureCards.forEach(card => cardObserver.observe(card));

    // ── Animated stat counter ──
    const statValues = document.querySelectorAll('.stat-value');

    const statsObserver = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const el = entry.target;
                const target = parseInt(el.getAttribute('data-target'));
                animateCounter(el, 0, target, 1200);
                statsObserver.unobserve(el);
            }
        });
    }, { threshold: 0.5 });

    statValues.forEach(el => statsObserver.observe(el));

    function animateCounter(el, start, end, duration) {
        const startTime = performance.now();

        function update(currentTime) {
            const elapsed = currentTime - startTime;
            const progress = Math.min(elapsed / duration, 1);

            // Ease out cubic
            const eased = 1 - Math.pow(1 - progress, 3);
            const current = Math.round(start + (end - start) * eased);

            el.textContent = current;

            if (progress < 1) {
                requestAnimationFrame(update);
            }
        }

        requestAnimationFrame(update);
    }

    // ── Parallax effect on hero shapes ──
    const heroShapes = document.querySelectorAll('.hero-shapes .shape');

    window.addEventListener('scroll', () => {
        const scrollY = window.scrollY;
        if (scrollY < 800) {
            heroShapes.forEach((shape, i) => {
                const speed = 0.02 + (i * 0.01);
                shape.style.transform = `translateY(${scrollY * speed}px)`;
            });
        }
    }, { passive: true });

    // ── Workflow steps reveal ──
    const workflowSteps = document.querySelectorAll('.workflow-step');

    const stepObserver = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.style.opacity = '1';
                entry.target.style.transform = 'translateY(0)';
                stepObserver.unobserve(entry.target);
            }
        });
    }, { threshold: 0.2 });

    workflowSteps.forEach((step, i) => {
        step.style.opacity = '0';
        step.style.transform = 'translateY(30px)';
        step.style.transition = `opacity 0.5s ease ${i * 0.15}s, transform 0.5s ease ${i * 0.15}s`;
        stepObserver.observe(step);
    });

    // ── Tech cards reveal ──
    const techCards = document.querySelectorAll('.tech-card');

    const techObserver = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.style.opacity = '1';
                entry.target.style.transform = 'translateY(0)';
                techObserver.unobserve(entry.target);
            }
        });
    }, { threshold: 0.1 });

    techCards.forEach((card, i) => {
        card.style.opacity = '0';
        card.style.transform = 'translateY(20px)';
        card.style.transition = `opacity 0.4s ease ${i * 0.08}s, transform 0.4s ease ${i * 0.08}s`;
        techObserver.observe(card);
    });

    // ── Docs cards reveal ──
    const docsCards = document.querySelectorAll('.docs-card');

    const docsObserver = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.style.opacity = '1';
                entry.target.style.transform = 'translateY(0)';
                docsObserver.unobserve(entry.target);
            }
        });
    }, { threshold: 0.1 });

    docsCards.forEach((card, i) => {
        card.style.opacity = '0';
        card.style.transform = 'translateY(20px)';
        card.style.transition = `opacity 0.4s ease ${i * 0.1}s, transform 0.4s ease ${i * 0.1}s`;
        docsObserver.observe(card);
    });

});
