import React, { createContext, useState, useContext, useCallback, useEffect } from 'react';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
    const [token, setToken] = useState(() => sessionStorage.getItem('sessionToken'));
    const [user, setUser] = useState(null);

    // Store token in sessionStorage whenever it changes
    useEffect(() => {
        if (token) {
            sessionStorage.setItem('sessionToken', token);
        } else {
            sessionStorage.removeItem('sessionToken');
        }
    }, [token]);

    const setAuthToken = useCallback((newToken) => {
        setToken(newToken);
    }, []);

    const logout = useCallback(() => {
        setToken(null);
        setUser(null);
        sessionStorage.clear();
        window.location.href = '/login';
    }, []);

    const getToken = useCallback(() => {
        return token || sessionStorage.getItem('sessionToken');
    }, [token]);

    const value = {
        token: getToken(),
        user,
        setUser,
        setAuthToken,
        logout,
        isAuthenticated: !!token,
    };

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth must be used within AuthProvider');
    }
    return context;
};
