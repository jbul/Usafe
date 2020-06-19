package com.usafe.entity;

import javax.persistence.*;
import java.util.List;

@Entity
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToMany(cascade = CascadeType.ALL)
    private List<User> receiverIds;
    private String type;
    private String message;

    public Notification() {
    }

    public Notification(List<User> receiverIds, String type, String message) {
        this.receiverIds = receiverIds;
        this.type = type;
        this.message = message;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<User> getReceiverIds() {
        return receiverIds;
    }

    public void setReceiverIds(List<User> receiverIds) {
        this.receiverIds = receiverIds;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", receiverIds=" + receiverIds +
                ", type='" + type + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
