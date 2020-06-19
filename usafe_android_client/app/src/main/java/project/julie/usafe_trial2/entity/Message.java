package project.julie.usafe_trial2.entity;

public class Message {

    String topic;
    Notification notification;

    public Message() {}

    public Message(String topic, Notification notification) {
        this.topic = topic;
        this.notification = notification;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Notification getNotification() {
        return notification;
    }

    public void setNotification(Notification notification) {
        this.notification = notification;
    }
}
