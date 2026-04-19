import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import './WelcomePage.css';

export default function WelcomePage() {
    const navigate = useNavigate();

    useEffect(() => {
        const timer = setTimeout(() => {
            navigate('/login');
        }, 5000);
        return () => clearTimeout(timer);
    }, [navigate]);

    return (
        <div className="welcome-page">
            <img src="/Welcome.jpg" alt="Welcome" className="welcome-image" />
        </div>
    );
}
