package com.enterprise.workflow.service;

import com.enterprise.workflow.enums.WorkflowStatus;
import org.springframework.stereotype.Component;

@Component  // ← Spring manages this class
public class WorkflowStateMachine {

    // ─── Can this transition happen? ────────────────────────────────────
    public boolean canTransition(WorkflowStatus current,
                                 WorkflowStatus next) {
        return switch (current) {
            case DRAFT           -> next == WorkflowStatus.PENDING;
            case PENDING         -> next == WorkflowStatus.IN_REVIEW;
            case IN_REVIEW       -> next == WorkflowStatus.APPROVED
                    || next == WorkflowStatus.REJECTED
                    || next == WorkflowStatus.NEEDS_MORE_INFO;
            case NEEDS_MORE_INFO -> next == WorkflowStatus.PENDING;
            case APPROVED,
                 REJECTED        -> false; // terminal states — no more moves
        };
    }

    // ─── Perform the transition or throw a clear error ──────────────────
    public WorkflowStatus transition(WorkflowStatus current,
                                     WorkflowStatus next) {
        if (!canTransition(current, next)) {
            throw new RuntimeException(
                    "Invalid transition: " + current + " → " + next
            );
        }
        return next;
    }
}