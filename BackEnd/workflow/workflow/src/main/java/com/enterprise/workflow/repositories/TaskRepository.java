package com.enterprise.workflow.repositories;

import com.enterprise.workflow.entity.Task;
import com.enterprise.workflow.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // get all tasks assigned to a specific user
    List<Task> findByAssignedToId(Long userId);

    // get all tasks inside a specific workflow
    List<Task> findByWorkflowId(Long workflowId);

    // get all tasks with a specific status
    // e.g. findByStatus(OVERDUE) → all overdue tasks
    List<Task> findByStatus(TaskStatus status);

    // get tasks assigned to a user AND a specific status
    // e.g. all IN_PROGRESS tasks for user ID 3
    List<Task> findByAssignedToIdAndStatus(Long userId, TaskStatus status);

    // get all tasks whose due date has passed and are not yet completed
    // used by the Scheduler to mark tasks as OVERDUE
    List<Task> findByDueDateBeforeAndStatusNot(
            LocalDateTime now,
            TaskStatus status
    );

    // count tasks inside a workflow
    long countByWorkflowId(Long workflowId);

    // count completed tasks inside a workflow
    // used to check if all tasks are done
    long countByWorkflowIdAndStatus(Long workflowId, TaskStatus status);

    Object countByStatus(TaskStatus taskStatus);
}