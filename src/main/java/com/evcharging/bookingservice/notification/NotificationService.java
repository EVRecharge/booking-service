package com.evcharging.bookingservice.notification;

import com.evcharging.bookingservice.client.DirectoryClient;
import com.evcharging.bookingservice.client.ProviderContact;
import com.evcharging.bookingservice.client.UserContact;
import com.evcharging.bookingservice.entity.Booking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.Body;
import software.amazon.awssdk.services.sesv2.model.Content;
import software.amazon.awssdk.services.sesv2.model.Destination;
import software.amazon.awssdk.services.sesv2.model.EmailContent;
import software.amazon.awssdk.services.sesv2.model.Message;
import software.amazon.awssdk.services.sesv2.model.SendEmailRequest;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

/**
 * Sends the two booking-lifecycle notifications the product requires: the provider is emailed and
 * texted when a driver requests a slot, and the driver is emailed and texted back once the provider
 * approves it. Every send is best-effort -- a broken phone number or an SES/SNS hiccup is logged and
 * swallowed here so it never fails the booking API call that triggered it.
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private static final DateTimeFormatter WHEN_FORMAT =
            DateTimeFormatter.ofPattern("EEE, d MMM yyyy 'at' h:mm a", Locale.ENGLISH);

    @Autowired
    private SesV2Client sesClient;

    @Autowired
    private SnsClient snsClient;

    @Autowired
    private DirectoryClient directoryClient;

    @Value("${app.notifications.enabled}")
    private boolean enabled;

    @Value("${app.notifications.email-from}")
    private String emailFrom;

    /** Fire-and-forget: looks up the provider's (and driver's, for the name) contact info and notifies the provider of a new request. */
    @Async
    public void notifyProviderOfNewBooking(Booking booking) {
        if (!enabled) {
            return;
        }
        ProviderContact provider = directoryClient.findProvider(booking.getProviderId());
        UserContact driver = directoryClient.findUser(booking.getUserId());
        if (provider == null) {
            return;
        }
        String when = booking.getStartTime().format(WHEN_FORMAT);
        String driverName = driver != null ? driver.fullName() : "A driver";
        String subject = "New charging request from " + driverName;
        String body = driverName + " has requested a charging slot at your station.\n\n"
                + "When: " + when + "\n"
                + "Duration: " + booking.getDurationInHours() + " hour(s)\n"
                + (booking.getNotes() != null && !booking.getNotes().isBlank() ? "Notes: " + booking.getNotes() + "\n" : "")
                + "\nLog in to EVRecharge India to confirm or decline this request.";

        sendEmail(provider.getEmail(), subject, body);
        sendSms(provider.getPhoneNumber(),
                "EVRecharge India: New charging request from " + driverName + " for " + when + ". Open the app to respond.");
    }

    /** Fire-and-forget: looks up the driver's (and provider's, for the name) contact info and notifies the driver of approval. */
    @Async
    public void notifyUserOfApproval(Booking booking) {
        if (!enabled) {
            return;
        }
        UserContact driver = directoryClient.findUser(booking.getUserId());
        ProviderContact provider = directoryClient.findProvider(booking.getProviderId());
        if (driver == null) {
            return;
        }
        String when = booking.getStartTime().format(WHEN_FORMAT);
        String providerName = provider != null ? provider.displayName() : "The charging station";
        String subject = "Your booking is confirmed";
        String body = providerName + " has confirmed your charging booking.\n\n"
                + "When: " + when + "\n"
                + "Duration: " + booking.getDurationInHours() + " hour(s)\n"
                + (booking.getAddress() != null ? "Where: " + booking.getAddress()
                    + (booking.getCity() != null ? ", " + booking.getCity() : "") + "\n" : "")
                + "\nSee you there!";

        sendEmail(driver.getEmail(), subject, body);
        sendSms(driver.getPhoneNumber(),
                "EVRecharge India: " + providerName + " confirmed your charging booking for " + when + ".");
    }

    private void sendEmail(String to, String subject, String body) {
        if (!enabled || to == null || to.isBlank()) {
            return;
        }
        try {
            sesClient.sendEmail(SendEmailRequest.builder()
                    .fromEmailAddress(emailFrom)
                    .destination(Destination.builder().toAddresses(to).build())
                    .content(EmailContent.builder()
                            .simple(Message.builder()
                                    .subject(Content.builder().data(subject).build())
                                    .body(Body.builder().text(Content.builder().data(body).build()).build())
                                    .build())
                            .build())
                    .build());
        } catch (Exception e) {
            log.warn("Failed to send booking email to {}: {}", to, e.getMessage());
        }
    }

    private void sendSms(String phoneNumber, String message) {
        if (!enabled || phoneNumber == null || phoneNumber.isBlank()) {
            return;
        }
        try {
            snsClient.publish(PublishRequest.builder()
                    .phoneNumber(normalizePhoneNumber(phoneNumber))
                    .message(message)
                    .messageAttributes(Map.of("AWS.SNS.SMS.SMSType",
                            MessageAttributeValue.builder().dataType("String").stringValue("Transactional").build()))
                    .build());
        } catch (Exception e) {
            log.warn("Failed to send booking SMS to {}: {}", phoneNumber, e.getMessage());
        }
    }

    /** SNS requires E.164 (+<country code><number>); assume India (+91) when no country code was entered. */
    private String normalizePhoneNumber(String raw) {
        String digits = raw.replaceAll("[^0-9+]", "");
        if (digits.startsWith("+")) {
            return digits;
        }
        if (digits.length() == 10) {
            return "+91" + digits;
        }
        return "+" + digits;
    }
}
