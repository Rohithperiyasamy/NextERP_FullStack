package com.enterprise.workflow.service;
import com.enterprise.workflow.entity.User;
import com.enterprise.workflow.entity.Workflow;
import com.enterprise.workflow.enums.Role;
import com.enterprise.workflow.enums.WorkflowStatus;
import com.enterprise.workflow.repositories.UserRepository;
import com.enterprise.workflow.repositories.WorkflowRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkflowServiceTests {

    @Mock
    private WorkflowRepository workflowRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private WorkflowService workflowService;

    private User employee;
    private User manager;
    private Workflow workflow;

    @BeforeEach
    void setUp() {
        employee = new User();
        employee.setId(1L);
        employee.setName("Test Employee");
        employee.setEmail("employee@example.com");
        employee.setRole(Role.EMPLOYEE);

        manager = new User();
        manager.setId(2L);
        manager.setName("Test Manager");
        manager.setEmail("manager@example.com");
        manager.setRole(Role.MANAGER);

        workflow = new Workflow();
        workflow.setId(10L);
        workflow.setTitle("Leave Request");
        workflow.setDescription("Original description");
        workflow.setType("LEAVE");
        workflow.setSubmittedBy(employee);
        workflow.setStatus(WorkflowStatus.PENDING);
    }

    // 1. SUBMIT WORKFLOW

    @Test
    void submitWorkflowShouldCreatePendingWorkflow() {
        when(userRepository.findByEmail("employee@example.com"))
                .thenReturn(Optional.of(employee));
        when(workflowRepository.save(any(Workflow.class)))
                .thenAnswer(invocation -> {
                    Workflow saved = invocation.getArgument(0);
                    saved.setId(10L);
                    return saved;
                });

        Workflow result = workflowService.submitWorkflow(
                "Leave Request",
                "Need leave",
                "LEAVE",
                "employee@example.com"
        );

        assertEquals("Leave Request", result.getTitle());
        assertEquals("Need leave", result.getDescription());
        assertEquals("LEAVE", result.getType());
        assertEquals(WorkflowStatus.PENDING, result.getStatus());
        assertSame(employee, result.getSubmittedBy());
        assertNotNull(result.getSubmittedAt());
        assertNotNull(result.getUpdatedAt());

        verify(workflowRepository).save(any(Workflow.class));
        verify(auditLogService).log(
                "Test Employee",
                "SUBMITTED",
                10L,
                "Leave Request",
                "PENDING",
                "Workflow submitted by employee"
        );
    }

    @Test
    void submitWorkflowShouldRejectUnknownUser() {
        when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> workflowService.submitWorkflow(
                        "Title", "Description", "LEAVE",
                        "unknown@example.com"
                )
        );

        assertEquals("User not found", exception.getMessage());
        verify(workflowRepository, never()).save(any());
    }

    @Test
    void submitWorkflowShouldRejectNonEmployee() {
        when(userRepository.findByEmail("manager@example.com"))
                .thenReturn(Optional.of(manager));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> workflowService.submitWorkflow(
                        "Title", "Description", "LEAVE",
                        "manager@example.com"
                )
        );

        assertEquals(
                "Only employees can submit workflows",
                exception.getMessage()
        );
        verify(workflowRepository, never()).save(any());
    }

    // 2. START REVIEW

    @Test
    void startReviewShouldChangePendingToInReview() {
        when(workflowRepository.findById(10L))
                .thenReturn(Optional.of(workflow));
        when(userRepository.findByEmail("manager@example.com"))
                .thenReturn(Optional.of(manager));
        when(workflowRepository.save(workflow)).thenReturn(workflow);

        Workflow result = workflowService.startReview(
                10L, "manager@example.com"
        );

        assertEquals(WorkflowStatus.IN_REVIEW, result.getStatus());
        assertNotNull(result.getUpdatedAt());

        verify(auditLogService).log(
                "Test Manager",
                "REVIEW_STARTED",
                10L,
                "Leave Request",
                "IN_REVIEW",
                "Manager started reviewing the workflow"
        );
    }

    @Test
    void startReviewShouldRejectNonManager() {
        when(workflowRepository.findById(10L))
                .thenReturn(Optional.of(workflow));
        when(userRepository.findByEmail("employee@example.com"))
                .thenReturn(Optional.of(employee));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> workflowService.startReview(
                        10L, "employee@example.com"
                )
        );

        assertEquals(
                "Only managers can review workflows",
                exception.getMessage()
        );
        verify(workflowRepository, never()).save(any());
    }

    @Test
    void startReviewShouldRejectNonPendingWorkflow() {
        workflow.setStatus(WorkflowStatus.APPROVED);

        when(workflowRepository.findById(10L))
                .thenReturn(Optional.of(workflow));
        when(userRepository.findByEmail("manager@example.com"))
                .thenReturn(Optional.of(manager));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> workflowService.startReview(
                        10L, "manager@example.com"
                )
        );

        assertTrue(exception.getMessage().contains("must be PENDING"));
        verify(workflowRepository, never()).save(any());
    }

    // 3. APPROVE WORKFLOW

    @Test
    void approveWorkflowShouldChangeStatusAndNotifyEmployee() {
        workflow.setStatus(WorkflowStatus.IN_REVIEW);

        when(workflowRepository.findById(10L))
                .thenReturn(Optional.of(workflow));
        when(userRepository.findByEmail("manager@example.com"))
                .thenReturn(Optional.of(manager));
        when(workflowRepository.save(workflow)).thenReturn(workflow);

        Workflow result = workflowService.approveWorkflow(
                10L, "manager@example.com"
        );

        assertEquals(WorkflowStatus.APPROVED, result.getStatus());

        verify(auditLogService).log(
                "Test Manager",
                "APPROVED",
                10L,
                "Leave Request",
                "APPROVED",
                "Workflow approved by manager"
        );

        verify(notificationService).notifyApproved(
                "employee@example.com",
                "Leave Request"
        );
    }

    @Test
    void approveWorkflowShouldRejectWrongStatus() {
        when(workflowRepository.findById(10L))
                .thenReturn(Optional.of(workflow));
        when(userRepository.findByEmail("manager@example.com"))
                .thenReturn(Optional.of(manager));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> workflowService.approveWorkflow(
                        10L, "manager@example.com"
                )
        );

        assertTrue(exception.getMessage().contains("must be IN_REVIEW"));
        verify(workflowRepository, never()).save(any());
        verifyNoInteractions(notificationService);
    }

    // 4. REJECT WORKFLOW

    @Test
    void rejectWorkflowShouldChangeStatusAndNotifyEmployee() {
        workflow.setStatus(WorkflowStatus.IN_REVIEW);

        when(workflowRepository.findById(10L))
                .thenReturn(Optional.of(workflow));
        when(userRepository.findByEmail("manager@example.com"))
                .thenReturn(Optional.of(manager));
        when(workflowRepository.save(workflow)).thenReturn(workflow);

        Workflow result = workflowService.rejectWorkflow(
                10L, "manager@example.com"
        );

        assertEquals(WorkflowStatus.REJECTED, result.getStatus());

        verify(auditLogService).log(
                "Test Manager",
                "REJECTED",
                10L,
                "Leave Request",
                "REJECTED",
                "Workflow rejected by manager"
        );

        verify(notificationService).notifyRejected(
                "employee@example.com",
                "Leave Request"
        );
    }

    @Test
    void rejectWorkflowShouldRejectWrongStatus() {
        when(workflowRepository.findById(10L))
                .thenReturn(Optional.of(workflow));
        when(userRepository.findByEmail("manager@example.com"))
                .thenReturn(Optional.of(manager));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> workflowService.rejectWorkflow(
                        10L, "manager@example.com"
                )
        );

        assertTrue(exception.getMessage().contains("must be IN_REVIEW"));
        verify(workflowRepository, never()).save(any());
        verifyNoInteractions(notificationService);
    }

    // 5. REQUEST MORE INFORMATION

    @Test
    void requestMoreInfoShouldChangeStatusAndNotifyEmployee() {
        workflow.setStatus(WorkflowStatus.IN_REVIEW);

        when(workflowRepository.findById(10L))
                .thenReturn(Optional.of(workflow));
        when(userRepository.findByEmail("manager@example.com"))
                .thenReturn(Optional.of(manager));
        when(workflowRepository.save(workflow)).thenReturn(workflow);

        Workflow result = workflowService.requestMoreInfo(
                10L, "manager@example.com"
        );

        assertEquals(WorkflowStatus.NEEDS_MORE_INFO, result.getStatus());

        verify(auditLogService).log(
                "Test Manager",
                "REQUESTED_MORE_INFO",
                10L,
                "Leave Request",
                "NEEDS_MORE_INFO",
                "Manager requested more information"
        );

        verify(notificationService).notifyMoreInfoNeeded(
                "employee@example.com",
                "Leave Request"
        );
    }

    @Test
    void requestMoreInfoShouldRejectWrongStatus() {
        when(workflowRepository.findById(10L))
                .thenReturn(Optional.of(workflow));
        when(userRepository.findByEmail("manager@example.com"))
                .thenReturn(Optional.of(manager));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> workflowService.requestMoreInfo(
                        10L, "manager@example.com"
                )
        );

        assertTrue(exception.getMessage().contains("must be IN_REVIEW"));
        verify(workflowRepository, never()).save(any());
        verifyNoInteractions(notificationService);
    }

    // 6. RESUBMIT WORKFLOW

    @Test
    void resubmitWorkflowShouldUpdateDescriptionAndResetStatus() {
        workflow.setStatus(WorkflowStatus.NEEDS_MORE_INFO);

        when(workflowRepository.findById(10L))
                .thenReturn(Optional.of(workflow));
        when(userRepository.findByEmail("employee@example.com"))
                .thenReturn(Optional.of(employee));
        when(workflowRepository.save(workflow)).thenReturn(workflow);

        Workflow result = workflowService.resubmitWorkflow(
                10L,
                "employee@example.com",
                "  Updated description  "
        );

        assertEquals("Updated description", result.getDescription());
        assertEquals(WorkflowStatus.PENDING, result.getStatus());
        assertNotNull(result.getUpdatedAt());

        verify(auditLogService).log(
                "Test Employee",
                "RESUBMITTED",
                10L,
                "Leave Request",
                "PENDING",
                "Employee resubmitted workflow with more information"
        );
    }

    @Test
    void resubmitWorkflowShouldRejectAnotherEmployeesWorkflow() {
        User anotherEmployee = new User();
        anotherEmployee.setId(3L);
        anotherEmployee.setName("Another Employee");
        anotherEmployee.setRole(Role.EMPLOYEE);

        workflow.setStatus(WorkflowStatus.NEEDS_MORE_INFO);

        when(workflowRepository.findById(10L))
                .thenReturn(Optional.of(workflow));
        when(userRepository.findByEmail("another@example.com"))
                .thenReturn(Optional.of(anotherEmployee));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> workflowService.resubmitWorkflow(
                        10L, "another@example.com", "Updated description"
                )
        );

        assertEquals(
                "You can only resubmit workflows you created",
                exception.getMessage()
        );
        verify(workflowRepository, never()).save(any());
    }

    @Test
    void resubmitWorkflowShouldRejectBlankDescription() {
        workflow.setStatus(WorkflowStatus.NEEDS_MORE_INFO);

        when(workflowRepository.findById(10L))
                .thenReturn(Optional.of(workflow));
        when(userRepository.findByEmail("employee@example.com"))
                .thenReturn(Optional.of(employee));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> workflowService.resubmitWorkflow(
                        10L, "employee@example.com", "   "
                )
        );

        assertEquals(
                "Please provide the updated workflow description",
                exception.getMessage()
        );
        verify(workflowRepository, never()).save(any());
    }

    @Test
    void resubmitWorkflowShouldRejectWrongStatus() {
        when(workflowRepository.findById(10L))
                .thenReturn(Optional.of(workflow));
        when(userRepository.findByEmail("employee@example.com"))
                .thenReturn(Optional.of(employee));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> workflowService.resubmitWorkflow(
                        10L, "employee@example.com", "Updated description"
                )
        );

        assertEquals(
                "Workflow must be NEEDS_MORE_INFO to resubmit",
                exception.getMessage()
        );
        verify(workflowRepository, never()).save(any());
    }

    // 7–10. RETRIEVAL METHODS

    @Test
    void getAllWorkflowsShouldReturnAllWorkflows() {
        List<Workflow> expected = List.of(workflow);
        when(workflowRepository.findAll()).thenReturn(expected);

        assertSame(expected, workflowService.getAllWorkflows());
    }

    @Test
    void getWorkflowsByStatusShouldReturnMatchingWorkflows() {
        List<Workflow> expected = List.of(workflow);
        when(workflowRepository.findByStatus(WorkflowStatus.PENDING))
                .thenReturn(expected);

        assertSame(
                expected,
                workflowService.getWorkflowsByStatus(WorkflowStatus.PENDING)
        );
    }

    @Test
    void getWorkflowsByUserShouldReturnSubmittedWorkflows() {
        List<Workflow> expected = List.of(workflow);
        when(workflowRepository.findBySubmittedById(1L))
                .thenReturn(expected);

        assertSame(expected, workflowService.getWorkflowsByUser(1L));
    }

    @Test
    void countByStatusShouldReturnRepositoryCount() {
        when(workflowRepository.countByStatus(WorkflowStatus.PENDING))
                .thenReturn(5L);

        assertEquals(
                5L,
                workflowService.countByStatus(WorkflowStatus.PENDING)
        );
    }

    @Test
    void getWorkflowByIdShouldReturnWorkflow() {
        when(workflowRepository.findById(10L))
                .thenReturn(Optional.of(workflow));

        assertSame(workflow, workflowService.getWorkflowById(10L));
    }

    @Test
    void getWorkflowByIdShouldThrowWhenNotFound() {
        when(workflowRepository.findById(99L))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> workflowService.getWorkflowById(99L)
        );

        assertEquals("Workflow not found: 99", exception.getMessage());
    }
}
