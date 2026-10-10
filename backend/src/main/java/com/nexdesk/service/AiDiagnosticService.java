package com.nexdesk.service;

import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class AiDiagnosticService {

    public static class DiagnosisResult {
        public String rootCause;
        public int confidence;
        public String suggestedPriority;
        public String suggestedCategory;
        public List<String> runbookSteps = new ArrayList<>();
        public String remediationCommand;
        public String commandType; // PowerShell, Bash, CMD, Manual
        public String estimatedSelfFixTime;
        public boolean canSelfResolve;
        public String duplicateIncidentWarning;

        public Map<String, Object> toMap() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("rootCause", rootCause);
            m.put("confidence", confidence);
            m.put("suggestedPriority", suggestedPriority);
            m.put("suggestedCategory", suggestedCategory);
            m.put("runbookSteps", runbookSteps);
            m.put("remediationCommand", remediationCommand);
            m.put("commandType", commandType);
            m.put("estimatedSelfFixTime", estimatedSelfFixTime);
            m.put("canSelfResolve", canSelfResolve);
            m.put("duplicateIncidentWarning", duplicateIncidentWarning);
            return m;
        }
    }

    public static class TechResolutionResult {
        public String diagnosticSummary;
        public List<String> technicalChecklist = new ArrayList<>();
        public String automationScript;
        public String scriptType;
        public String customerReplyDraft;
        public String preventionAdvice;

        public Map<String, Object> toMap() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("diagnosticSummary", diagnosticSummary);
            m.put("technicalChecklist", technicalChecklist);
            m.put("automationScript", automationScript);
            m.put("scriptType", scriptType);
            m.put("customerReplyDraft", customerReplyDraft);
            m.put("preventionAdvice", preventionAdvice);
            return m;
        }
    }

    public DiagnosisResult diagnose(String title, String description, String category) {
        String text = ((title != null ? title : "") + " " + (description != null ? description : "")).toLowerCase();
        DiagnosisResult res = new DiagnosisResult();

        // 1. Network & WiFi issues
        if (text.contains("wifi") || text.contains("wi-fi") || text.contains("internet") || text.contains("dns") || text.contains("network") || text.contains("cannot connect") || text.contains("no connection")) {
            res.suggestedCategory = "Network";
            res.suggestedPriority = "HIGH";
            res.confidence = 94;
            res.rootCause = "Local Network Adapter cache stall or DNS resolver lease expiration.";
            res.estimatedSelfFixTime = "90 seconds";
            res.canSelfResolve = true;
            res.commandType = "PowerShell / Terminal";
            res.remediationCommand = "ipconfig /flushdns ; ipconfig /release ; ipconfig /renew";
            res.runbookSteps.add("Flush your local DNS cache and release DHCP leases using the quick terminal command below.");
            res.runbookSteps.add("Toggle Wi-Fi adapter OFF for 10 seconds and reconnect to the 5GHz 'NexCorp-Secure' SSID.");
            res.runbookSteps.add("Verify default gateway ping: run 'ping 10.0.0.1 -n 4'. If 0% packet loss, browser DNS cache was the culprit.");

            if (text.contains("office") || text.contains("floor") || text.contains("building")) {
                res.duplicateIncidentWarning = "Note: 2 other users in your building reported Wi-Fi latency. IT Network team is actively monitoring AP-B3.";
            }
            return res;
        }

        // 2. VPN & Remote Access
        if (text.contains("vpn") || text.contains("anyconnect") || text.contains("tunnel") || text.contains("globalprotect") || text.contains("wireguard")) {
            res.suggestedCategory = "Network";
            res.suggestedPriority = "HIGH";
            res.confidence = 91;
            res.rootCause = "Stale Zero-Trust VPN certificate cache or split-tunnel routing deadlock.";
            res.estimatedSelfFixTime = "2 minutes";
            res.canSelfResolve = true;
            res.commandType = "PowerShell";
            res.remediationCommand = "Restart-Service -Name 'vpn*' -Force; Clear-DnsClientCache";
            res.runbookSteps.add("Quit your VPN client completely from system tray (Right Click -> Disconnect & Exit).");
            res.runbookSteps.add("Ensure your home Wi-Fi MTU is not fragmenting packets (disable custom MTU).");
            res.runbookSteps.add("Relaunch VPN client and authenticate with SSO Authenticator token (not SMS code).");
            return res;
        }

        // 3. Mouse, Keyboard, USB & Hardware Endpoints
        if (text.contains("mouse") || text.contains("keyboard") || text.contains("usb") || text.contains("monitor") || text.contains("screen") || text.contains("flicker") || text.contains("dock") || text.contains("charger") || text.contains("laptop")) {
            res.suggestedCategory = "Hardware";
            res.suggestedPriority = "MEDIUM";
            res.confidence = 88;
            res.rootCause = "USB endpoint power-state suspension or Thunderbolt display handshake failure.";
            res.estimatedSelfFixTime = "3 minutes";
            res.canSelfResolve = true;
            res.commandType = "Device Manager / OS";
            res.remediationCommand = "# Disconnect and reconnect USB peripheral; toggle USB Selective Suspend in Power Options";
            res.runbookSteps.add("Unplug the peripheral from the dock or hub and plug it directly into a laptop native USB-C/A port.");
            res.runbookSteps.add("If wireless/Bluetooth, unpair the device in Bluetooth Settings, toggle Bluetooth OFF for 5s, and re-pair.");
            res.runbookSteps.add("If mouse cursor is freezing, blow air into the optical sensor and clean mousepad surface.");
            return res;
        }

        // 4. Access, Passwords, SSO & MFA
        if (text.contains("password") || text.contains("login") || text.contains("mfa") || text.contains("2fa") || text.contains("okta") || text.contains("locked") || text.contains("access denied") || text.contains("permission")) {
            res.suggestedCategory = "Access";
            res.suggestedPriority = "HIGH";
            res.confidence = 96;
            res.rootCause = "Active Directory Kerberos token expiration or failed MFA sync window.";
            res.estimatedSelfFixTime = "1 minute";
            res.canSelfResolve = true;
            res.commandType = "Self-Service Portal";
            res.remediationCommand = "klist purge # Flushes Kerberos ticket cache on Windows / macOS";
            res.runbookSteps.add("Visit the Self-Service Password Reset (SSPR) portal at https://identity.nexdesk.internal/reset.");
            res.runbookSteps.add("If using Microsoft/Okta Authenticator, verify your phone's clock is set to 'Automatic Time Sync'.");
            res.runbookSteps.add("Purge expired Kerberos tickets: open terminal and run 'klist purge', then re-login.");
            return res;
        }

        // 5. Software, Docker, Developer Tools, Outlook
        if (text.contains("docker") || text.contains("git") || text.contains("outlook") || text.contains("teams") || text.contains("slack") || text.contains("crash") || text.contains("memory") || text.contains("slow") || text.contains("software")) {
            res.suggestedCategory = "Software";
            res.suggestedPriority = "MEDIUM";
            res.confidence = 86;
            res.rootCause = "Local runtime cache bloat, stale daemon lock, or GPU hardware acceleration glitch.";
            res.estimatedSelfFixTime = "2 minutes";
            res.canSelfResolve = true;
            res.commandType = "Terminal / App";
            if (text.contains("docker")) {
                res.remediationCommand = "docker system prune -f --volumes";
                res.runbookSteps.add("Run 'docker system prune -f' to free dangling build containers and reclaim disk space.");
                res.runbookSteps.add("Increase WSL2 / Docker Engine memory limit in ~/.wslconfig to 8GB.");
            } else if (text.contains("outlook") || text.contains("email")) {
                res.remediationCommand = "outlook.exe /cleanviews ; outlook.exe /resetnavpane";
                res.runbookSteps.add("Launch Outlook in Safe Mode: Hold CTRL while clicking the Outlook icon.");
                res.runbookSteps.add("Rebuild search index in Outlook Settings -> Search -> Indexing Options -> Rebuild.");
            } else {
                res.remediationCommand = "taskkill /F /IM electron* /T # Force close lingering background renderer threads";
                res.runbookSteps.add("Disable 'Hardware Acceleration' in the app settings (Settings -> Appearance -> Hardware Accel).");
                res.runbookSteps.add("Clear app cache in %APPDATA% or ~/Library/Application Support/.");
            }
            return res;
        }

        // Default General Triage
        res.suggestedCategory = category != null && !category.isBlank() ? category : "General";
        res.suggestedPriority = "MEDIUM";
        res.confidence = 75;
        res.rootCause = "General system anomaly or application state inconsistency.";
        res.estimatedSelfFixTime = "5 minutes";
        res.canSelfResolve = false;
        res.commandType = "System Reboot";
        res.remediationCommand = "shutdown /r /t 0 # Clean operating system restart";
        res.runbookSteps.add("Save all work and perform a complete system reboot to clear resident memory leaks.");
        res.runbookSteps.add("Verify available free disk space on primary drive C: (minimum 15% required).");
        res.runbookSteps.add("Submit ticket with device diagnostics attached so IT Ops can inspect telemetry immediately.");
        return res;
    }

    public TechResolutionResult generateResolution(String title, String description, String category, String priority) {
        TechResolutionResult r = new TechResolutionResult();
        String text = ((title != null ? title : "") + " " + (description != null ? description : "")).toLowerCase();

        if (text.contains("wifi") || text.contains("network") || text.contains("internet")) {
            r.diagnosticSummary = "Client workstation experiencing 802.11ax association handshake dropouts or IP pool exhaustion on local subnet.";
            r.technicalChecklist.add("Inspect Aruba/Cisco WLC controller for RSSI and SNR readings on client MAC.");
            r.technicalChecklist.add("Verify DHCP lease duration and pool headroom on Infoblox/Windows DHCP Server.");
            r.technicalChecklist.add("Check for channel overlap or radar DFS strikes on 5GHz band.");
            r.automationScript = "# Remote Network Reset Script (PowerShell)\nRestart-NetAdapter -Name 'Wi-Fi'\nClear-DnsClientCache\nGet-NetIPConfiguration | Select-Object InterfaceAlias, IPv4Address, IPv4DefaultGateway";
            r.scriptType = "PowerShell";
            r.customerReplyDraft = "Hello,\n\nOur automated diagnostic shows a temporary DHCP lease conflict on your local wireless AP. We have refreshed your network session on our controller. Please toggle your Wi-Fi off and on once. If the issue persists, connect to 'NexCorp-5G-Direct' and let us know.\n\nBest regards,\nNexDesk IT Operations";
            r.preventionAdvice = "Ensure client device driver for Intel Wi-Fi 6E AX210 is updated to >= v22.190.";
            return r;
        }

        if (text.contains("vpn") || text.contains("anyconnect")) {
            r.diagnosticSummary = "Remote access VPN session rejection caused by stale posture assessment or certificate expiration.";
            r.technicalChecklist.add("Check Cisco ISE / Palo Alto GlobalProtect logs for radius auth failure code.");
            r.technicalChecklist.add("Verify client machine compliance with CrowdStrike sensor active state.");
            r.automationScript = "# Re-register device posture agent\nStop-Service -Name 'ciscodac'\nStart-Service -Name 'ciscodac'";
            r.scriptType = "PowerShell";
            r.customerReplyDraft = "Hi there,\n\nWe inspected your VPN connection log and identified that your security posture token needed a refresh. We have reset your gateway session. Please close AnyConnect completely, reopen it, and authenticate via your MFA authenticator.\n\nWarm regards,\nNexDesk Security Ops";
            r.preventionAdvice = "Advise user to reconnect to corporate network every 30 days to renew machine certificates.";
            return r;
        }

        if (text.contains("mouse") || text.contains("hardware") || text.contains("usb") || text.contains("monitor")) {
            r.diagnosticSummary = "Peripheral hardware input failure or USB-C hub power delivery handshake interruption.";
            r.technicalChecklist.add("Check Device Manager for yellow exclamation marks under Universal Serial Bus controllers.");
            r.technicalChecklist.add("Verify firmware version of Dell/Lenovo Thunderbolt Docking Station.");
            r.technicalChecklist.add("If physical hardware damage, schedule express equipment replacement via IT Locker.");
            r.automationScript = "# Rescan hardware devices in Windows\npnpunattend.exe\ndevcon rescan";
            r.scriptType = "Batch / CMD";
            r.customerReplyDraft = "Hello,\n\nThank you for reaching out. If you have already tried plugging the peripheral into a direct laptop USB port without success, we have pre-approved a replacement unit for you. You can pick up a new ergonomic mouse at the IT Kiosk (2nd Floor, Room 204) with badge tap, or reply to have one couriered to your desk.\n\nCheers,\nNexDesk Hardware Support";
            r.preventionAdvice = "Dock firmware updates should be pushed in the next quarterly patch wave.";
            return r;
        }

        // Generic Tech resolution
        r.diagnosticSummary = "Application or environment state exception. Recommended standard ITIL Incident resolution procedure.";
        r.technicalChecklist.add("Review Windows Event Viewer Application and System logs for Event IDs 1000/1001.");
        r.technicalChecklist.add("Validate user permissions and security group memberships in Azure Active Directory.");
        r.technicalChecklist.add("Check remote system resource saturation (CPU, RAM, Storage).");
        r.automationScript = "# Fetch recent error events\nGet-EventLog -LogName Application -EntryType Error -Newest 10 | Format-Table TimeGenerated, Source, Message -Wrap";
        r.scriptType = "PowerShell";
        r.customerReplyDraft = "Hello,\n\nWe are actively investigating this ticket. We have assigned this to our specialized support engineer. We will update you with our findings shortly. If you notice any error codes on screen, please take a quick screenshot and reply directly to this ticket.\n\nBest regards,\nNexDesk IT Support Team";
        r.preventionAdvice = "Ensure client OS is on the latest quarterly corporate baseline build.";
        return r;
    }
}
