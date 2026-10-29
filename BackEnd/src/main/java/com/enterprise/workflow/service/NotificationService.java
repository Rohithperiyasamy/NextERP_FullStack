package com.enterprise.workflow.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
@RequiredArgsConstructor
public class NotificationService {

    private final JavaMailSender mailSender;
    private static final Logger log =
            LoggerFactory.getLogger(NotificationService.class);

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
            log.error("Failed to send email to {}", toEmail, e);
        }
    }

    // ─── Notify employee when workflow is approved ────────────────
    public void notifyApproved(String toEmail, String workflowTitle) {
        sendEmail(
                toEmail,
                "Update on your workflow,  — " + workflowTitle,
                "Hello,\n\nYour workflow \"" + workflowTitle
                        + "\" has been Approved ✅.\n\nThank you."
        );
    }

    // ─── Notify employee when workflow is rejected ────────────────
    public void notifyRejected(String toEmail, String workflowTitle) {
        sendEmail(
                toEmail,
                "Update on your Workflow  — " + workflowTitle,
                "Hello,\n\nYour workflow \"" + workflowTitle
                        + "\" has been REJECTED.\n\nPlease contact your manager."
        );
    }

    // ─── Notify employee when more info is needed ─────────────────
    public void notifyMoreInfoNeeded(String toEmail, String workflowTitle) {
        sendEmail(
                toEmail,
                "Update on your Workflow — " + workflowTitle,
                "Hello,\n\nYour workflow \"" + workflowTitle
                        + "\" requires more information.\n\nPlease resubmit with details."
        );
    }
}