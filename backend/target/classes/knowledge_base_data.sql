-- ============================================================
-- PHASE 4: SELF-SERVICE RESOLUTION
-- Knowledge Base Sample Data
-- ============================================================
-- 
-- This SQL script creates sample knowledge base entries
-- for common IT issues that can be auto-resolved.
-- 
-- Run this script after the application starts (tables are created)
-- using MySQL Workbench or command line:
-- mysql -u root -p powergrid_tickets < knowledge_base_data.sql
-- ============================================================

-- Clear existing data (optional - comment out if you want to append)
-- TRUNCATE TABLE knowledge_base;

-- ============================================================
-- NETWORK CATEGORY SOLUTIONS
-- ============================================================

INSERT INTO knowledge_base (issue_type, issue_title, solution_steps, auto_closable, category, keywords, priority_rank, success_rate, usage_count, success_count, is_active, created_time, updated_time)
VALUES (
    'VPN',
    'VPN Connection Issues',
    '["Disconnect from any existing VPN connection", "Restart the Cisco AnyConnect client", "Clear your browser cache and cookies", "Check if your internet connection is working properly", "Try connecting to a different VPN server if available", "Restart your computer and try again", "If still not working, check if VPN credentials need to be updated"]',
    true,
    'NETWORK',
    'vpn,cisco,anyconnect,connection,connect,disconnect,timeout',
    10,
    0.85,
    0,
    0,
    true,
    NOW(),
    NOW()
);

INSERT INTO knowledge_base (issue_type, issue_title, solution_steps, auto_closable, category, keywords, priority_rank, success_rate, usage_count, success_count, is_active, created_time, updated_time)
VALUES (
    'WIFI',
    'WiFi/Wireless Connection Issues',
    '["Check if WiFi is enabled on your device", "Toggle WiFi off and on again", "Forget the current network and reconnect with credentials", "Move closer to the WiFi router/access point", "Restart your computer/device", "Check if other devices can connect to the same network", "Reset network settings: Settings → Network → Reset"]',
    true,
    'NETWORK',
    'wifi,wireless,wifi,signal,disconnect,slow,network',
    9,
    0.80,
    0,
    0,
    true,
    NOW(),
    NOW()
);

INSERT INTO knowledge_base (issue_type, issue_title, solution_steps, auto_closable, category, keywords, priority_rank, success_rate, usage_count, success_count, is_active, created_time, updated_time)
VALUES (
    'INTERNET',
    'Internet Access Issues',
    '["Check if your network cable is properly connected (for LAN)", "Open Command Prompt and run: ipconfig /release then ipconfig /renew", "Try accessing different websites to confirm if issue is general", "Clear DNS cache: Open CMD as Admin and run: ipconfig /flushdns", "Check if proxy settings are correct in browser", "Restart your router/modem if possible", "Contact IT if issue persists across all devices"]',
    true,
    'NETWORK',
    'internet,browsing,web,access,slow,connection',
    8,
    0.75,
    0,
    0,
    true,
    NOW(),
    NOW()
);

INSERT INTO knowledge_base (issue_type, issue_title, solution_steps, auto_closable, category, keywords, priority_rank, success_rate, usage_count, success_count, is_active, created_time, updated_time)
VALUES (
    'LAN',
    'LAN/Ethernet Connection Issues',
    '["Check if the ethernet cable is properly connected at both ends", "Try a different ethernet port on your device or wall socket", "Check for any visible damage on the cable", "Disable and re-enable the network adapter: Control Panel → Network → Change adapter settings", "Update network drivers: Device Manager → Network adapters → Update driver", "Try a different ethernet cable if available"]',
    true,
    'NETWORK',
    'lan,ethernet,cable,wired,port,network,adapter',
    7,
    0.82,
    0,
    0,
    true,
    NOW(),
    NOW()
);

-- ============================================================
-- ACCESS CATEGORY SOLUTIONS
-- ============================================================

INSERT INTO knowledge_base (issue_type, issue_title, solution_steps, auto_closable, category, keywords, priority_rank, success_rate, usage_count, success_count, is_active, created_time, updated_time)
VALUES (
    'PASSWORD',
    'Password Reset',
    '["Go to the Password Reset Portal: https://passwordreset.powergrid.in", "Click on Forgot Password or Reset Password", "Enter your Employee ID and registered email/mobile", "You will receive an OTP on your registered contact", "Enter the OTP and set a new password", "Password requirements: Minimum 8 characters, 1 uppercase, 1 number, 1 special character", "Wait 5 minutes before trying to login with new password"]',
    true,
    'ACCESS',
    'password,reset,forgot,change,expired,unlock',
    10,
    0.95,
    0,
    0,
    true,
    NOW(),
    NOW()
);

INSERT INTO knowledge_base (issue_type, issue_title, solution_steps, auto_closable, category, keywords, priority_rank, success_rate, usage_count, success_count, is_active, created_time, updated_time)
VALUES (
    'ACCOUNT_UNLOCK',
    'Account Unlock',
    '["Wait 30 minutes - accounts auto-unlock after lockout period", "If urgent, go to Self-Service Portal: https://selfservice.powergrid.in", "Select Account Unlock option", "Verify your identity using security questions or OTP", "Your account will be unlocked immediately", "Ensure CAPS LOCK is off before entering password", "If still locked, contact IT Helpdesk"]',
    true,
    'ACCESS',
    'locked,unlock,account,lockout,blocked,login,failed',
    9,
    0.90,
    0,
    0,
    true,
    NOW(),
    NOW()
);

INSERT INTO knowledge_base (issue_type, issue_title, solution_steps, auto_closable, category, keywords, priority_rank, success_rate, usage_count, success_count, is_active, created_time, updated_time)
VALUES (
    'PERMISSION',
    'Access Permission Request',
    '["This request requires manager approval", "Please fill the Access Request Form on ServiceNow portal", "Select the resource/folder/application you need access to", "Provide business justification for the access", "Submit the request - it will be routed to your manager", "Once approved, access will be granted within 24 hours", "You will receive email confirmation when access is granted"]',
    false,
    'ACCESS',
    'permission,access,folder,share,drive,restricted',
    5,
    0.0,
    0,
    0,
    true,
    NOW(),
    NOW()
);

-- ============================================================
-- SOFTWARE CATEGORY SOLUTIONS
-- ============================================================

INSERT INTO knowledge_base (issue_type, issue_title, solution_steps, auto_closable, category, keywords, priority_rank, success_rate, usage_count, success_count, is_active, created_time, updated_time)
VALUES (
    'LOGIN',
    'Application Login Issues',
    '["Clear browser cache and cookies: Ctrl+Shift+Delete", "Try logging in using Incognito/Private mode", "Ensure you are using the correct username format (e.g., employee@powergrid.in)", "Check if CAPS LOCK is enabled", "Try resetting your password using the Forgot Password option", "Disable any browser extensions that might interfere", "Try a different browser (Chrome, Edge, Firefox)"]',
    true,
    'SOFTWARE',
    'login,signin,authentication,credentials,access,failed',
    9,
    0.78,
    0,
    0,
    true,
    NOW(),
    NOW()
);

INSERT INTO knowledge_base (issue_type, issue_title, solution_steps, auto_closable, category, keywords, priority_rank, success_rate, usage_count, success_count, is_active, created_time, updated_time)
VALUES (
    'CRASH',
    'Application Crash/Freeze',
    '["Force close the application: Ctrl+Alt+Delete → Task Manager → End Task", "Restart your computer", "Check for application updates and install if available", "Run the application as Administrator: Right-click → Run as Administrator", "Clear application cache/temporary files", "Check if there is enough disk space (at least 10% free)", "Try repairing the installation from Control Panel → Programs"]',
    true,
    'SOFTWARE',
    'crash,freeze,hang,stuck,not responding,error,stopped',
    8,
    0.72,
    0,
    0,
    true,
    NOW(),
    NOW()
);

INSERT INTO knowledge_base (issue_type, issue_title, solution_steps, auto_closable, category, keywords, priority_rank, success_rate, usage_count, success_count, is_active, created_time, updated_time)
VALUES (
    'INSTALLATION',
    'Software Installation Request',
    '["Check if the software is available in Company Software Center", "Open Software Center from Start Menu", "Search for the required software", "Click Install and wait for completion", "If not available in Software Center, raise a request on ServiceNow", "Provide business justification for the software", "Note: Installation of unauthorized software is not permitted"]',
    false,
    'SOFTWARE',
    'install,setup,software,application,program,download',
    5,
    0.0,
    0,
    0,
    true,
    NOW(),
    NOW()
);

INSERT INTO knowledge_base (issue_type, issue_title, solution_steps, auto_closable, category, keywords, priority_rank, success_rate, usage_count, success_count, is_active, created_time, updated_time)
VALUES (
    'UPDATE',
    'Software Update Issues',
    '["Restart your computer to complete pending updates", "Check Windows Update: Settings → Update & Security → Windows Update", "For specific software, check Help → Check for Updates", "Ensure you have stable internet connection during update", "If update fails, try running Windows Update Troubleshooter", "Clear Windows Update cache: Stop wuauserv service, delete C:\\Windows\\SoftwareDistribution, restart service", "Contact IT if critical security updates are failing"]',
    true,
    'SOFTWARE',
    'update,upgrade,patch,version,outdated',
    7,
    0.70,
    0,
    0,
    true,
    NOW(),
    NOW()
);

INSERT INTO knowledge_base (issue_type, issue_title, solution_steps, auto_closable, category, keywords, priority_rank, success_rate, usage_count, success_count, is_active, created_time, updated_time)
VALUES (
    'PERFORMANCE',
    'Application/System Performance Issues',
    '["Close unnecessary applications and browser tabs", "Check Task Manager for high CPU/Memory usage: Ctrl+Shift+Esc", "End any processes using excessive resources", "Restart your computer to clear memory", "Run Disk Cleanup: Search for Disk Cleanup in Start Menu", "Disable startup programs: Task Manager → Startup tab", "Check for malware: Run Windows Defender scan", "Ensure at least 20% disk space is free"]',
    true,
    'SOFTWARE',
    'slow,performance,speed,lag,sluggish,freeze,memory',
    8,
    0.75,
    0,
    0,
    true,
    NOW(),
    NOW()
);

-- ============================================================
-- EMAIL CATEGORY SOLUTIONS
-- ============================================================

INSERT INTO knowledge_base (issue_type, issue_title, solution_steps, auto_closable, category, keywords, priority_rank, success_rate, usage_count, success_count, is_active, created_time, updated_time)
VALUES (
    'OUTLOOK',
    'Outlook Application Issues',
    '["Close Outlook completely", "Start Outlook in Safe Mode: Hold Ctrl while clicking Outlook icon", "If Safe Mode works, disable add-ins: File → Options → Add-ins → Manage COM Add-ins", "Repair Outlook: Control Panel → Programs → Microsoft Office → Change → Repair", "Delete and recreate Outlook profile: Control Panel → Mail → Show Profiles", "Check if OST file is too large (>50GB): Archive old emails", "Run SCANPST.exe to repair OST/PST files"]',
    true,
    'EMAIL',
    'outlook,email,client,crash,freeze,slow,not opening',
    9,
    0.80,
    0,
    0,
    true,
    NOW(),
    NOW()
);

INSERT INTO knowledge_base (issue_type, issue_title, solution_steps, auto_closable, category, keywords, priority_rank, success_rate, usage_count, success_count, is_active, created_time, updated_time)
VALUES (
    'EMAIL_SEND',
    'Cannot Send Emails',
    '["Check your internet connection", "Verify the recipient email address is correct", "Check if your Outbox has stuck emails - try resending", "Reduce attachment size (limit: 25MB for external)", "Check if you are not exceeding daily send limit", "Try sending from Outlook Web Access (OWA)", "Check if your account is not blocked: Contact IT if sending to external domains fails"]',
    true,
    'EMAIL',
    'send,sending,email,outbox,stuck,failed,delivery',
    8,
    0.75,
    0,
    0,
    true,
    NOW(),
    NOW()
);

INSERT INTO knowledge_base (issue_type, issue_title, solution_steps, auto_closable, category, keywords, priority_rank, success_rate, usage_count, success_count, is_active, created_time, updated_time)
VALUES (
    'EMAIL_RECEIVE',
    'Cannot Receive Emails',
    '["Check your Junk/Spam folder for missing emails", "Verify your mailbox is not full: Check quota in Outlook", "Click Send/Receive All Folders button", "Check if rules are moving emails to other folders", "Verify the sender is not blocked: Settings → Mail → Blocked Senders", "Try accessing email via Outlook Web Access", "Ask sender to resend with different subject line"]',
    true,
    'EMAIL',
    'receive,receiving,inbox,missing,email,not getting',
    8,
    0.70,
    0,
    0,
    true,
    NOW(),
    NOW()
);

INSERT INTO knowledge_base (issue_type, issue_title, solution_steps, auto_closable, category, keywords, priority_rank, success_rate, usage_count, success_count, is_active, created_time, updated_time)
VALUES (
    'CALENDAR',
    'Calendar/Meeting Issues',
    '["Refresh your calendar: F5 or Send/Receive", "Check if you have the correct time zone: File → Options → Calendar", "For meeting not showing: Ask organizer to resend invite", "Clear calendar cache: Close Outlook, delete content from %localappdata%\\Microsoft\\Outlook\\", "Check shared calendar permissions with the owner", "For recurring meeting issues: Delete and recreate the series", "Try accessing calendar via Outlook Web Access"]',
    true,
    'EMAIL',
    'calendar,meeting,invite,schedule,appointment,room,booking',
    7,
    0.72,
    0,
    0,
    true,
    NOW(),
    NOW()
);

-- ============================================================
-- HARDWARE CATEGORY SOLUTIONS
-- ============================================================

INSERT INTO knowledge_base (issue_type, issue_title, solution_steps, auto_closable, category, keywords, priority_rank, success_rate, usage_count, success_count, is_active, created_time, updated_time)
VALUES (
    'PRINTER',
    'Printer Issues',
    '["Check if printer is powered on and not showing errors", "Verify printer has paper and ink/toner", "Check cable connection (USB) or network connectivity", "Set as default printer: Settings → Devices → Printers", "Clear print queue: Services → Print Spooler → Stop → Delete files in C:\\Windows\\System32\\spool\\PRINTERS → Start", "Remove and re-add the printer", "Update printer driver from manufacturer website"]',
    true,
    'HARDWARE',
    'printer,print,printing,paper,jam,queue,offline',
    8,
    0.75,
    0,
    0,
    true,
    NOW(),
    NOW()
);

INSERT INTO knowledge_base (issue_type, issue_title, solution_steps, auto_closable, category, keywords, priority_rank, success_rate, usage_count, success_count, is_active, created_time, updated_time)
VALUES (
    'MONITOR',
    'Monitor/Display Issues',
    '["Check if monitor is powered on", "Verify cable connections (HDMI/VGA/DisplayPort)", "Try a different cable if available", "Adjust resolution: Right-click Desktop → Display Settings", "Update display drivers: Device Manager → Display adapters", "Connect to a different port on the laptop/PC", "Test monitor with a different device to isolate issue", "For external monitor not detected: Windows+P → Extend/Duplicate"]',
    true,
    'HARDWARE',
    'monitor,display,screen,blank,black,flickering,resolution',
    8,
    0.70,
    0,
    0,
    true,
    NOW(),
    NOW()
);

INSERT INTO knowledge_base (issue_type, issue_title, solution_steps, auto_closable, category, keywords, priority_rank, success_rate, usage_count, success_count, is_active, created_time, updated_time)
VALUES (
    'KEYBOARD',
    'Keyboard Issues',
    '["Check if keyboard is properly connected", "Try a different USB port", "Check if Num Lock/Caps Lock is affecting input", "Restart your computer", "Check keyboard language: Windows+Space to switch", "Update keyboard driver: Device Manager → Keyboards", "Test with On-Screen Keyboard: Start → On-Screen Keyboard", "For laptop: Check if Fn key is stuck or pressed"]',
    true,
    'HARDWARE',
    'keyboard,keys,typing,not working,stuck,layout',
    7,
    0.80,
    0,
    0,
    true,
    NOW(),
    NOW()
);

INSERT INTO knowledge_base (issue_type, issue_title, solution_steps, auto_closable, category, keywords, priority_rank, success_rate, usage_count, success_count, is_active, created_time, updated_time)
VALUES (
    'MOUSE',
    'Mouse Issues',
    '["Check if mouse is properly connected", "Try a different USB port", "For wireless mouse: Replace batteries or check Bluetooth connection", "Clean the mouse sensor and mouse pad", "Update mouse driver: Device Manager → Mice and other pointing devices", "Adjust mouse settings: Settings → Devices → Mouse", "Test with a different mouse to isolate the issue"]',
    true,
    'HARDWARE',
    'mouse,cursor,pointer,click,scroll,not moving,wireless',
    7,
    0.82,
    0,
    0,
    true,
    NOW(),
    NOW()
);

INSERT INTO knowledge_base (issue_type, issue_title, solution_steps, auto_closable, category, keywords, priority_rank, success_rate, usage_count, success_count, is_active, created_time, updated_time)
VALUES (
    'LAPTOP',
    'Laptop Issues',
    '["For boot issues: Hold power button for 10 seconds, then restart", "Check battery charge level", "Connect power adapter and verify charging LED", "Remove all external devices and try booting", "Try booting in Safe Mode: Hold Shift and click Restart", "Check for overheating: Ensure vents are not blocked", "For screen issues: Connect external monitor to test", "Hard reset: Power off, remove battery if possible, hold power 30 sec, reconnect and start"]',
    true,
    'HARDWARE',
    'laptop,notebook,boot,power,battery,charge,start',
    8,
    0.65,
    0,
    0,
    true,
    NOW(),
    NOW()
);

-- ============================================================
-- VERIFY INSERTED DATA
-- ============================================================

SELECT 
    id,
    issue_type,
    issue_title,
    auto_closable,
    category,
    priority_rank,
    is_active
FROM knowledge_base
ORDER BY category, priority_rank DESC;

-- Summary by category
SELECT 
    category,
    COUNT(*) as total_entries,
    SUM(CASE WHEN auto_closable = true THEN 1 ELSE 0 END) as auto_closable_count
FROM knowledge_base
WHERE is_active = true
GROUP BY category;
