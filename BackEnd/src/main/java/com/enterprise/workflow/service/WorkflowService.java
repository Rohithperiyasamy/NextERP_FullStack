package com.enterprise.workflow.service;

import com.enterprise.workflow.entity.User;
import com.enterprise.workflow.entity.Workflow;
import com.enterprise.workflow.enums.Role;
import com.enterprise.workflow.enums.WorkflowStatus;
import com.enterprise.workflow.repositories.UserRepository;
import com.enterprise.workflow.repositories.WorkflowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkflowService {

    private final WorkflowRepository workflowRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;

    // ─── 1. SUBMIT WORKFLOW ───────────────────────────────────────────
    public Workflow submitWorkflow(
            String title,
            String description,
            String type,
            String authenticatedEmail) {

        User submitter = userRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (submitter.getRole() != Role.EMPLOYEE) {
            throw new RuntimeException("Only employees can submit workflows");
        }

        Workflow workflow = new Workflow();
        workflow.setTitle(title);
        workflow.setDescription(description);
        workflow.setType(type);
        workflow.setSubmittedBy(submitter);
        workflow.setStatus(WorkflowStatus.PENDING);
        workflow.setSubmittedAt(LocalDateTime.now());
        workflow.setUpdatedAt(LocalDateTime.now());

        Workflow saved = workflowRepository.save(workflow);

        auditLogService.log(
                submitter.getName(),
                "SUBMITTED",
                saved.getId(),
                saved.getTitle(),
                "PENDING",
                "Workflow submitted by employee"
        );

        return saved;
    }

    // ─── 2. START REVIEW ─────────────────────────────────────────────
    public Workflow startReview(Long workflowId, String authenticatedEmail) {

        Workflow workflow = getWorkflowById(workflowId);

        User manager = userRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        if (manager.getRole() != Role.MANAGER) {
            throw new RuntimeException("Only managers can review workflows");
        }

        if (workflow.getStatus() != WorkflowStatus.PENDING) {
            throw new RuntimeException(
                    "Workflow must be PENDING to start review. Current: "
                            + workflow.getStatus()
            );
        }

        workflow.setStatus(WorkflowStatus.IN_REVIEW);
        workflow.setUpdatedAt(LocalDateTime.now());
        Workflow saved = workflowRepository.save(workflow);

        auditLogService.log(
                manager.getName(),
                "REVIEW_STARTED",
                saved.getId(),
                saved.getTitle(),
                "IN_REVIEW",
                "Manager started reviewing the workflow"
        );

        return saved;
    }

    // ─── 3. APPROVE WORKFLOW ──────────────────────────────────────────
    public Workflow approveWorkflow(Long workflowId, String authenticatedEmail) {

        Workflow workflow = getWorkflowById(workflowId);

        User manager = userRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        if (manager.getRole() != Role.MANAGER) {
            throw new RuntimeException("Only managers can approve workflows");
        }

        if (workflow.getStatus() != WorkflowStatus.IN_REVIEW) {
            throw new RuntimeException(
                    "Workflow must be IN_REVIEW to approve. Current: "
                            + workflow.getStatus()
            );
        }

        workflow.setStatus(WorkflowStatus.APPROVED);
        workflow.setUpdatedAt(LocalDateTime.now());
        Workflow saved = workflowRepository.save(workflow);

        auditLogService.log(
                manager.getName(),
                "APPROVED",
                saved.getId(),
                saved.getTitle(),
                "APPROVED",
                "Workflow approved by manager"
        );

        notificationService.notifyApproved(
                workflow.getSubmittedBy().getEmail(),
                workflow.getTitle()
        );

        return saved;
    }

    // ─── 4. REJECT WORKFLOW ───────────────────────────────────────────
    public Workflow rejectWorkflow(Long workflowId, String authenticatedEmail) {

        Workflow workflow = getWorkflowById(workflowId);

        User manager = userRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        if (manager.getRole() != Role.MANAGER) {
            throw new RuntimeException("Only managers can reject workflows");
        }

        if (workflow.getStatus() != WorkflowStatus.IN_REVIEW) {
            throw new RuntimeException(
                    "Workflow must be IN_REVIEW to reject. Current: "
                            + workflow.getStatus()
            );
        }

        workflow.setStatus(WorkflowStatus.REJECTED);
        workflow.setUpdatedAt(LocalDateTime.now());
        Workflow saved = workflowRepository.save(workflow);

        auditLogService.log(
                manager.getName(),
                "REJECTED",
                saved.getId(),
                saved.getTitle(),
                "REJECTED",
                "Workflow rejected by manager"
        );

        notificationService.notifyRejected(
                workflow.getSubmittedBy().getEmail(),
                workflow.getTitle()
        );

        return saved;
    }

    // ─── 5. REQUEST MORE INFO ─────────────────────────────────────────
    public Workflow requestMoreInfo(Long workflowId, String authenticatedEmail) {

        Workflow workflow = getWorkflowById(workflowId);

        User manager = userRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        if (manager.getRole() != Role.MANAGER) {
            throw new RuntimeException("Only managers can request more info");
        }

        if (workflow.getStatus() != WorkflowStatus.IN_REVIEW) {
            throw new RuntimeException(
                    "Workflow must be IN_REVIEW. Current: "
                            + workflow.getStatus()
            );
        }

        workflow.setStatus(WorkflowStatus.NEEDS_MORE_INFO);
        workflow.setUpdatedAt(LocalDateTime.now());
        Workflow saved = workflowRepository.save(workflow);

        auditLogService.log(
                manager.getName(),
                "REQUESTED_MORE_INFO",
                saved.getId(),
                saved.getTitle(),
                "NEEDS_MORE_INFO",
                "Manager requested more information"
        );

        notificationService.notifyMoreInfoNeeded(
                workflow.getSubmittedBy().getEmail(),
                workflow.getTitle()
        );

        return saved;
    }

    // ─── 6. RESUBMIT WORKFLOW ─────────────────────────────────────────
    public Workflow resubmitWorkflow(
            Long workflowId,
            String authenticatedEmail,
            String updatedDescription) {

        Workflow workflow = getWorkflowById(workflowId);

        User user = userRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() != Role.EMPLOYEE) {
            throw new RuntimeException("Only employees can resubmit workflows");
        }

        if (workflow.getSubmittedBy() == null
                || !workflow.getSubmittedBy().getId().equals(user.getId())) {
            throw new RuntimeException(
                    "You can only resubmit workflows you created");
        }

        if (updatedDescription == null || updatedDescription.isBlank()) {
            throw new RuntimeException(
                    "Please provide the updated workflow description");
        }

        if (workflow.getStatus() != WorkflowStatus.NEEDS_MORE_INFO) {
            throw new RuntimeException(
                    "Workflow must be NEEDS_MORE_INFO to resubmit");
        }

        workflow.setDescription(updatedDescription.trim());
        workflow.setStatus(WorkflowStatus.PENDING);
        workflow.setUpdatedAt(LocalDateTime.now());

        Workflow saved = workflowRepository.save(workflow);

        auditLogService.log(
                user.getName(),
                "RESUBMITTED",
                saved.getId(),
                saved.getTitle(),
                "PENDING",
                "Employee resubmitted workflow with more information"
        );

        return saved;
    }

    // ─── 7. GET ALL WORKFLOWS ─────────────────────────────────────────
    public List<Workflow> getAllWorkflows() {
        return workflowRepository.findAll();
    }

    // ─── 8. GET WORKFLOWS BY STATUS ───────────────────────────────────
    public List<Workflow> getWorkflowsByStatus(WorkflowStatus status) {
        return workflowRepository.findByStatus(status);
    }

    // ─── 9. GET WORKFLOWS BY USER ─────────────────────────────────────
    public List<Workflow> getWorkflowsByUser(Long userId) {
        return workflowRepository.findBySubmittedById(userId);
    }

    // ─── 10. COUNT BY STATUS ──────────────────────────────────────────
    public long countByStatus(WorkflowStatus status) {
        return workflowRepository.countByStatus(status);
    }

    // ════════════════════════════════════════════════════════════════════
    // HELPER METHODS
    // ════════════════════════════════════════════════════════════════════

    public Workflow getWorkflowById(Long id) {
        return workflowRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Workflow not found: " + id));
    }

    private User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
    }
}