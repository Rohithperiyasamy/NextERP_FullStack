package com.enterprise.workflow.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // WHO did it
    private String performedBy;   // name of the user who did the action

    // WHAT they did
    private String action;        // e.g. "SUBMITTED", "APPROVED", "REJECTED"

    // WHICH workflow was affected
    private Long workflowId;

    private String workflowTitle; // save title so we can read log easily

    // WHAT the status changed to
    private String newStatus;

    // any extra notes
    private String remarks;

    // WHEN it happened
    private LocalDateTime timestamp;
}