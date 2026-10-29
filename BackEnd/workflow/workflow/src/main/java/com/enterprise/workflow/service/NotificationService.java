package com.enterprise.workflow.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final JavaMailSender mailSender;

    // ─── Send a simple email ──────────────────────────────────────
    public void sendEmail(String toEmail, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            System.out.println("Email sent to: " + toEmail);
        } catch (Exception e) {
            // don't crash the app if email fails
            System.out.println("Email failed: " + e.getMessage());
        }
    }

    // ─── Notify employee when workflow is approved ────────────────
    public void notifyApproved(String toEmail, String workflowTitle) {
        sendEmail(
                toEmail,
                "Workflow Approved ✅ — " + workflowTitle,
                "Hello,\n\nYour workflow \"" + workflowTitle
                        + "\" has been APPROVED.\n\nThank you."
        );
    }

    // ─── Notify employee when workflow is rejected ────────────────
    public void notifyRejected(String toEmail, String workflowTitle) {
        sendEmail(
                toEmail,
                "Workflow Rejected ❌ — " + workflowTitle,
                "Hello,\n\nYour workflow \"" + workflowTitle
                        + "\" has been REJECTED.\n\nPlease contact your manager."
        );
    }

    // ─── Notify employee when more info is needed ─────────────────
    public void notifyMoreInfoNeeded(String toEmail, String workflowTitle) {
        sendEmail(
                toEmail,
                "More Info Needed 📋 — " + workflowTitle,
                "Hello,\n\nYour workflow \"" + workflowTitle
                        + "\" requires more information.\n\nPlease resubmit with details."
        );
    }
}