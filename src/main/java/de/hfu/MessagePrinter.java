package de.hfu;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import de.hfu.service.MessageService;
import de.hfu.model.Message;



import java.util.List;

@Component
public class MessagePrinter {

    @Autowired
    MessageService messageService;

    public void setMessageService(
            MessageService messageService){
        this.messageService = messageService;
    }

    public void printMessages() {
        List<Message> messages = messageService.findAllMessages();
        for (Message message : messages) {
            System.out.println("Message: " + message.getText() + ", Benutzer: " + message.getUser().getUsername());
        }
    }
}
