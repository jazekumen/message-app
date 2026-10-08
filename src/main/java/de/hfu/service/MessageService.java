package de.hfu.service;

import de.hfu.model.Message;
import de.hfu.model.User;

import java.util.Date;
import java.util.List;

/**
 * Business logic for users and messages.
 * Same method names as the HFU backend used in the course, so the
 * controller code did not have to change.
 */
public interface MessageService {

    /** All messages, newest first. */
    List<Message> findAllMessages();

    /** Messages written after the given date, newest first (used by the Ajax polling). */
    List<Message> findLatestMessages(Date date);

    void saveMessage(Message message);

    List<User> findAllUsers();

    /** @return the user, or {@code null} if no user has this username */
    User findUserByUsername(String username);

    /**
     * Stores a new user. The password must already be encoded.
     *
     * @throws IllegalArgumentException if the username is already taken
     */
    void createUser(User user);
}
