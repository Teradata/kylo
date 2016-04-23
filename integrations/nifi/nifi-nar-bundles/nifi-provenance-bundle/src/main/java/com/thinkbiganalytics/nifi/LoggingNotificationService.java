package com.thinkbiganalytics.nifi;

import org.apache.nifi.bootstrap.notification.AbstractNotificationService;
import org.apache.nifi.bootstrap.notification.NotificationContext;
import org.apache.nifi.bootstrap.notification.NotificationFailedException;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.ValidationContext;
import org.apache.nifi.components.ValidationResult;
import org.apache.nifi.processor.exception.ProcessException;
import java.util.*;

/**
 * Created by sr186054 on 3/4/16.
 */
public class LoggingNotificationService  extends AbstractNotificationService {


    public static final PropertyDescriptor A_PROPERTY = new PropertyDescriptor.Builder()
            .name("PROP Hostname")
            .description("The hostname of the SMTP Server that is used to send Email Notifications")
            .required(false)
            .build();

    /**
     * Mapping of the mail properties to the NiFi PropertyDescriptors that will be evaluated at runtime
     */
    private static final Map<String, PropertyDescriptor> propertyToContext = new HashMap<>();

    static {
        propertyToContext.put("a.property", A_PROPERTY);
    }

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        final List<PropertyDescriptor> properties = new ArrayList<>();
        properties.add(A_PROPERTY);
        return properties;
    }

    @Override
    protected Collection<ValidationResult> customValidate(final ValidationContext context) {
        final List<ValidationResult> errors = new ArrayList<>(super.customValidate(context));

    /*    final String to = context.getProperty(TO).getValue();
        final String cc = context.getProperty(CC).getValue();
        final String bcc = context.getProperty(BCC).getValue();

        if (to == null && cc == null && bcc == null) {
            errors.add(new ValidationResult.Builder().subject("To, CC, BCC").valid(false).explanation("Must specify at least one To/CC/BCC address").build());
        }
*/
        return errors;
    }


    @Override
    public void notify(final NotificationContext context, final String subject, final String messageText) throws NotificationFailedException {

        try {
            System.out.println("******************************************** ERROR!!!!!!!!!!!!!!!!!! ");
            System.out.println("******************************************** ERROR subject "+subject);
            System.out.println("******************************************** ERROR messageText "+messageText);
        } catch (final ProcessException e) {
            throw new NotificationFailedException("Failed to send E-mail Notification", e);
        }
    }

}
