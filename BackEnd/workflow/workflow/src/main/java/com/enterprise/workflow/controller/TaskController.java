package com.enterprise.workflow.controller;

import com.enterprise.workflow.entity.Task;
import com.enterprise.workflow.service.TaskService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    // ── POST /api/tasks ───────────────────────────────────────────────
    // Create a new task inside a workflow
    @PostMapping
    public ResponseEntity<?> createTask(@RequestBody CreateTaskRequest request) {
        try {
            Task task = taskService.createTask(
                    request.getTitle(),
                    request.getDescription(),
                    request.getWorkflowId(),
                    request.getAssignedToId(),
                    request.getDueDate(),
                    request.getPriority()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(task);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ── GET /api/tasks/workflow/5 ─────────────────────────────────────
    // Get all tasks for a workflow
    @GetMapping("/workflow/{workflowId}")
    public ResponseEntity<List<Task>> getTasksByWorkflow(
            @PathVariable Long workflowId) {
        return ResponseEntity.ok(taskService.getTasksForWorkflow(workflowId));
    }

    // ── GET /api/tasks/user/3 ─────────────────────────────────────────
    // Get all tasks assigned to a user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Task>> getTasksByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(taskService.getTasksForUser(userId));
    }

    // ── PUT /api/tasks/5/start ────────────────────────────────────────
    // Start a task
    @PutMapping("/{id}/start")
    public ResponseEntity<?> startTask(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(taskService.startTask(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ── PUT /api/tasks/5/complete ─────────────────────────────────────
    // Complete a task
    @PutMapping("/{id}/complete")
    public ResponseEntity<?> completeTask(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(taskService.completeTask(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ── inner request class ───────────────────────────────────────────
    @Data
    public static class CreateTaskRequest {
        private Long workflowId;
        private Long assignedToId;
        private String title;
        private String description;
        private Integer priority;
        private LocalDateTime dueDate;


    }
}