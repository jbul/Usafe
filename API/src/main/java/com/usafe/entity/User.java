package com.usafe.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@NamedQueries({
        @NamedQuery(name = User.SHOW_USER_NAMED_QUERY, query = "from User where email =:email")})

@Entity
public class User {

    public static final String SHOW_USER_NAMED_QUERY = "User.FindByEmail";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long userId;
    private String firstName;
    private String lastName;
    private String password;
    private String phoneNo;

    // Length decided by: https://stackoverflow.com/questions/386294/what-is-the-maximum-length-of-a-valid-email-address
    @Column(unique = true, length = 64)
    private String email;

    @JsonIgnore
    @ManyToMany(targetEntity = User.class, fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    private List<User> users;

    @OneToMany
    private List<Journey> journeys;

    public User() {
        journeys = new ArrayList<>();
        users = new ArrayList<>();
    }

    public User(String firstName, String lastName, String email, String password, String phoneNo) {
        super();
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.phoneNo = phoneNo;
    }

    public User(String firstName, String lastName, String email, String password, String phoneNo, List<User> users) {
        this(firstName, lastName, email, password, phoneNo);
        this.users = users;
    }


    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public List<Journey> getJourneys() {
        return journeys;
    }

    public void setJourneys(List<Journey> journeys) {
        this.journeys = journeys;
    }

    @Override
    public boolean equals(Object obj) {
        return this.getUserId() == ((User)obj).getUserId();
    }

    @Override
    public String toString() {
        return "User [email: " + this.email
                + ", last name: " + this.lastName
                + ", first name: " + this.firstName + "]";
    }
}
