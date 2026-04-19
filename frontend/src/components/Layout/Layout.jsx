import React from 'react';
import { Toaster } from 'react-hot-toast';
import Header from './Header';
import Footer from './Footer';
import Sidebar from './Sidebar';
import SecondaryNav from './SecondaryNav';

export default function Layout({ children, showNav = true, showUser = true, showSidebar = false, showSecondaryNav = false }) {
    const headerHeight = 60;
    const secondaryNavHeight = showSecondaryNav ? 56 : 0;
    const sidebarWidth = showSidebar ? 260 : 0;
    const footerHeight = 48;
    const topOffset = headerHeight + secondaryNavHeight;

    return (
        <div style={{
            position: 'fixed',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            display: 'flex',
            flexDirection: 'column',
            backgroundColor: '#f5f7fa'
        }}>
            <Toaster
                position="top-center"
                reverseOrder={false}
                gutter={8}
                containerClassName="toast-container-center"
                toastOptions={{
                    duration: 5000,
                }}
            />
            {/* Fixed Header */}
            <Header showNav={showNav} showUser={showUser} />

            {/* Fixed Secondary Nav */}
            {showSecondaryNav && <SecondaryNav />}

            {/* Content Area - Header + Sidebar + Main + Footer */}
            <div style={{
                display: 'flex',
                flex: 1,
                overflow: 'hidden',
                position: 'relative'
            }}>
                {/* Fixed Sidebar */}
                {showSidebar && (
                    <div style={{
                        position: 'fixed',
                        left: 0,
                        top: topOffset,
                        width: `${sidebarWidth}px`,
                        height: `calc(100vh - ${topOffset}px - ${footerHeight}px)`,
                        zIndex: 50,
                        overflowY: 'auto',
                        overflowX: 'hidden',
                        backgroundColor: 'transparent'
                    }}>
                        <Sidebar />
                    </div>
                )}

                {/* Scrollable Main Content */}
                <main
                    className="main-content"
                    style={{
                        flex: 1,
                        marginLeft: `${sidebarWidth}px`,
                        overflow: 'auto',
                        overflowX: 'hidden'
                    }}
                >
                    {children}
                </main>
            </div>

            {/* Fixed Footer */}
            <Footer />
        </div>
    );
}
