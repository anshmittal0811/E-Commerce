package com.notification_service_api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.notification_service_api.dto.PaymentResponse;
import com.notification_service_api.exception.EmailSendException;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service class for sending email notifications.
 * 
 * <p>This service handles:
 * <ul>
 *   <li>Sending order confirmation emails</li>
 *   <li>Building HTML email templates with order details</li>
 *   <li>Email validation and error handling</li>
 * </ul>
 * 
 * <p>Uses Spring's JavaMailSender for email delivery.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    /**
     * The sender email address configured via application properties.
     */
    @Value("${spring.mail.username}")
    private String fromEmail;

    private final JavaMailSender mailSender;

    /**
     * Sends an order confirmation email based on payment response data.
     * 
     * <p>This method builds an HTML email with order details including:
     * <ul>
     *   <li>Order number and date</li>
     *   <li>Total amount paid</li>
     *   <li>Order status</li>
     *   <li>Customer address and phone</li>
     * </ul>
     * 
     * @param paymentResponse the payment data containing order and user information
     * @throws EmailSendException if email sending fails
     */
    public void sendEmail(PaymentResponse paymentResponse) {
        log.info("Preparing order confirmation email for order ID: {}, recipient: {}",
                paymentResponse.getOrderId(), paymentResponse.getUserEmail());

        // Validate required fields
        validatePaymentResponse(paymentResponse);

        String subject = "Your purchase has been successfully completed - Order #" + paymentResponse.getOrderId();
        String htmlMessage = buildEmailTemplate(paymentResponse);

        try {
            sendOrderConfirmation(paymentResponse.getUserEmail(), subject, htmlMessage);
            log.info("Order confirmation email sent successfully - Order ID: {}, Email: {}",
                    paymentResponse.getOrderId(), paymentResponse.getUserEmail());

        } catch (MessagingException e) {
            log.error("Failed to send order confirmation email - Order ID: {}, Email: {}, Error: {}",
                    paymentResponse.getOrderId(), paymentResponse.getUserEmail(), e.getMessage());
            throw new EmailSendException("Failed to send order confirmation email", e);
        }
    }

    // ==================== Private Helper Methods ====================

    /**
     * Validates that the payment response contains all required fields for email.
     * 
     * @param paymentResponse the payment response to validate
     * @throws IllegalArgumentException if required fields are missing
     */
    private void validatePaymentResponse(PaymentResponse paymentResponse) {
        if (paymentResponse == null) {
            throw new IllegalArgumentException("Payment response cannot be null");
        }
        if (paymentResponse.getUserEmail() == null || paymentResponse.getUserEmail().isBlank()) {
            throw new IllegalArgumentException("Recipient email is required");
        }
        if (paymentResponse.getOrderId() == null) {
            throw new IllegalArgumentException("Order ID is required");
        }
    }

    /**
     * Builds the HTML email template with payment/order details.
     * 
     * @param paymentResponse the payment data to populate the template
     * @return the populated HTML email content
     */
    private String buildEmailTemplate(PaymentResponse paymentResponse) {
        String htmlTemplate = """
                <!DOCTYPE html>
                <html>
                <head>
                    <title>Purchase Confirmation</title>
                    <style>
                        body { font-family: Arial, sans-serif; color: #333; }
                        .container { width: 100%%; max-width: 600px; margin: 0 auto; }
                        .header { background-color: #4CAF50; color: white; padding: 10px; text-align: center; }
                        .content { padding: 20px; }
                        .footer { background-color: #f1f1f1; color: #333; text-align: center; padding: 10px; font-size: 12px; }
                        table { width: 100%%; border-collapse: collapse; }
                        th, td { text-align: left; padding: 8px; }
                        th { background-color: #f2f2f2; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Thank you for your purchase, %s!</h1>
                        </div>
                        <div class="content">
                            <p>Hello %s,</p>
                            <p>We are pleased to inform you that your purchase has been successfully processed. Below are the details of your order:</p>
                            <table>
                                <tr>
                                    <th>Order Number:</th>
                                    <td>%s</td>
                                </tr>
                                <tr>
                                    <th>Order Date:</th>
                                    <td>%s</td>
                                </tr>
                                <tr>
                                    <th>Total Amount Paid:</th>
                                    <td>$%.2f</td>
                                </tr>
                                <tr>
                                    <th>Order Status:</th>
                                    <td>%s</td>
                                </tr>
                            </table>
                            <h3>Customer Details</h3>
                            <p>Address: %s</p>
                            <p>Phone: %s</p>
                        </div>
                        <div class="footer">
                            <p>Thank you for shopping with us.</p>
                        </div>
                    </div>
                </body>
                </html>
                """;

        return String.format(htmlTemplate,
                paymentResponse.getUserName(),
                paymentResponse.getUserName(),
                paymentResponse.getOrderId(),
                paymentResponse.getOrderDate() != null ? paymentResponse.getOrderDate().toString() : "N/A",
                paymentResponse.getTotalAmount() != null ? paymentResponse.getTotalAmount() : 0.0,
                paymentResponse.getOrderStatus() != null ? paymentResponse.getOrderStatus().name() : "COMPLETED",
                paymentResponse.getUserAddress() != null ? paymentResponse.getUserAddress() : "N/A",
                paymentResponse.getUserPhone() != null ? paymentResponse.getUserPhone() : "N/A");
    }

    /**
     * Sends an HTML email using JavaMailSender.
     * 
     * @param toEmail the recipient email address
     * @param subject the email subject
     * @param htmlContent the HTML email body
     * @throws MessagingException if email sending fails
     * @throws IllegalArgumentException if required parameters are invalid
     * @throws IllegalStateException if sender email is not configured
     */
    private void sendOrderConfirmation(String toEmail, String subject, String htmlContent) throws MessagingException {
        // Validate parameters
        if (toEmail == null || toEmail.isBlank()) {
            throw new IllegalArgumentException("Recipient email cannot be null or blank");
        }
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("Email subject cannot be null or blank");
        }
        if (htmlContent == null || htmlContent.isBlank()) {
            throw new IllegalArgumentException("Email content cannot be null or blank");
        }
        if (fromEmail == null || fromEmail.isBlank()) {
            throw new IllegalStateException("Sender email is not configured. Please set spring.mail.username");
        }

        log.debug("Sending email - From: {}, To: {}, Subject: {}", fromEmail, toEmail, subject);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(toEmail);
        helper.setFrom(fromEmail);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);

        log.debug("Email sent successfully to: {}", toEmail);
    }

}
