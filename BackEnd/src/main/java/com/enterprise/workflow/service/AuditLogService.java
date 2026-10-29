package com.enterprise.workflow.service;

import com.enterprise.workflow.entity.AuditLog;
import com.enterprise.workflow.repositories.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    // ─── Save one audit log entry ─────────────────────────────────
    public void log(String performedBy,
                    String action,
                    Long workflowId,
                    String workflowTitle,
                    String newStatus,
                    String remarks) {

        AuditLog log = new AuditLog();
        log.setPerformedBy(performedBy);
        log.setAction(action);
        log.setWorkflowId(workflowId);
        log.setWorkflowTitle(workflowTitle);
        log.setNewStatus(newStatus);
        log.setRemarks(remarks);
        log.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(log);
    }

    // ─── Get all logs for one workflow ────────────────────────────
    public List<AuditLog> getLogsForWorkflow(Long workflowId) {
        return auditLogRepository.findByWorkflowIdOrderByTimestampAsc(workflowId);
    }

    // ─── Get all logs by a person ─────────────────────────────────
    public List<AuditLog> getLogsByPerson(String name) {
        return auditLogRepository.findByPerformedByOrderByTimestampDesc(name);
    }

    // ─── Get all logs ─────────────────────────────────────────────
    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAll();
    }
}
