/**
 * Standardized API response normalization
 * Handles inconsistent backend response structures
 */

export const normalizeTicketResponse = (data) => {
    if (!data) return [];

    // If data is already an array
    if (Array.isArray(data)) return data;

    // Try multiple nested paths
    if (Array.isArray(data.tickets)) return data.tickets;
    if (Array.isArray(data.data?.tickets)) return data.data.tickets;
    if (Array.isArray(data.items)) return data.items;
    if (Array.isArray(data.data?.items)) return data.data.items;
    if (Array.isArray(data.data)) return data.data;

    return [];
};

export const normalizeEngineerResponse = (data) => {
    if (!data) return [];

    if (Array.isArray(data)) return data;
    if (Array.isArray(data.engineers)) return data.engineers;
    if (Array.isArray(data.data?.engineers)) return data.data.engineers;
    if (Array.isArray(data.users)) return data.users;
    if (Array.isArray(data.data?.users)) return data.data.users;
    if (Array.isArray(data.data)) return data.data;

    return [];
};

export const normalizeCategoryResponse = (data) => {
    if (!data) return {};

    // If already an object with categories
    if (data.categories && typeof data.categories === 'object') return data.categories;
    if (data.data?.categories && typeof data.data.categories === 'object') return data.data.categories;

    // If data itself is the categories object
    if (typeof data === 'object' && !Array.isArray(data)) return data;
    if (data.data && typeof data.data === 'object' && !Array.isArray(data.data)) return data.data;

    return {};
};

export const normalizeSLAResponse = (data) => {
    if (!data) return {};

    if (data.sla) return data.sla;
    if (data.data?.sla) return data.data.sla;
    if (data.slaByCriteria) return data.slaByCriteria;
    if (data.data?.slaByCriteria) return data.data.slaByCriteria;

    return data;
};

export const normalizeKnowledgeBaseResponse = (data) => {
    if (!data) return [];

    if (Array.isArray(data)) return data;
    if (Array.isArray(data.articles)) return data.articles;
    if (Array.isArray(data.data?.articles)) return data.data.articles;
    if (Array.isArray(data.content)) return data.content;
    if (Array.isArray(data.data)) return data.data;

    return [];
};

export const normalizeStatsResponse = (data) => {
    if (!data) return {};

    if (data.stats) return data.stats;
    if (data.data?.stats) return data.data.stats;
    if (data.data) return data.data;

    return data;
};

/**
 * Extract nested data with multiple fallback paths
 */
export const safeExtract = (obj, paths, defaultValue = null) => {
    for (const path of paths) {
        let current = obj;
        const keys = path.split('.');
        let found = true;

        for (const key of keys) {
            if (current && typeof current === 'object' && key in current) {
                current = current[key];
            } else {
                found = false;
                break;
            }
        }

        if (found && current !== undefined) {
            return current;
        }
    }

    return defaultValue;
};

/**
 * Validate response data has required fields
 */
export const validateResponseData = (data, requiredFields) => {
    if (!data) return false;

    for (const field of requiredFields) {
        if (!(field in data)) {
            console.warn(`Missing required field: ${field}`);
            return false;
        }
    }

    return true;
};

/**
 * Convert object to array with validation
 */
export const objectToArray = (obj, keyField = 'category', valueField = 'count') => {
    if (!obj || typeof obj !== 'object' || Array.isArray(obj)) {
        return [];
    }

    return Object.entries(obj).map(([key, value]) => ({
        [keyField]: key || 'GENERAL',
        [valueField]: Number.isFinite(value) ? value : 0,
    })).filter(item => item[valueField] > 0);
};
