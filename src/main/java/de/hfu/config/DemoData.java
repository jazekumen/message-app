package de.hfu.config;

import de.hfu.model.Message;
import de.hfu.model.User;
import de.hfu.service.MessageService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Fills an empty database with two demo users and a few messages,
 * so the app shows something right after the first start.
 * Turn off with app.demo-data=false.
 */
@Component
@Order(1)
@ConditionalOnProperty(name = "app.demo-data", havingValue = "true", matchIfMissing = true)
public class DemoData implements ApplicationRunner {

    public static final String DEMO_PASSWORD = "demo";

    private final MessageService messageService;
    private final PasswordEncoder passwordEncoder;

    public DemoData(MessageService messageService, PasswordEncoder passwordEncoder) {
        this.messageService = messageService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!messageService.findAllUsers().isEmpty()) {
            return; // keep existing data
        }
        User heike = createUser("heike", "Heike Schmidt", "heike@example.com");
        User stefan = createUser("stefan", "Stefan Schneider", "stefan@example.com");

        post(heike, "Knetradierer nicht mehr neben offene Haribo-Tüte legen!", "Furtwangen", 3);
        post(stefan, "Die Lage ist hoffnungslos, aber nicht ernst. (Paul Watzlawick)", null, 2);
        post(heike, "Zuhause ist da, wo man sein WLAN-Passwort kennt", "Villingen-Schwenningen", 1);
    }

    private User createUser(String username, String fullname, String email) {
        User user = new User(username, passwordEncoder.encode(DEMO_PASSWORD), fullname, email);
        messageService.createUser(user);
        return user;
    }

    private void post(User author, String text, String location, int daysAgo) {
        Date date = new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(daysAgo));
        Message message = new Message(text, date, author);
        message.setLocation(location);
        messageService.saveMessage(message);
    }
}
