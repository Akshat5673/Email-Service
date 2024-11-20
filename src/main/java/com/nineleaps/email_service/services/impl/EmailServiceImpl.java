package com.nineleaps.email_service.services.impl;

import com.nineleaps.email_service.Payloads.MyMessage;
import com.nineleaps.email_service.services.EmailService;
import jakarta.mail.*;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.IntStream;

import static com.nineleaps.email_service.utils.AppConstants.ENCODING;
import static com.nineleaps.email_service.utils.AppConstants.SENDER;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    @Value("${mail.store.protocol}")
    String protocol;
    @Value("${mail.imaps.host}")
    String host;
    @Value("${mail.imaps.port}")
    String port;
    @Value("${spring.mail.username}")
    String username;
    @Value("${spring.mail.password}")
    String password;

    private final JavaMailSender mailSender;
    @Autowired
    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendEmail(String to, String subject, String message) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(to);
        mailMessage.setSubject(subject);
        mailMessage.setText(message);
        mailMessage.setFrom(SENDER);

        mailSender.send(mailMessage);
        log.info("Email has been sent !");
    }

    @Override
    public void sendEmail(String[] to, String subject, String message) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(to);
        mailMessage.setSubject(subject);
        mailMessage.setText(message);
        mailMessage.setFrom(SENDER);

        mailSender.send(mailMessage);
        log.info("Email has been sent to multiple people !");
    }

    @Override
    public void sendEmailWithHtml(String to, String subject, String htmlContent) {
        MimeMessage mailMessage = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(mailMessage,true,ENCODING);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setFrom(SENDER);
            helper.setText(htmlContent,true);
            mailSender.send(mailMessage);
            log.info("Email has been sent with Html !");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void sendEmailWithFile(String to, String subject, String message, File file) {
        MimeMessage mailMessage = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(mailMessage,true,ENCODING);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setFrom(SENDER);
            helper.setText(message);
            FileSystemResource resource = new FileSystemResource(file);
            helper.addAttachment(Objects.requireNonNull(resource.getFilename()),file);
            mailSender.send(mailMessage);
            log.info("Email has been sent with file!");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendEmailWithFile(String to, String subject, String message, InputStream stream) {
        MimeMessage mailMessage = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(mailMessage,true,ENCODING);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setFrom(SENDER);
            helper.setText(message,true);

            File file = new File("src/main/resources/static/test1.png");
            Files.copy(stream,file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            FileSystemResource resource = new FileSystemResource(file);
            helper.addAttachment(Objects.requireNonNull(resource.getFilename()),file);
            mailSender.send(mailMessage);
            log.info("Email has been sent with file as input stream!");

        } catch (MessagingException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<MyMessage> getInboxMessages() {
        Properties properties = createEmailProperties();
        try (Store store = connectToEmailStore(properties)) {
            Folder inbox = openInbox(store);

            Message[] messages = inbox.getMessages();
            return Arrays.stream(messages)
                    .map(this::mapMessageToMyMessage)
                    .toList();

        } catch (MessagingException e) {
            throw new RuntimeException("Error retrieving inbox messages", e);
        }
    }

    private Properties createEmailProperties() {
        Properties properties = new Properties();
        properties.setProperty("mail.store.protocol", protocol);
        properties.setProperty("mail.imaps.host", host);
        properties.setProperty("mail.imaps.port", port);
        return properties;
    }

    private Store connectToEmailStore(Properties properties) throws MessagingException {
        Session session = Session.getDefaultInstance(properties);
        Store store = session.getStore();
        store.connect(username, password);
        return store;
    }

    private Folder openInbox(Store store) throws MessagingException {
        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_ONLY);
        return inbox;
    }

    private MyMessage mapMessageToMyMessage(Message message) {
        try {
            String content = extractContent(message);
            List<String> files = extractAttachments(message);

            return MyMessage.builder()
                    .subject(message.getSubject())
                    .content(content)
                    .files(files)
                    .build();
        } catch (MessagingException | IOException e) {
            throw new RuntimeException("Error processing message: " + message, e);
        }
    }

    private String extractContent(Message message) throws MessagingException, IOException {
        if (message.isMimeType("text/plain") || message.isMimeType("text/html")) {
            return (String) message.getContent();
        }

        if (message.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) message.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                if (bodyPart.isMimeType("text/plain")) {
                    return (String) bodyPart.getContent();
                }
            }
        }
        return null;
    }

    private List<String> extractAttachments(Message message) throws MessagingException, IOException {
        if (!message.isMimeType("multipart/*")) {
            return Collections.emptyList();
        }

        Multipart multipart = (Multipart) message.getContent();
        return IntStream.range(0, multipart.getCount())
                .mapToObj(i -> {
                    try {
                        BodyPart bodyPart = multipart.getBodyPart(i);
                        if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
                            return saveAttachment(bodyPart);
                        }
                    } catch (MessagingException | IOException e) {
                        throw new RuntimeException("Error extracting attachment", e);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private String saveAttachment(BodyPart bodyPart) throws IOException, MessagingException {
        InputStream stream = bodyPart.getInputStream();
        File file = new File("src/main/resources/static/received/" + bodyPart.getFileName());
        Files.copy(stream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return file.getAbsolutePath();
    }


}
