package de.hfu.service;

import de.hfu.model.Message;
import de.hfu.model.User;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * {@link MessageService} backed by the embedded H2 database, using plain JDBC.
 * The bean is named "messageService" because the servlet looks it up by that name.
 */
@Service("messageService")
public class JdbcMessageService implements MessageService {

    /** SQLSTATE for "unique constraint violated" */
    private static final String DUPLICATE_KEY = "23505";

    private static final String SELECT_MESSAGES =
            "SELECT m.id, m.text, m.location, m.created_at, "
            + "u.id AS user_id, u.username, u.fullname, u.email "
            + "FROM messages m JOIN users u ON u.id = m.user_id ";

    private static final String NEWEST_FIRST = " ORDER BY m.created_at DESC, m.id DESC";

    private final DataSource dataSource;

    public JdbcMessageService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<Message> findAllMessages() {
        return queryMessages(SELECT_MESSAGES + NEWEST_FIRST);
    }

    @Override
    public List<Message> findLatestMessages(Date date) {
        return queryMessages(SELECT_MESSAGES + "WHERE m.created_at > ?" + NEWEST_FIRST,
                new Timestamp(date.getTime()));
    }

    @Override
    public void saveMessage(Message message) {
        if (message.getUser() == null || message.getUser().getId() == null) {
            throw new IllegalArgumentException("Message needs a saved author");
        }
        String sql = "INSERT INTO messages (text, location, created_at, user_id) VALUES (?, ?, ?, ?)";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, message.getText());
            ps.setString(2, message.getLocation());
            ps.setTimestamp(3, new Timestamp(message.getDate().getTime()));
            ps.setLong(4, message.getUser().getId());
            ps.executeUpdate();
            message.setId(generatedId(ps));
        } catch (SQLException e) {
            throw new IllegalStateException("Could not save message", e);
        }
    }

    @Override
    public List<User> findAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, username, password, fullname, email FROM users ORDER BY username";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(mapUser(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not load users", e);
        }
        return users;
    }

    @Override
    public User findUserByUsername(String username) {
        String sql = "SELECT id, username, password, fullname, email FROM users WHERE username = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapUser(rs) : null;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not load user " + username, e);
        }
    }

    @Override
    public void createUser(User user) {
        String sql = "INSERT INTO users (username, password, fullname, email) VALUES (?, ?, ?, ?)";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getFullname());
            ps.setString(4, user.getEmail());
            ps.executeUpdate();
            user.setId(generatedId(ps));
        } catch (SQLException e) {
            if (DUPLICATE_KEY.equals(e.getSQLState())) {
                throw new IllegalArgumentException(
                        "Der Benutzername \"" + user.getUsername() + "\" ist bereits vergeben.");
            }
            throw new IllegalStateException("Could not create user", e);
        }
    }

    // ---------------------------------------------------------------- helpers

    private List<Message> queryMessages(String sql, Object... params) {
        List<Message> messages = new ArrayList<>();
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    messages.add(mapMessage(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not load messages", e);
        }
        return messages;
    }

    private static Message mapMessage(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("user_id"));
        user.setUsername(rs.getString("username"));
        user.setFullname(rs.getString("fullname"));
        user.setEmail(rs.getString("email"));
        // the password hash is deliberately not loaded together with messages

        Message message = new Message(rs.getString("text"),
                new Date(rs.getTimestamp("created_at").getTime()), user);
        message.setId(rs.getLong("id"));
        message.setLocation(rs.getString("location"));
        return message;
    }

    private static User mapUser(ResultSet rs) throws SQLException {
        User user = new User(rs.getString("username"), rs.getString("password"),
                rs.getString("fullname"), rs.getString("email"));
        user.setId(rs.getLong("id"));
        return user;
    }

    private static Long generatedId(PreparedStatement ps) throws SQLException {
        try (ResultSet keys = ps.getGeneratedKeys()) {
            return keys.next() ? keys.getLong(1) : null;
        }
    }
}
