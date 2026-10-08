package de.hfu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
@ServletComponentScan
public class MessageApp {
    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(MessageApp.class, args);

        // Print existing messages once at startup (Praktikum 7)
        try {
            MessagePrinter messageprinter = context.getBean(MessagePrinter.class);
            messageprinter.printMessages();
        } catch (Exception e) {
            System.err.println("Could not load messages at startup: " + e.getMessage());
        }
    }
}
