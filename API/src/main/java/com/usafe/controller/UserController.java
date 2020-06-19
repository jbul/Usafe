package com.usafe.controller;

import com.usafe.LatLng;
import com.usafe.entity.User;
import com.usafe.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;

@RestController
public class UserController {

    @Context
    private UriInfo context;
    private UserRepository userRepository;

    @Autowired
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("saveUser")
    public String createUser(@RequestParam("email") String email,
                             @RequestParam("phoneNo") String phoneNo,
                             @RequestParam("firstName") String firstName,
                             @RequestParam("lastName") String lastName,
                             @RequestParam("password") String password) {
        userRepository.save(new User(firstName, lastName, email, password, phoneNo));
        return "User saved";
    }

    @PostMapping(name = "loginUser", produces = {"application/json"})
    public User loginUser(@RequestParam("email") String email,
                          @RequestParam("password") String password) {

        User u = userRepository.findByEmail(email);
        if (u != null && u.getPassword().equals(password)) {
            System.out.println("FOUND");
            return u;
        } else {
            return null;
        }
    }

    @GetMapping("isUserExist/{phoneNo}")
    public Boolean isUserExist(@PathVariable("phoneNo") String phoneNo) {
        return userRepository.findByPhoneNo(phoneNo) != null;
    }

    @PostMapping("addFriend")
    public String addFriend(@RequestParam("phoneNo") String phoneNo, @RequestParam("email") String email) {
        User currentUser = userRepository.findByEmail(email);
        User friend = userRepository.findByPhoneNo(phoneNo);
        if (currentUser.getUsers() == null) {
            currentUser.setUsers(new ArrayList<>());
        }
        currentUser.getUsers().add(friend);
        userRepository.save(currentUser);
        return "Friend added";
    }

    @GetMapping("isFriendAdded/{userEmail}/{friendPhoneNo}")
    public Boolean isFriendAdded(@PathVariable("userEmail") String userEmail, @PathVariable("friendPhoneNo") String friendPhoneNo) {
        User currentUser = userRepository.findByEmail(userEmail);
        User friend = userRepository.findByPhoneNo(friendPhoneNo);
        return currentUser.getUsers().contains(friend);
    }

    @PostMapping("findAllFriends")
    public List<User> getAllFriends(@RequestParam("userID") long userID) {
        User currentUser = userRepository.findById(userID);
        return currentUser.getUsers();
    }
}
