package com.enterprise.workflow.controller;

import com.enterprise.workflow.entity.Workflow;
import com.enterprise.workflow.enums.WorkflowStatus;
import com.enterprise.workflow.service.WorkflowService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workflows")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;


    // ── POST /api/workflows/submit ─────────────────────────────────────
    // Employee submits a new workflow
    @PostMapping("/submit")
    public ResponseEntity<?> submitWorkflow(
            @RequestBody SubmitRequest request,
            Authentication authentication) {

        Workflow workflow = workflowService.submitWorkflow(
                request.getTitle(),
                request.getDescription(),
                request.getType(),
                authentication.getName()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(workflow);
    }

    // ── GET /api/workflows ─────────────────────────────────────────────
    // Get all workflows
    @GetMapping
    public ResponseEntity<List<Workflow>> getAllWorkflows() {
        return ResponseEntity.ok(workflowService.getAllWorkflows()); // ✅
    }

    // ── GET /api/workflows/5 ───────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<?> getWorkflowById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(workflowService.getWorkflowById(id)); // ✅
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ── GET /api/workflows/status/PENDING ──────────────────
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getByStatus(@PathVariable String status) {
        try {
            WorkflowStatus workflowStatus = WorkflowStatus.valueOf(status.toUpperCase());
            return ResponseEntity.ok(workflowService.getWorkflowsByStatus(workflowStatus)); // ✅
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    // ── PUT /api/workflows/5/review?managerId=2 ───────────────────────
    // Manager starts reviewing
    @PutMapping("/{id}/review")
    public ResponseEntity<?> startReview(@PathVariable Long id,
                                         Authentication authentication) {
        try {
            return ResponseEntity.ok(workflowService.startReview(id, authentication.getName()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ── PUT /api/workflows/5/approve?managerId=2 ──────────────────────
    // Manager approves
    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveWorkflow(@PathVariable Long id,
                                             Authentication authentication) {
        try {
            return ResponseEntity.ok(workflowService.approveWorkflow(id, authentication.getName()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ── PUT /api/workflows/5/reject?managerId=2 ───────────────────────
    // Manager rejects
    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rejectWorkflow(@PathVariable Long id,
                                            Authentication authentication) {
        try {
            return ResponseEntity.ok(workflowService.rejectWorkflow(id, authentication.getName()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ── PUT /api/workflows/5/more-info?managerId=2 ────────────────────
    // Manager requests more info
    @PutMapping("/{id}/more-info")
    public ResponseEntity<?> requestMoreInfo(@PathVariable Long id,
                                             Authentication authentication) {
        try {
            return ResponseEntity.ok(workflowService.requestMoreInfo(id, authentication.getName()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    // Employee updates the description and resubmits a request that needs more information.
    @PutMapping("/{id}/resubmit")
    public ResponseEntity<?> resubmitWorkflow(
            @PathVariable Long id,
            @RequestBody ResubmitRequest request,
            Authentication authentication) {
        try {
            return ResponseEntity.ok(
                    workflowService.resubmitWorkflow(
                            id,
                            authentication.getName(),
                            request.getDescription()
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Data
    public static class ResubmitRequest {
        private String description;
    }
    // ── inner request class ────────────────────────────────────────────
   @Data
    public static class SubmitRequest {
        private String title;
        private String description;
        private String type;



    }
}