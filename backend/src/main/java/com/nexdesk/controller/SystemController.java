package com.nexdesk.controller;

import com.nexdesk.dto.DashboardResponse;
import com.nexdesk.service.AiDiagnosticService;
import com.nexdesk.service.DashboardService;
import com.nexdesk.service.SystemHealthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@Tag(name = "System & AI Diagnostics", description = "Infrastructure health, self-healing diagnostics, and system telemetry")
public class SystemController {

    private final SystemHealthService systemHealthService;
    private final AiDiagnosticService aiDiagnosticService;
    private final DashboardService dashboardService;
    private final DataSource dataSource;

    public SystemController(SystemHealthService systemHealthService,
                            AiDiagnosticService aiDiagnosticService,
                            DashboardService dashboardService,
                            DataSource dataSource) {
        this.systemHealthService = systemHealthService;
        this.aiDiagnosticService = aiDiagnosticService;
        this.dashboardService = dashboardService;
        this.dataSource = dataSource;
    }

    @GetMapping("/api/health")
    @Operation(summary = "Health check and database connectivity probe")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new LinkedHashMap<>();
        health.put("status", "UP");
        health.put("application", "NexDesk Enterprise IT Service Hub");
        health.put("version", "2.0.0");
        health.put("timestamp", System.currentTimeMillis());

        boolean dbOk = false;
        try (Connection conn = dataSource.getConnection()) {
            dbOk = conn.isValid(2);
        } catch (Exception ignored) {
        }
        health.put("database", dbOk);

        return ResponseEntity.ok(health);
    }

    @GetMapping("/api/system/status")
    @Operation(summary = "Real-time infrastructure health and outage radar")
    public ResponseEntity<Map<String, Object>> getSystemStatus() {
        return ResponseEntity.ok(systemHealthService.getStatus());
    }

    @GetMapping("/api/analytics")
    @Operation(summary = "Service velocity analytics and KPIs (Frontend alias)")
    public ResponseEntity<DashboardResponse> getAnalytics() {
        return ResponseEntity.ok(dashboardService.getDashboardMetrics());
    }

    @PostMapping("/api/ai/diagnose")
    @Operation(summary = "AI Self-Healing Engine: Real-time issue diagnosis and runbook generation")
    public ResponseEntity<Map<String, Object>> diagnose(@RequestBody Map<String, String> body) {
        String title = body.getOrDefault("title", "");
        String description = body.getOrDefault("description", "");
        String category = body.getOrDefault("category", "General");

        AiDiagnosticService.DiagnosisResult result = aiDiagnosticService.diagnose(title, description, category);
        return ResponseEntity.ok(result.toMap());
    }

    @PostMapping("/api/ai/resolve")
    @Operation(summary = "AI Technician Assistant: Generate diagnostic checklist and fix script")
    public ResponseEntity<Map<String, Object>> generateResolution(@RequestBody Map<String, String> body) {
        String title = body.getOrDefault("title", "");
        String description = body.getOrDefault("description", "");
        String category = body.getOrDefault("category", "General");
        String priority = body.getOrDefault("priority", "MEDIUM");

        AiDiagnosticService.TechResolutionResult result = aiDiagnosticService.generateResolution(title, description, category, priority);
        return ResponseEntity.ok(result.toMap());
    }
}
