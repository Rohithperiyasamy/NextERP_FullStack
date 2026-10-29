package com.enterprise.workflow.repositories;

import com.enterprise.workflow.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // get all logs for one specific workflow
    List<AuditLog> findByWorkflowIdOrderByTimestampAsc(Long workflowId);

    // get all logs by a specific person
    List<AuditLog> findByPerformedByOrderByTimestampDesc(String performedBy);

    // get all logs for a specific action
    List<AuditLog> findByAction(String action);

    Object countByAction(String approved);
}