package de.hfu;

import de.hfu.model.Message;
import de.hfu.model.User;
import de.hfu.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class MeinController {

    private MessageService messageService;
    private PasswordEncoder passwordEncoder;

    // Setter for MessageService
    @Autowired
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    // Setter for PasswordEncoder (hashes passwords before they are stored)
    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @RequestMapping("/nachrichtenListe.html")
    public ModelAndView allMessages() {
        ModelAndView mav = new ModelAndView();
        List<Message> messages = messageService.findAllMessages();
        mav.addObject("messages", messages);
        mav.addObject("lastClientMessage",
                (messages.size() == 0)? 0 : messages.get(0).getDate().getTime());
        mav.setViewName("nachrichtenListe");
        return mav;
    }



    // New method to handle the registration form
    @RequestMapping(value = "/registerForm.html")
    public ModelAndView registerInput() {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("registerForm");
        return mav;
    }

    // New method to handle the registration form submission
    // POST only, so the password never ends up in a URL or browser history
    @PostMapping(value = "/registerSave.html")
    public ModelAndView registerSave(String username, String password, String fullname, String email) {
        ModelAndView mav = new ModelAndView();
        System.out.println("Registering user: " + username);
        try {
            if (isBlank(username) || isBlank(password) || isBlank(email)) {
                throw new IllegalArgumentException("Benutzername, Passwort und E-Mail sind Pflichtfelder.");
            }
            User registerUser = new User(username.trim(), passwordEncoder.encode(password), fullname, email.trim());
            messageService.createUser(registerUser);
            mav.setViewName("registerSuccess");
        } catch (Exception exception) {
            mav.addObject("fehler", exception.getMessage());
            // keep what the user typed (except the password)
            mav.addObject("username", username);
            mav.addObject("fullname", fullname);
            mav.addObject("email", email);
            mav.setViewName("registerForm");
            System.out.println("Cannot create user: " + exception.getMessage());
        }
        return mav;
    }

    @RequestMapping(value = "/messageForm.html")
    public String messageForm() {
        return "messageForm";
    }


    @PostMapping("/createMessage.html")
    public String createMessage(@RequestParam("messageText") String text,
                                @RequestParam(value = "location", required = false) String location,
                                Principal principal) {
        try {
            if (isBlank(text) || text.length() > 140) {
                throw new IllegalArgumentException("Nachricht muss 1 bis 140 Zeichen lang sein");
            }
            System.out.println("user " + principal.getName() + " created message: " + text);
            User user = messageService.findUserByUsername(principal.getName());
            Message message = new Message(text, new Date(), user);
            if (location != null && !location.trim().isEmpty()) {
                message.setLocation(location.trim());
            }
            messageService.saveMessage(message);
            return "redirect:/nachrichtenListe.html";
        } catch (Exception e) {
            System.err.println("Error while creating message: " + e.getMessage());
            return "redirect:/messageForm.html?error=true";
        }
    }


    // Praktikum 10, Schritt 2: list all users to check that registration worked
    @RequestMapping("/users.html")
    public ModelAndView allUsers() {
        ModelAndView mav = new ModelAndView();
        mav.addObject("users", messageService.findAllUsers());
        mav.setViewName("users");
        return mav;
    }

    @RequestMapping(value = "/login.html")
    public String login() {
        return "login";
    }

    @RequestMapping("/ajax/messages.json")
    @ResponseBody
    public List<Map<String, Object>> messages(@RequestParam(required = false) Long lastClientMessage) {
        if (lastClientMessage == null) {
            lastClientMessage = 0L;
        }
        // Only send the fields the page needs. Returning the Message objects directly
        // would also send every author's email and password hash to the browser.
        List<Map<String, Object>> result = new ArrayList<>();
        for (Message message : messageService.findLatestMessages(new Date(lastClientMessage))) {
            Map<String, Object> user = new LinkedHashMap<>();
            user.put("username", message.getUser().getUsername());
            user.put("fullname", message.getUser().getFullname());

            Map<String, Object> json = new LinkedHashMap<>();
            json.put("text", message.getText());
            json.put("location", message.getLocation());
            json.put("date", message.getDate().getTime());
            json.put("user", user);
            result.add(json);
        }
        return result;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
