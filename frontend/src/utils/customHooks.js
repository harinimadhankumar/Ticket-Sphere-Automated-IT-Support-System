import { useState, useCallback } from 'react';
import { handleApiError } from './errorHandler';
import { showToast } from './toastConfig';

/**
 * Generic data fetching hook with error handling
 */
export const useFetch = (fetchFunction, initialState = null) => {
    const [data, setData] = useState(initialState);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    const execute = useCallback(async (...args) => {
        try {
            setLoading(true);
            setError(null);
            const result = await fetchFunction(...args);
            setData(result);
            return result;
        } catch (err) {
            const message = handleApiError(err, 'useFetch');
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, [fetchFunction]);

    const reset = useCallback(() => {
        setData(initialState);
        setError(null);
    }, [initialState]);

    const refresh = useCallback(async (...args) => {
        return execute(...args);
    }, [execute]);

    return { data, loading, error, execute, reset, refresh };
};

/**
 * Hook for management dashboard data
 */
export const useManagementDashboard = () => {
    const [dashboard, setDashboard] = useState({
        tickets: [],
        engineers: [],
        categories: {},
        stats: {},
    });
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const loadDashboard = useCallback(async (fetchFunction) => {
        try {
            setLoading(true);
            setError(null);
            const data = await fetchFunction();
            setDashboard(data);
            return data;
        } catch (err) {
            const message = handleApiError(err, 'useManagementDashboard');
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    return { dashboard, loading, error, loadDashboard };
};

/**
 * Hook for paginated data with filters
 */
export const usePaginatedData = (initialFilters = {}) => {
    const [data, setData] = useState([]);
    const [filteredData, setFilteredData] = useState([]);
    const [loading, setLoading] = useState(false);
    const [filters, setFilters] = useState(initialFilters);
    const [searchQuery, setSearchQuery] = useState('');

    const applyFilters = useCallback((items, filterConfig) => {
        let result = items;

        // Apply each filter
        Object.entries(filterConfig).forEach(([key, value]) => {
            if (value && value !== 'all') {
                result = result.filter(item => {
                    const itemValue = item[key];
                    if (Array.isArray(itemValue)) {
                        return itemValue.includes(value);
                    }
                    return String(itemValue).toLowerCase() === String(value).toLowerCase();
                });
            }
        });

        // Apply search
        if (searchQuery) {
            const q = searchQuery.toLowerCase();
            result = result.filter(item =>
                Object.values(item).some(val =>
                    String(val).toLowerCase().includes(q)
                )
            );
        }

        return result;
    }, [searchQuery]);

    const updateData = useCallback((newData) => {
        setData(newData);
        setFilteredData(applyFilters(newData, filters));
    }, [applyFilters, filters]);

    const updateFilters = useCallback((newFilters) => {
        setFilters(newFilters);
        setFilteredData(applyFilters(data, newFilters));
    }, [applyFilters, data]);

    return {
        data,
        filteredData,
        loading,
        filters,
        searchQuery,
        setLoading,
        updateData,
        updateFilters,
        setSearchQuery,
    };
};

/**
 * Hook for modal state management
 */
export const useModal = (initialState = false) => {
    const [isOpen, setIsOpen] = useState(initialState);
    const [data, setData] = useState(null);

    const open = useCallback((modalData = null) => {
        setData(modalData);
        setIsOpen(true);
    }, []);

    const close = useCallback(() => {
        setIsOpen(false);
        setData(null);
    }, []);

    const toggle = useCallback(() => {
        setIsOpen(prev => !prev);
    }, []);

    return { isOpen, data, open, close, toggle, setData };
};

/**
 * Hook for form state management with validation
 */
export const useForm = (initialValues, onSubmit, validate = null) => {
    const [values, setValues] = useState(initialValues);
    const [errors, setErrors] = useState({});
    const [touched, setTouched] = useState({});
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handleChange = useCallback((e) => {
        const { name, value } = e.target;
        setValues(prev => ({ ...prev, [name]: value }));
        // Clear error when user starts typing
        if (errors[name]) {
            setErrors(prev => ({ ...prev, [name]: null }));
        }
    }, [errors]);

    const handleBlur = useCallback((e) => {
        const { name } = e.target;
        setTouched(prev => ({ ...prev, [name]: true }));
    }, []);

    const handleSubmit = useCallback(async (e) => {
        e.preventDefault();
        setIsSubmitting(true);

        try {
            // Validate if validator provided
            if (validate) {
                const validationErrors = validate(values);
                if (Object.keys(validationErrors).length > 0) {
                    setErrors(validationErrors);
                    setIsSubmitting(false);
                    return;
                }
            }

            await onSubmit(values);
            setValues(initialValues);
            setTouched({});
        } catch (error) {
            console.error('Form submission error:', error);
        } finally {
            setIsSubmitting(false);
        }
    }, [values, onSubmit, validate, initialValues]);

    const reset = useCallback(() => {
        setValues(initialValues);
        setErrors({});
        setTouched({});
    }, [initialValues]);

    return {
        values,
        errors,
        touched,
        isSubmitting,
        handleChange,
        handleBlur,
        handleSubmit,
        reset,
        setValues,
        setErrors,
    };
};
