/**
 * Form validation utilities
 */

export const validateEmail = (email) => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
};

export const validatePassword = (password) => {
    // Min 6 characters
    return password && password.length >= 6;
};

export const validateUsername = (username) => {
    // Alphanumeric and underscores, 3-20 chars
    const usernameRegex = /^[a-zA-Z0-9_]{3,20}$/;
    return usernameRegex.test(username);
};

export const validatePhoneNumber = (phone) => {
    // Basic phone validation
    const phoneRegex = /^\+?[\d\s\-()]{10,}$/;
    return phoneRegex.test(phone);
};

/**
 * Engineer profile validation
 */
export const validateEngineerProfile = (data) => {
    const errors = {};

    if (!data.name || data.name.trim() === '') {
        errors.name = 'Name is required';
    }

    if (!data.email || !validateEmail(data.email)) {
        errors.email = 'Valid email is required';
    }

    if (!data.team || data.team.trim() === '') {
        errors.team = 'Team is required';
    }

    return errors;
};

/**
 * Engineer form validation
 */
export const validateEngineerForm = (data) => {
    const errors = {};

    if (!data.name || data.name.trim() === '') {
        errors.name = 'Name is required';
    }

    if (!data.email || !validateEmail(data.email)) {
        errors.email = 'Valid email is required';
    }

    if (!data.password || !validatePassword(data.password)) {
        errors.password = 'Password must be at least 6 characters';
    }

    if (!data.team || data.team.trim() === '') {
        errors.team = 'Team is required';
    }

    return errors;
};

/**
 * Manager form validation
 */
export const validateManagerForm = (data) => {
    const errors = {};

    if (!data.name || data.name.trim() === '') {
        errors.name = 'Name is required';
    }

    if (!data.username || !validateUsername(data.username)) {
        errors.username = 'Username must be 3-20 alphanumeric characters';
    }

    if (!data.email || !validateEmail(data.email)) {
        errors.email = 'Valid email is required';
    }

    if (!data.password || !validatePassword(data.password)) {
        errors.password = 'Password must be at least 6 characters';
    }

    if (!data.department || data.department.trim() === '') {
        errors.department = 'Department is required';
    }

    return errors;
};

/**
 * Department form validation
 */
export const validateDepartmentForm = (data) => {
    const errors = {};

    if (!data.code || data.code.trim() === '') {
        errors.code = 'Department code is required';
    }

    if (!data.name || data.name.trim() === '') {
        errors.name = 'Department name is required';
    }

    if (!data.description || data.description.trim() === '') {
        errors.description = 'Description is required';
    }

    return errors;
};

/**
 * Settings form validation
 */
export const validateSettingsForm = (data) => {
    const errors = {};

    if (data.email && !validateEmail(data.email)) {
        errors.email = 'Valid email is required';
    }

    if (data.phone && !validatePhoneNumber(data.phone)) {
        errors.phone = 'Valid phone number is required';
    }

    return errors;
};

/**
 * Ticket resolution validation
 */
export const validateTicketResolution = (data) => {
    const errors = {};

    if (!data.resolutionNotes || data.resolutionNotes.trim() === '') {
        errors.resolutionNotes = 'Resolution notes are required';
    }

    if (data.resolutionNotes && data.resolutionNotes.length < 10) {
        errors.resolutionNotes = 'Resolution notes must be at least 10 characters';
    }

    return errors;
};

/**
 * Escalation form validation
 */
export const validateEscalationForm = (data) => {
    const errors = {};

    if (!data.reason || data.reason.trim() === '') {
        errors.reason = 'Escalation reason is required';
    }

    if (!data.targetTeamLead || data.targetTeamLead === '') {
        errors.targetTeamLead = 'Please select a target team lead';
    }

    return errors;
};

/**
 * Progress note validation
 */
export const validateProgressNote = (note) => {
    const errors = {};

    if (!note || note.trim() === '') {
        errors.note = 'Progress note cannot be empty';
    }

    if (note && note.length < 5) {
        errors.note = 'Progress note must be at least 5 characters';
    }

    if (note && note.length > 1000) {
        errors.note = 'Progress note cannot exceed 1000 characters';
    }

    return errors;
};

/**
 * Generic required field validation
 */
export const validateRequired = (value, fieldName = 'This field') => {
    return value && value.toString().trim() !== ''
        ? null
        : `${fieldName} is required`;
};

/**
 * Generic min length validation
 */
export const validateMinLength = (value, minLength, fieldName = 'This field') => {
    if (!value) return null;
    return value.toString().length >= minLength
        ? null
        : `${fieldName} must be at least ${minLength} characters`;
};

/**
 * Generic max length validation
 */
export const validateMaxLength = (value, maxLength, fieldName = 'This field') => {
    if (!value) return null;
    return value.toString().length <= maxLength
        ? null
        : `${fieldName} cannot exceed ${maxLength} characters`;
};

/**
 * Generic date range validation
 */
export const validateDateRange = (startDate, endDate) => {
    const errors = {};

    if (!startDate) {
        errors.startDate = 'Start date is required';
    }

    if (!endDate) {
        errors.endDate = 'End date is required';
    }

    if (startDate && endDate && new Date(startDate) > new Date(endDate)) {
        errors.endDate = 'End date must be after start date';
    }

    return errors;
};
