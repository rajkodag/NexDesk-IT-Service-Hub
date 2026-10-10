package com.nexdesk.service;

import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class SystemHealthService {

    private static final AtomicInteger deflectedCount = new AtomicInteger(148);

    public Map<String, Object> getStatus() {
        Map<String, Object> data = new LinkedHashMap<>();

        List<Map<String, Object>> services = new ArrayList<>();
        services.add(createService("Single Sign-On (SSO / Okta)", "Identity & Auth", "OPERATIONAL", "99.99%", "24ms"));
        services.add(createService("Zero-Trust VPN Gateway", "Network & Remote Access", "OPERATIONAL", "99.95%", "38ms"));
        services.add(createService("Corporate Email & Calendar", "Exchange / M365", "DEGRADED", "98.70%", "310ms"));
        services.add(createService("Cloud VPC & AWS Transit Hub", "Infrastructure", "OPERATIONAL", "100.0%", "12ms"));
        services.add(createService("Campus Wi-Fi 6E (All Floors)", "Local Network", "OPERATIONAL", "99.88%", "8ms"));
        services.add(createService("GitHub Enterprise & CI/CD", "DevOps & Tooling", "OPERATIONAL", "99.94%", "45ms"));

        data.put("overallStatus", "DEGRADED"); // Because email is degraded
        data.put("uptimeOverall", "99.91%");
        data.put("services", services);

        // Active corporate incident banner
        Map<String, Object> activeIncident = new LinkedHashMap<>();
        activeIncident.put("incidentId", "INC-402");
        activeIncident.put("title", "Microsoft 365 Exchange Mailbox Sync Latency");
        activeIncident.put("impact", "Users may experience a 3-5 minute delay receiving incoming emails.");
        activeIncident.put("status", "INVESTIGATING");
        activeIncident.put("startedAt", "Today at 09:15 AM EST");
        activeIncident.put("action", "IT Ops is coordinating with Microsoft Tier-3 Engineering. No individual tickets required.");
        data.put("activeIncident", activeIncident);

        data.put("deflectedTicketsCount", deflectedCount.get());
        data.put("aiResolutionSuccessRate", "84.2%");

        return data;
    }

    public int incrementDeflected() {
        return deflectedCount.incrementAndGet();
    }

    private Map<String, Object> createService(String name, String category, String status, String uptime, String latency) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", name);
        m.put("category", category);
        m.put("status", status);
        m.put("uptime", uptime);
        m.put("latency", latency);
        return m;
    }
}
