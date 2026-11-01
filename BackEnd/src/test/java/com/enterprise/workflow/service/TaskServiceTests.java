package com.enterprise.workflow.service;




import com.enterprise.workflow.entity.Task;
import com.enterprise.workflow.entity.User;
import com.enterprise.workflow.entity.Workflow;
import com.enterprise.workflow.enums.TaskStatus;
import com.enterprise.workflow.repositories.TaskRepository;
import com.enterprise.workflow.repositories.UserRepository;
import com.enterprise.workflow.repositories.WorkflowRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTests {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WorkflowRepository workflowRepository;

    @InjectMocks
    private TaskService taskService;

    private Workflow workflow;
    private User user;
    private Task task;

    @BeforeEach
    void setUp() {
        workflow = new Workflow();
        user = new User();

        task = new Task();
        task.setStatus(TaskStatus.PENDING);
    }

    @Test
    void createTaskShouldSaveTaskWithPendingStatus() {
        LocalDateTime dueDate = LocalDateTime.now().plusDays(2);

        when(workflowRepository.findById(1L))
                .thenReturn(Optional.of(workflow));
        when(userRepository.findById(2L))
                .thenReturn(Optional.of(user));
        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Task result = taskService.createTask(
                "Test Task", "Test Description",
                1L, 2L, dueDate, 1
        );

        assertEquals("Test Task", result.getTitle());
        assertEquals("Test Description", result.getDescription());
        assertEquals(TaskStatus.PENDING, result.getStatus());
        assertEquals(dueDate, result.getDueDate());
        assertEquals(1, result.getPriority());
        assertSame(workflow, result.getWorkflow());
        assertSame(user, result.getAssignedTo());

        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void createTaskShouldFailWhenWorkflowDoesNotExist() {
        when(workflowRepository.findById(1L))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> taskService.createTask(
                        "Task", "Description",
                        1L, 2L, LocalDateTime.now(), 1
                )
        );

        assertEquals("Workflow not found", exception.getMessage());
        verify(taskRepository, never()).save(any());
        verify(userRepository, never()).findById(any());
    }

    @Test
    void createTaskShouldFailWhenUserDoesNotExist() {
        when(workflowRepository.findById(1L))
                .thenReturn(Optional.of(workflow));
        when(userRepository.findById(2L))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> taskService.createTask(
                        "Task", "Description",
                        1L, 2L, LocalDateTime.now(), 1
                )
        );

        assertEquals("User not found", exception.getMessage());
        verify(taskRepository, never()).save(any());
    }

    @Test
    void completeTaskShouldSetCompletedStatus() {
        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);

        Task result = taskService.completeTask(1L);

        assertEquals(TaskStatus.COMPLETED, result.getStatus());
        verify(taskRepository).save(task);
    }

    @Test
    void completeTaskShouldFailWhenTaskDoesNotExist() {
        when(taskRepository.findById(1L))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> taskService.completeTask(1L)
        );

        assertEquals("Task not found", exception.getMessage());
        verify(taskRepository, never()).save(any());
    }

    @Test
    void completeTaskShouldRejectAlreadyCompletedTask() {
        task.setStatus(TaskStatus.COMPLETED);

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> taskService.completeTask(1L)
        );

        assertEquals("Task is already completed", exception.getMessage());
        verify(taskRepository, never()).save(any());
    }

    @Test
    void startTaskShouldSetInProgressStatus() {
        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);

        Task result = taskService.startTask(1L);

        assertEquals(TaskStatus.IN_PROGRESS, result.getStatus());
        verify(taskRepository).save(task);
    }

    @Test
    void startTaskShouldFailWhenTaskDoesNotExist() {
        when(taskRepository.findById(1L))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> taskService.startTask(1L)
        );

        assertEquals("Task not found", exception.getMessage());
        verify(taskRepository, never()).save(any());
    }

    @Test
    void getTasksForUserShouldReturnRepositoryResults() {
        List<Task> expected = List.of(task);

        when(taskRepository.findByAssignedToId(2L))
                .thenReturn(expected);

        List<Task> result = taskService.getTasksForUser(2L);

        assertSame(expected, result);
        verify(taskRepository).findByAssignedToId(2L);
    }

    @Test
    void getTasksForWorkflowShouldReturnRepositoryResults() {
        List<Task> expected = List.of(task);

        when(taskRepository.findByWorkflowId(1L))
                .thenReturn(expected);

        List<Task> result = taskService.getTasksForWorkflow(1L);

        assertSame(expected, result);
        verify(taskRepository).findByWorkflowId(1L);
    }
}
