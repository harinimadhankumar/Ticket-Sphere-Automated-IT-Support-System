import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';

export default function Header({ showNav = false }) {
    const [istTime, setIstTime] = useState('');

    useEffect(() => {
        const updateTime = () => {
            const now = new Date();
            // Format: HH:MM:SS in 24-hour format for IST
            const timeString = now.toLocaleTimeString('en-GB', {
                timeZone: 'Asia/Kolkata',
                hour: '2-digit',
                minute: '2-digit',
                second: '2-digit'
            });
            setIstTime(timeString);
        };

        updateTime();
        const interval = setInterval(updateTime, 1000);
        return () => clearInterval(interval);
    }, []);

    return (
        <>
            <style>{`
                .header-brand {
                    text-decoration: none;
                    color: inherit;
                }
            `}</style>

            <header className="app-header">
                <div style={{ display: 'flex', alignItems: 'center', gap: 12, flexShrink: 0 }}>
                    <Link to="/" className="header-brand">
                        <div className="header-brand-icon">⚡</div>
                        <div className="header-brand-text">
                            <h1>PowerGrid ITSM</h1>
                            <span>IT Service Management</span>
                        </div>
                    </Link>
                </div>
                {/* Scrolling Banner */}
                <div className="header-banner">
                    <div className="banner-scroll">
                        <span className="banner-text">
                            Centralized IT Service Management   |   Real-time Ticket Tracking   |   SLA Compliance   |   Knowledge Base Integration   |   Advanced Escalation Management   |   Professional Support   |   Service Excellence   |   Centralized IT Service Management   |   Real-time Ticket Tracking   |   SLA Compliance   |   Knowledge Base Integration   |   Advanced Escalation Management   |   Professional Support   |   Service Excellence   |
                        </span>
                    </div>
                </div>
                <div className="header-right">
                    <div className="ist-clock">
                        <span className="ist-clock-text">{istTime} (IST)</span>
                    </div>
                </div>
            </header>
        </>
    );
}
