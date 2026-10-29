package com.enterprise.workflow.repositories;

import com.enterprise.workflow.entity.Workflow;
import com.enterprise.workflow.enums.WorkflowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowRepository extends JpaRepository<Workflow, Long> {

    // get all workflows with a specific status
    // e.g. findByStatus(PENDING) → all pending workflows
    List<Workflow> findByStatus(WorkflowStatus status);

    // get all workflows submitted by one specific user
    List<Workflow> findBySubmittedById(Long userId);

    // get all workflows of a specific type
    // e.g. type = "LEAVE_REQUEST"
    List<Workflow> findByType(String type);

    // get workflows by a user AND a specific status
    // e.g. all PENDING workflows submitted by user ID 5
    List<Workflow> findBySubmittedByIdAndStatus(Long userId, WorkflowStatus status);

    // count how many workflows are in a given status
    // useful for dashboard/reports
    long countByStatus(WorkflowStatus status);
}