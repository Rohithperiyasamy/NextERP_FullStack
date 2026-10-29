package com.enterprise.workflow.controller;

import com.enterprise.workflow.enums.Role;
import com.enterprise.workflow.enums.TaskStatus;
import com.enterprise.workflow.enums.WorkflowStatus;
import com.enterprise.workflow.repositories.AuditLogRepository;
import com.enterprise.workflow.repositories.TaskRepository;
import com.enterprise.workflow.repositories.UserRepository;
import com.enterprise.workflow.repositories.WorkflowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final WorkflowRepository workflowRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;

    // ── GET /api/dashboard/summary ────────────────────────────────
    // Returns everything the admin dashboard needs in ONE call
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {

        Map<String, Object> summary = new HashMap<>();

        // ── Users ──────────────────────────────────────────────────
        Map<String, Object> users = new HashMap<>();
        users.put("total",     userRepository.count());
        users.put("employees", userRepository.countByRole(Role.EMPLOYEE));
        users.put("managers",  userRepository.countByRole(Role.MANAGER));
        users.put("admins",    userRepository.countByRole(Role.ADMIN));
        summary.put("users", users);

        // ── Workflows ──────────────────────────────────────────────
        Map<String, Object> workflows = new HashMap<>();
        workflows.put("total",
                workflowRepository.count());
        workflows.put("pending",
                workflowRepository.countByStatus(WorkflowStatus.PENDING));
        workflows.put("inReview",
                workflowRepository.countByStatus(WorkflowStatus.IN_REVIEW));
        workflows.put("approved",
                workflowRepository.countByStatus(WorkflowStatus.APPROVED));
        workflows.put("rejected",
                workflowRepository.countByStatus(WorkflowStatus.REJECTED));
        workflows.put("needsMoreInfo",
                workflowRepository.countByStatus(WorkflowStatus.NEEDS_MORE_INFO));
        summary.put("workflows", workflows);

        // ── Tasks ──────────────────────────────────────────────────
        Map<String, Object> tasks = new HashMap<>();
        tasks.put("total",      taskRepository.count());
        tasks.put("pending",    taskRepository.countByStatus(TaskStatus.PENDING));
        tasks.put("inProgress", taskRepository.countByStatus(TaskStatus.IN_PROGRESS));
        tasks.put("completed",  taskRepository.countByStatus(TaskStatus.COMPLETED));
        tasks.put("overdue",    taskRepository.countByStatus(TaskStatus.OVERDUE));
        summary.put("tasks", tasks);

        // ── Audit ──────────────────────────────────────────────────
        Map<String, Object> audit = new HashMap<>();
        audit.put("totalActions",  auditLogRepository.count());
        audit.put("approvals",     auditLogRepository.countByAction("APPROVED"));
        audit.put("rejections",    auditLogRepository.countByAction("REJECTED"));
        audit.put("submissions",   auditLogRepository.countByAction("SUBMITTED"));
        summary.put("auditActivity", audit);

        return ResponseEntity.ok(summary);
    }
}