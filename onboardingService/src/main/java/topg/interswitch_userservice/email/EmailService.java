package topg.interswitch_userservice.email;


import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class  EmailService {
    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine springTemplateEngine;


    @Async
    public void sendEmail(
            String to,
            String username,
            EmailTemplate emailTemplate,
            String confirmationUrl,
            String activationCode,
            String subject
    ) throws MessagingException {

        // Set the template name
        String templateName = (emailTemplate == null) ? "confirm_email" : emailTemplate.name();

        // Create a new MIME email message
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(
                message,
                MimeMessageHelper.MULTIPART_MODE_MIXED,
                StandardCharsets.UTF_8.name()
        );

        // Populate template variables
        Map<String, Object> properties = new HashMap<>();
        properties.put("username", username);
        properties.put("confirmationUrl", confirmationUrl);
        properties.put("activation_code", activationCode);

        // Set the email template context
        Context context = new Context();
        context.setVariables(properties);
        String htmlContent = springTemplateEngine.process(templateName, context);

        // Set email properties
        messageHelper.setTo(to);
        messageHelper.setSubject(subject);
        messageHelper.setText(htmlContent, true);
        messageHelper.setFrom("ayomidefalade@gmail.com");

        // Send the email
        javaMailSender.send(message);
    }
}
