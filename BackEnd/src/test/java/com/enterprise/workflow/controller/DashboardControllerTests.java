
package com.enterprise.workflow.controller;

import com.enterprise.workflow.entity.AuditLog;
import com.enterprise.workflow.entity.Task;
import com.enterprise.workflow.entity.User;
import com.enterprise.workflow.entity.Workflow;
import com.enterprise.workflow.enums.Role;
import com.enterprise.workflow.enums.TaskStatus;
import com.enterprise.workflow.enums.WorkflowStatus;
import com.enterprise.workflow.repositories.AuditLogRepository;
import com.enterprise.workflow.repositories.TaskRepository;
import com.enterprise.workflow.repositories.UserRepository;
import com.enterprise.workflow.repositories.WorkflowRepository;
import com.enterprise.workflow.security.JwtUtil;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class DashboardControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkflowRepository workflowRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;


    // Test 1: Dashboard must reject unauthenticated requests
    @Test
    void dashboardShouldRequireAuthentication() throws Exception {

        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isUnauthorized());
    }


    // Test 2: Dashboard must accept authenticated requests
    @Test
    void dashboardShouldReturnSummaryForAuthenticatedUser() throws Exception {

        String token = jwtUtil.generateToken(
                "test@example.com", "EMPLOYEE");

        mockMvc.perform(get("/api/dashboard/summary")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users.total").exists())
                .andExpect(jsonPath("$.workflows.total").exists())
                .andExpect(jsonPath("$.tasks.total").exists())
                .andExpect(jsonPath("$.auditActivity.totalActions").exists());
    }


    // Test 3: Dashboard counts must match repository counts
    @Test
    void dashboardShouldReturnCorrectCounts() throws Exception {

        String token = jwtUtil.generateToken(
                "dashboard-test@example.com", "EMPLOYEE");

        mockMvc.perform(get("/api/dashboard/summary")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.users.total")
                        .value(userRepository.count()))
                .andExpect(jsonPath("$.users.employees")
                        .value(userRepository.countByRole(Role.EMPLOYEE)))
                .andExpect(jsonPath("$.users.managers")
                        .value(userRepository.countByRole(Role.MANAGER)))
                .andExpect(jsonPath("$.users.admins")
                        .value(userRepository.countByRole(Role.ADMIN)))

                .andExpect(jsonPath("$.workflows.total")
                        .value(workflowRepository.count()))
                .andExpect(jsonPath("$.workflows.pending")
                        .value(workflowRepository.countByStatus(WorkflowStatus.PENDING)))
                .andExpect(jsonPath("$.workflows.inReview")
                        .value(workflowRepository.countByStatus(WorkflowStatus.IN_REVIEW)))
                .andExpect(jsonPath("$.workflows.approved")
                        .value(workflowRepository.countByStatus(WorkflowStatus.APPROVED)))
                .andExpect(jsonPath("$.workflows.rejected")
                        .value(workflowRepository.countByStatus(WorkflowStatus.REJECTED)))
                .andExpect(jsonPath("$.workflows.needsMoreInfo")
                        .value(workflowRepository.countByStatus(WorkflowStatus.NEEDS_MORE_INFO)))

                .andExpect(jsonPath("$.tasks.total")
                        .value(taskRepository.count()))
                .andExpect(jsonPath("$.tasks.pending")
                        .value(taskRepository.countByStatus(TaskStatus.PENDING)))
                .andExpect(jsonPath("$.tasks.inProgress")
                        .value(taskRepository.countByStatus(TaskStatus.IN_PROGRESS)))
                .andExpect(jsonPath("$.tasks.completed")
                        .value(taskRepository.countByStatus(TaskStatus.COMPLETED)))
                .andExpect(jsonPath("$.tasks.overdue")
                        .value(taskRepository.countByStatus(TaskStatus.OVERDUE)))

                .andExpect(jsonPath("$.auditActivity.totalActions")
                        .value(auditLogRepository.count()))
                .andExpect(jsonPath("$.auditActivity.approvals")
                        .value(auditLogRepository.countByAction("APPROVED")))
                .andExpect(jsonPath("$.auditActivity.rejections")
                        .value(auditLogRepository.countByAction("REJECTED")))
                .andExpect(jsonPath("$.auditActivity.submissions")
                        .value(auditLogRepository.countByAction("SUBMITTED")));
    }


    // Test 4: Verify exact dashboard counts using independently seeded data
    @Test
    void dashboardShouldReturnExactCountsFromDatabase() throws Exception {

        // Capture existing database counts
        long initialUsers = userRepository.count();
        long initialEmployees = userRepository.countByRole(Role.EMPLOYEE);
        long initialManagers = userRepository.countByRole(Role.MANAGER);
        long initialAdmins = userRepository.countByRole(Role.ADMIN);

        long initialWorkflows = workflowRepository.count();
        long initialPendingWorkflows =
                workflowRepository.countByStatus(WorkflowStatus.PENDING);
        long initialInReviewWorkflows =
                workflowRepository.countByStatus(WorkflowStatus.IN_REVIEW);
        long initialApprovedWorkflows =
                workflowRepository.countByStatus(WorkflowStatus.APPROVED);
        long initialRejectedWorkflows =
                workflowRepository.countByStatus(WorkflowStatus.REJECTED);
        long initialNeedsMoreInfoWorkflows =
                workflowRepository.countByStatus(WorkflowStatus.NEEDS_MORE_INFO);

        long initialTasks = taskRepository.count();
        long initialPendingTasks =
                (long) taskRepository.countByStatus(TaskStatus.PENDING);
        long initialInProgressTasks =
                (long) taskRepository.countByStatus(TaskStatus.IN_PROGRESS);
        long initialCompletedTasks =
                (long) taskRepository.countByStatus(TaskStatus.COMPLETED);
        long initialOverdueTasks =
                (long) taskRepository.countByStatus(TaskStatus.OVERDUE);

        long initialAuditActions = auditLogRepository.count();
        long initialApprovals =
                (long) auditLogRepository.countByAction("APPROVED");
        long initialRejections =
                (long) auditLogRepository.countByAction("REJECTED");
        long initialSubmissions =
                (long) auditLogRepository.countByAction("SUBMITTED");


        // 1. Create five users
        User employee1 = createUser("Employee 1", "emp1@test.com", Role.EMPLOYEE);
        User employee2 = createUser("Employee 2", "emp2@test.com", Role.EMPLOYEE);
        User employee3 = createUser("Employee 3", "emp3@test.com", Role.EMPLOYEE);
        User manager = createUser("Manager", "manager@test.com", Role.MANAGER);
        User admin = createUser("Admin", "admin@test.com", Role.ADMIN);

        userRepository.saveAll(
                List.of(employee1, employee2, employee3, manager, admin)
        );


        // 2. Create six workflows
        createWorkflow("Workflow 1", WorkflowStatus.PENDING, employee1);
        createWorkflow("Workflow 2", WorkflowStatus.PENDING, employee1);
        createWorkflow("Workflow 3", WorkflowStatus.IN_REVIEW, employee2);
        createWorkflow("Workflow 4", WorkflowStatus.APPROVED, employee2);
        createWorkflow("Workflow 5", WorkflowStatus.REJECTED, employee3);
        createWorkflow("Workflow 6", WorkflowStatus.NEEDS_MORE_INFO, employee3);


        // 3. Create five tasks
        createTask("Task 1", TaskStatus.PENDING, employee1);
        createTask("Task 2", TaskStatus.PENDING, employee2);
        createTask("Task 3", TaskStatus.IN_PROGRESS, employee1);
        createTask("Task 4", TaskStatus.COMPLETED, employee2);
        createTask("Task 5", TaskStatus.OVERDUE, employee3);


        // 4. Create five audit records
        createAudit("APPROVED", "Workflow 4");
        createAudit("APPROVED", "Workflow 4");
        createAudit("REJECTED", "Workflow 5");
        createAudit("SUBMITTED", "Workflow 1");
        createAudit("SUBMITTED", "Workflow 2");


        // 5. Generate JWT
        String token = jwtUtil.generateToken("admin@test.com", "ADMIN");


        // 6. Verify exact expected increments
        mockMvc.perform(get("/api/dashboard/summary")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())

                // Users
                .andExpect(jsonPath("$.users.total").value(initialUsers + 5))
                .andExpect(jsonPath("$.users.employees").value(initialEmployees + 3))
                .andExpect(jsonPath("$.users.managers").value(initialManagers + 1))
                .andExpect(jsonPath("$.users.admins").value(initialAdmins + 1))

                // Workflows
                .andExpect(jsonPath("$.workflows.total").value(initialWorkflows + 6))
                .andExpect(jsonPath("$.workflows.pending").value(initialPendingWorkflows + 2))
                .andExpect(jsonPath("$.workflows.inReview").value(initialInReviewWorkflows + 1))
                .andExpect(jsonPath("$.workflows.approved").value(initialApprovedWorkflows + 1))
                .andExpect(jsonPath("$.workflows.rejected").value(initialRejectedWorkflows + 1))
                .andExpect(jsonPath("$.workflows.needsMoreInfo").value(initialNeedsMoreInfoWorkflows + 1))

                // Tasks
                .andExpect(jsonPath("$.tasks.total").value(initialTasks + 5))
                .andExpect(jsonPath("$.tasks.pending").value(initialPendingTasks + 2))
                .andExpect(jsonPath("$.tasks.inProgress").value(initialInProgressTasks + 1))
                .andExpect(jsonPath("$.tasks.completed").value(initialCompletedTasks + 1))
                .andExpect(jsonPath("$.tasks.overdue").value(initialOverdueTasks + 1))

                // Audit activity
                .andExpect(jsonPath("$.auditActivity.totalActions").value(initialAuditActions + 5))
                .andExpect(jsonPath("$.auditActivity.approvals").value(initialApprovals + 2))
                .andExpect(jsonPath("$.auditActivity.rejections").value(initialRejections + 1))
                .andExpect(jsonPath("$.auditActivity.submissions").value(initialSubmissions + 2));
    }


    // Helper: Create user
    private User createUser(String name, String email, Role role) {

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword("test-password");
        user.setRole(role);

        return user;
    }


    // Helper: Create workflow
    private void createWorkflow(
            String title, WorkflowStatus status, User employee) {

        Workflow workflow = new Workflow();
        workflow.setTitle(title);
        workflow.setDescription("Dashboard integration test");
        workflow.setType("TEST");
        workflow.setStatus(status);
        workflow.setSubmittedBy(employee);
        workflow.setSubmittedAt(LocalDateTime.now());
        workflow.setUpdatedAt(LocalDateTime.now());

        workflowRepository.save(workflow);
    }


    // Helper: Create task
    private void createTask(
            String title, TaskStatus status, User employee) {

        Task task = new Task();
        task.setTitle(title);
        task.setDescription("Dashboard integration test");
        task.setStatus(status);
        task.setAssignedTo(employee);
        task.setDueDate(LocalDateTime.now().plusDays(1));
        task.setPriority(1);

        taskRepository.save(task);
    }


    // Helper: Create audit log
    private void createAudit(String action, String workflowTitle) {

        AuditLog audit = new AuditLog();
        audit.setPerformedBy("Admin");
        audit.setAction(action);
        audit.setWorkflowId(1L);
        audit.setWorkflowTitle(workflowTitle);
        audit.setNewStatus(action);
        audit.setRemarks("Dashboard integration test");
        audit.setTimestamp(LocalDateTime.now());

        auditLogRepository.save(audit);
    }
}
