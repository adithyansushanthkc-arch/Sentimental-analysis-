/**
 * main.js – Global UI enhancements for the Sentiment Analysis System
 */

// Auto-dismiss alerts after 6 seconds
document.addEventListener('DOMContentLoaded', function () {
    const alerts = document.querySelectorAll('.alert.alert-success:not(.alert-dismissible), .alert.alert-info:not(.alert-dismissible)');
    alerts.forEach(function (alert) {
        setTimeout(function () {
            const bsAlert = bootstrap.Alert.getOrCreateInstance(alert);
            if (bsAlert) bsAlert.close();
            else alert.style.opacity = '0';
        }, 6000);
    });

    // Add fade-in animation to page content
    const animatableRows = document.querySelectorAll('.row > .col-md-3, .row > .col-md-4, .row > .col-md-6, .row > .col-md-7, .row > .col-md-5');
    animatableRows.forEach(function (el, i) {
        el.style.animationDelay = (i * 0.06) + 's';
        el.classList.add('animate-in');
    });

    // Confirm delete buttons
    document.querySelectorAll('[data-confirm]').forEach(function (el) {
        el.addEventListener('click', function (e) {
            if (!confirm(el.dataset.confirm || 'Are you sure?')) {
                e.preventDefault();
            }
        });
    });

    // Upload button loading state
    const uploadBtn = document.getElementById('uploadBtn');
    if (uploadBtn) {
        uploadBtn.closest('form') && uploadBtn.closest('form').addEventListener('submit', function () {
            uploadBtn.disabled = true;
            uploadBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Uploading...';
        });
    }

    // Active link highlight based on current path (for nested pages)
    const currentPath = window.location.pathname;
    document.querySelectorAll('.nav-link').forEach(function (link) {
        if (link.getAttribute('href') && currentPath.startsWith(link.getAttribute('href'))) {
            link.classList.add('active');
        }
    });

    // Tooltip initialization
    const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]');
    tooltipTriggerList.forEach(function (el) {
        new bootstrap.Tooltip(el);
    });
});
