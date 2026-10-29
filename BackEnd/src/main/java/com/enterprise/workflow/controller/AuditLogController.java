package com.enterprise.workflow.controller;

import com.enterprise.workflow.entity.AuditLog;
import com.enterprise.workflow.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    // GET /api/audit — get ALL audit logs
    @GetMapping
    public ResponseEntity<List<AuditLog>> getAllLogs() {
        return ResponseEntity.ok(auditLogService.getAllLogs());
    }

    // GET /api/audit/workflow/1 — logs for one workflow
    @GetMapping("/workflow/{workflowId}")
    public ResponseEntity<List<AuditLog>> getLogsForWorkflow(
            @PathVariable Long workflowId) {
        return ResponseEntity.ok(auditLogService.getLogsForWorkflow(workflowId));
    }

    // GET /api/audit/person/Rohith — logs by person name
    @GetMapping("/person/{name}")
    public ResponseEntity<List<AuditLog>> getLogsByPerson(
            @PathVariable String name) {
        return ResponseEntity.ok(auditLogService.getLogsByPerson(name));
    }
}