import { showToast } from './toastConfig';

/**
 * Centralized HTTP error handler
 * Maps HTTP status codes and error types to user-friendly messages
 */
export const handleApiError = (error, context = '') => {
    console.error(`[${context}] Error:`, error);

    // Network errors
    if (!error.response && error.message === 'Failed to fetch') {
        const message = 'Network connection problem. Please check your internet connection.';
        showToast.error(message);
        return message;
    }

    // HTTP status code errors
    if (error.status === 401 || error.response?.status === 401) {
        const message = 'Your session has expired. Please login again.';
        showToast.error(message);
        sessionStorage.clear();
        window.location.href = '/login';
        return message;
    }

    if (error.status === 403 || error.response?.status === 403) {
        const message = 'You do not have permission to perform this action.';
        showToast.error(message);
        return message;
    }

    if (error.status === 404 || error.response?.status === 404) {
        const message = 'The requested resource was not found.';
        showToast.error(message);
        return message;
    }

    if (error.status === 500 || error.response?.status === 500) {
        const message = 'Server error occurred. Please try again later.';
        showToast.error(message);
        return message;
    }

    if (error.status >= 500 || error.response?.status >= 500) {
        const message = 'Server error occurred. Please try again later.';
        showToast.error(message);
        return message;
    }

    // Custom error messages
    if (error.message) {
        showToast.error(error.message);
        return error.message;
    }

    // Generic fallback
    const message = 'An error occurred. Please try again.';
    showToast.error(message);
    return message;
};

/**
 * Standardized API fetch with centralized error handling
 */
export const fetchApi = async (url, options = {}) => {
    try {
        const token = sessionStorage.getItem('sessionToken');
        const headers = {
            'Content-Type': 'application/json',
            ...options.headers,
        };

        if (token) {
            headers['X-Session-Token'] = token;
        }

        const response = await fetch(url, {
            ...options,
            headers,
        });

        // Handle unauthorized
        if (response.status === 401) {
            sessionStorage.clear();
            window.location.href = '/login';
            throw new Error('Session expired');
        }

        // Handle non-ok responses
        if (!response.ok) {
            const error = new Error(`HTTP ${response.status}`);
            error.status = response.status;
            error.response = response;
            throw error;
        }

        const data = await response.json();
        return { success: true, data };
    } catch (error) {
        return { success: false, error };
    }
};

/**
 * Map backend error responses to user messages
 */
export const getApiErrorMessage = (error) => {
    if (typeof error === 'string') return error;
    if (error.message) return error.message;
    if (error.error) return error.error;
    return 'An error occurred. Please try again.';
};

/**
 * Validate API response structure
 */
export const validateApiResponse = (data, expectedStructure) => {
    if (!data) return false;
    if (Array.isArray(expectedStructure)) {
        return Array.isArray(data);
    }
    if (typeof expectedStructure === 'object') {
        return typeof data === 'object' && !Array.isArray(data);
    }
    return typeof data === expectedStructure;
};
