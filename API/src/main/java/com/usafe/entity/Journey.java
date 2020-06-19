package com.usafe.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.locationtech.jts.geom.Geometry;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Journey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JsonIgnore
    private User user;
    private double latitude;
    private double longitude;

    @Column(length = 10000000)
    private Geometry route;

    @OneToMany(fetch = FetchType.EAGER)
    private List<Notification> notificationList;

    @OneToOne(cascade = CascadeType.ALL)
    private Path path;

    private boolean active;

    public Journey() {
        this.notificationList = new ArrayList<>();
    }

    public Journey(User user, double latitude, double longitude, Geometry route) {
        this.notificationList = new ArrayList<>();
        this.user = user;
        this.latitude = latitude;
        this.longitude = longitude;
        this.route = route;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public Geometry getRoute() {
        return route;
    }

    public void setRoute(Geometry route) {
        this.route = route;
    }

    public List<Notification> getNotificationList() {
        return notificationList;
    }

    public void setNotificationList(List<Notification> notificationList) {
        this.notificationList = notificationList;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
