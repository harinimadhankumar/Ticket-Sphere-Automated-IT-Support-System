import React from 'react';

export default function LoadingSpinner({ text = 'Loading...' }) {
    return (
        <div className="loading-overlay">
            <div className="loading-spinner"></div>
            <span className="loading-text">{text}</span>
        </div>
    );
}
