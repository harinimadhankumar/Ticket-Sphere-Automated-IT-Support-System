import toast from 'react-hot-toast';

// Prevent duplicate toasts
let lastToastTime = {};

// Professional minimal white card toast - centered popup
export const showToast = {
    success: (message) => {
        // Prevent duplicate toasts within 500ms
        const key = `success-${message}`;
        const now = Date.now();
        if (lastToastTime[key] && now - lastToastTime[key] < 500) {
            return;
        }
        lastToastTime[key] = now;

        toast.success(message, {
            duration: 5000,
            position: 'top-center',
            style: {
                background: 'white',
                color: '#1E293B',
                fontSize: '14px',
                fontWeight: '500',
                padding: '16px 24px',
                borderRadius: '8px',
                boxShadow: '0 20px 60px rgba(0, 0, 0, 0.15)',
                zIndex: 9999,
                border: 'none',
            },
            icon: '',
        });
    },

    error: (message) => {
        // Prevent duplicate toasts within 500ms
        const key = `error-${message}`;
        const now = Date.now();
        if (lastToastTime[key] && now - lastToastTime[key] < 500) {
            return;
        }
        lastToastTime[key] = now;

        toast.error(message, {
            duration: 5000,
            position: 'top-center',
            style: {
                background: 'white',
                color: '#1E293B',
                fontSize: '14px',
                fontWeight: '500',
                padding: '16px 24px',
                borderRadius: '8px',
                boxShadow: '0 20px 60px rgba(0, 0, 0, 0.15)',
                zIndex: 9999,
                border: 'none',
            },
            icon: '',
        });
    },

    info: (message) => {
        // Prevent duplicate toasts within 500ms
        const key = `info-${message}`;
        const now = Date.now();
        if (lastToastTime[key] && now - lastToastTime[key] < 500) {
            return;
        }
        lastToastTime[key] = now;

        toast(message, {
            duration: 5000,
            position: 'top-center',
            style: {
                background: 'white',
                color: '#1E293B',
                fontSize: '14px',
                fontWeight: '500',
                padding: '16px 24px',
                borderRadius: '8px',
                boxShadow: '0 20px 60px rgba(0, 0, 0, 0.15)',
                zIndex: 9999,
                border: 'none',
            },
            icon: '',
        });
    },

    warning: (message) => {
        // Prevent duplicate toasts within 500ms
        const key = `warning-${message}`;
        const now = Date.now();
        if (lastToastTime[key] && now - lastToastTime[key] < 500) {
            return;
        }
        lastToastTime[key] = now;

        toast(message, {
            duration: 5000,
            position: 'top-center',
            style: {
                background: 'white',
                color: '#1E293B',
                fontSize: '14px',
                fontWeight: '500',
                padding: '16px 24px',
                borderRadius: '8px',
                boxShadow: '0 20px 60px rgba(0, 0, 0, 0.15)',
                zIndex: 9999,
                border: 'none',
            },
            icon: '',
        });
    },

    loading: (message) => {
        // Dismiss all existing toasts before showing loading
        toast.remove();

        return toast.loading(message, {
            position: 'top-center',
            style: {
                background: 'white',
                color: '#1E293B',
                fontSize: '14px',
                fontWeight: '500',
                padding: '16px 24px',
                borderRadius: '8px',
                boxShadow: '0 20px 60px rgba(0, 0, 0, 0.15)',
                zIndex: 9999,
                border: 'none',
            },
        });
    },

    dismiss: (toastId) => {
        if (toastId) {
            toast.dismiss(toastId);
        } else {
            toast.remove();
        }
    },
};

// User-friendly error messages mapping
export const getUserFriendlyMessage = (error) => {
    if (typeof error === 'string') {
        if (error.includes('Failed to update profile')) {
            return 'Unable to save profile changes. Please try again.';
        }
        if (error.includes('Session expired')) {
            return 'Your session has expired. Please login again.';
        }
        if (error.includes('Invalid')) {
            return 'Invalid information provided. Please check your input.';
        }
        if (error.includes('not found')) {
            return 'Resource not found. Please refresh and try again.';
        }
        if (error.includes('500') || error.includes('Internal Server')) {
            return 'Server error occurred. Please try again later.';
        }
        if (error.includes('Network') || error.includes('Failed to fetch')) {
            return 'Network connection problem. Please check your internet.';
        }
    }
    return 'Something went wrong. Please try again.';
};
