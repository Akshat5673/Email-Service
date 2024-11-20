package com.nineleaps.email_service;

import com.nineleaps.email_service.services.impl.EmailServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

@SpringBootTest
public class EmailSenderTest {

    @Autowired
    private EmailServiceImpl emailService;

    @Test
    void emailSendTest(){
        System.out.println("Sending Email!");
        emailService.sendEmail("example@gmail.com","Test Email","This is a demo email !");
    }

    @Test
    void emailSendTestMultiple(){
        System.out.println("Sending email to multiple people!");
        String [] to = new String[3];
        to[0]= "example@gmail.com";
        to[1]= "example@gmail.com";
        to[2]= "example@gmail.com";

        emailService.sendEmail(to,"Another test email.","Demo email for multiple recipients.");
    }

    @Test
    void emailSendHTMLTest(){
        System.out.println("Sending Email with HTML!");
        String html = "" +
                "<h1 style='color:red;border:1px solid red;'>Welcome to Email Sender !</h1>" +
                "<p>\n" +
                " Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut" +
                " labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris" +
                " nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit" +
                " esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt " +
                "in culpa qui officia deserunt mollit anim id est laborum.\n" +
                "</p>\n"+
                "";
        emailService.sendEmailWithHtml("example@gmail.com","Test Email",html);
    }

    @Test
    void sendEmailWithFile(){
        System.out.println("Sending Email with file!");
        emailService.sendEmailWithFile("example@gmail.com","Test Email","Email contains file."
                ,new File("/home/nineleaps/Email-service/email-service/src/main/resources/static/gargoyle-8791108_640.jpg"));
    }

    @Test
    void sendEmailWithStream(){
        System.out.println("Sending Email with file input stream !");
        File file = new File("/home/nineleaps/Email-service/email-service/src/main/resources/static/gargoyle-8791108_640.jpg");
        try {
            InputStream is = new FileInputStream(file);
            emailService.sendEmailWithFile("example@gmail.com","Test Email","Email contains file made from input stream.",is
            );
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

}
