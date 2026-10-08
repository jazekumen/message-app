package de.hfu.model;

import java.util.Date;

/**
 * A single post. Mirrors the data model from the course
 * (text, location, date, author).
 */
public class Message {

    private Long id;
    private String text;
    private String location;
    private Date date;
    private User user;

    public Message() {
    }

    public Message(String text, Date date, User user) {
        this.text = text;
        this.date = date;
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "Message[" + text + " by " + (user == null ? "?" : user.getUsername()) + "]";
    }
}
