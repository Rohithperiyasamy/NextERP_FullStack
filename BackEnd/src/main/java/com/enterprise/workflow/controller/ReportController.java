package com.enterprise.workflow.controller;

import com.enterprise.workflow.enums.WorkflowStatus;
import com.enterprise.workflow.repositories.AuditLogRepository;
import com.enterprise.workflow.repositories.TaskRepository;
import com.enterprise.workflow.repositories.UserRepository;
import com.enterprise.workflow.repositories.WorkflowRepository;
import com.enterprise.workflow.enums.TaskStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final WorkflowRepository workflowRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;

    // ── GET /api/reports/workflows ─────────────────────────────────
    // Complete workflow status breakdown
    @GetMapping("/workflows")
    public ResponseEntity<Map<String, Object>> workflowReport() {

        Map<String, Object> report = new HashMap<>();

        report.put("total",
                workflowRepository.count());

        report.put("pending",
                workflowRepository.countByStatus(WorkflowStatus.PENDING));

        report.put("inReview",
                workflowRepository.countByStatus(WorkflowStatus.IN_REVIEW));

        report.put("approved",
                workflowRepository.countByStatus(WorkflowStatus.APPROVED));

        report.put("rejected",
                workflowRepository.countByStatus(WorkflowStatus.REJECTED));

        report.put("needsMoreInfo",
                workflowRepository.countByStatus(WorkflowStatus.NEEDS_MORE_INFO));

        return ResponseEntity.ok(report);
    }

    // ── GET /api/reports/tasks ─────────────────────────────────────
    // Task status breakdown
    @GetMapping("/tasks")
    public ResponseEntity<Map<String, Object>> taskReport() {

        Map<String, Object> report = new HashMap<>();

        report.put("total",
                taskRepository.count());

        report.put("pending",
                taskRepository.countByStatus(TaskStatus.PENDING));

        report.put("inProgress",
                taskRepository.countByStatus(TaskStatus.IN_PROGRESS));

        report.put("completed",
                taskRepository.countByStatus(TaskStatus.COMPLETED));

        report.put("overdue",
                taskRepository.countByStatus(TaskStatus.OVERDUE));

        return ResponseEntity.ok(report);
    }

    // ── GET /api/reports/users ─────────────────────────────────────
    // User count breakdown by role
    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> userReport() {

        Map<String, Object> report = new HashMap<>();

        report.put("totalUsers",
                userRepository.count());

        report.put("employees",
                userRepository.countByRole(
                        com.enterprise.workflow.enums.Role.EMPLOYEE));

        report.put("managers",
                userRepository.countByRole(
                        com.enterprise.workflow.enums.Role.MANAGER));

        report.put("admins",
                userRepository.countByRole(
                        com.enterprise.workflow.enums.Role.ADMIN));

        return ResponseEntity.ok(report);
    }

    // ── GET /api/reports/audit ─────────────────────────────────────
    // Audit log activity count
    @GetMapping("/audit")
    public ResponseEntity<Map<String, Object>> auditReport() {

        Map<String, Object> report = new HashMap<>();

        report.put("totalActions",
                auditLogRepository.count());

        report.put("approvals",
                auditLogRepository.countByAction("APPROVED"));

        report.put("rejections",
                auditLogRepository.countByAction("REJECTED"));

        report.put("submissions",
                auditLogRepository.countByAction("SUBMITTED"));

        return ResponseEntity.ok(report);
    }
}