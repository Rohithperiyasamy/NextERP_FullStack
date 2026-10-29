package com.enterprise.workflow.service;



import com.enterprise.workflow.entity.Task;
import com.enterprise.workflow.entity.User;
import com.enterprise.workflow.entity.Workflow;
import com.enterprise.workflow.enums.TaskStatus;
import com.enterprise.workflow.repositories.TaskRepository;
import com.enterprise.workflow.repositories.UserRepository;
import com.enterprise.workflow.repositories.WorkflowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final WorkflowRepository workflowRepository;

    // ─── CREATE a task inside a workflow ────────────────────────────────
    public Task createTask(String title, String description,
                           Long workflowId, Long assignedToUserId,
                           LocalDateTime dueDate, Integer priority) {

        Workflow workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new RuntimeException("Workflow not found"));

        User assignedUser = userRepository.findById(assignedToUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setWorkflow(workflow);
        task.setAssignedTo(assignedUser);
        task.setDueDate(dueDate);
        task.setPriority(priority);
        task.setStatus(TaskStatus.PENDING);  // always starts PENDING

        return taskRepository.save(task);
    }

    // ─── COMPLETE a task ─────────────────────────────────────────────────
    public Task completeTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (task.getStatus() == TaskStatus.COMPLETED) {
            throw new RuntimeException("Task is already completed");
        }

        task.setStatus(TaskStatus.COMPLETED);
        return taskRepository.save(task);
    }

    // ─── START a task ────────────────────────────────────────────────────
    public Task startTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        task.setStatus(TaskStatus.IN_PROGRESS);
        return taskRepository.save(task);
    }

    // ─── GET all tasks assigned to a specific user ───────────────────────
    public List<Task> getTasksForUser(Long userId) {
        return taskRepository.findByAssignedToId(userId);
    }

    // ─── GET all tasks for a workflow ────────────────────────────────────
    public List<Task> getTasksForWorkflow(Long workflowId) {
        return taskRepository.findByWorkflowId(workflowId);
    }
}