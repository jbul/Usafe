package com.usafe.controller;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.usafe.entity.Journey;
import com.usafe.entity.Notification;
import com.usafe.entity.Path;
import com.usafe.entity.User;
import com.usafe.repository.JourneyRepository;
import com.usafe.repository.NotificationRepository;
import com.usafe.repository.PathRepository;
import com.usafe.repository.UserRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@RestController
public class PushController {

    private static final String FOLLOW_USER_NOTIFICATION_TYPE = "followUser";
    private static final String USER_RUNNING_NOTIFICATION_TYPE = "userRunning";
    private static final String USER_NOT_FOLLOWING_PATH_NOTIFICATION_TYPE = "userNotFollowingRoute";
    private static final String USER_FINISHED_JOURNEY = "journeyFinished";

    private UserRepository userRepository;
    private JourneyRepository journeyRepository;
    private NotificationRepository notificationRepository;
    private PathRepository pathRepository;

    static {
        try {
            Resource resource = new ClassPathResource("firebase_file.json");

            InputStream input = resource.getInputStream();

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(input))
                    .setDatabaseUrl("PUT YOUR FIREBASE URL HERE")
                    .build();

            FirebaseApp.initializeApp(options);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Autowired
    public PushController(UserRepository userRepository,
                          JourneyRepository journeyRepository, NotificationRepository notificationRepository, PathRepository pathRepository) {
        this.userRepository = userRepository;
        this.journeyRepository = journeyRepository;
        this.notificationRepository = notificationRepository;
        this.pathRepository = pathRepository;
    }

    @PostMapping("/follow")
    public String sendPush(String ids, String user, @RequestParam("latlngs") List<String> latLngList, @RequestParam long userID,
                           @RequestParam("lat") double latitude, @RequestParam("long") double longitude) throws Exception {

        List<Coordinate> pts = new ArrayList<>();

        GeometryFactory gf = new GeometryFactory();
        Path firstPath = null;
        Path path = null;

        for (String l : latLngList) {
            Double[] r = convert(l);
            Coordinate c = new Coordinate(r[0], r[1]);
            pts.add(c);

            if (firstPath == null) {
                firstPath = new Path(c);
                path = firstPath;
            } else if (path != null) {
                Path newPath = new Path(c);
                path.setNextPath(newPath);
                path = newPath;
            }
            pathRepository.save(path);
        }

        Geometry g = gf.createLineString(pts.toArray(new Coordinate[]{}));
        User currentUser = userRepository.findById(userID);

        Journey currentJourney = journeyRepository.findByUserAndActiveTrue(currentUser);



        if (currentJourney == null) {
            Journey userNewJourney = new Journey(currentUser, latitude, longitude, g);
            userNewJourney.setActive(true);
            userNewJourney.setPath(firstPath);

            if (ids != null) {
                List<User> userIds = parseUserIdList(ids);

                String messageContent = user + " is starting a journey. Follow " + user + "!";
                Notification notification = new Notification(userIds,
                        FOLLOW_USER_NOTIFICATION_TYPE,
                        messageContent);
                notificationRepository.save(notification);
                userNewJourney.getNotificationList().add(notification);

                for (User id : userIds) {
//            // See documentation on defining a message payload.
                    Message message = Message.builder()
                            .putData("message", messageContent)
                            .putData("userId", String.valueOf(userID))
                            .putData("type", FOLLOW_USER_NOTIFICATION_TYPE)
                            .setTopic(String.valueOf(id.getPhoneNo()))
                            .build();

                    // Send a message to the devices subscribed to the provided topic.
                    String response = FirebaseMessaging.getInstance().send(message);

                    // Response is a message ID string.
                    System.out.println("Successfully sent message: " + response);
                }
            }

            journeyRepository.save(userNewJourney);

            return "Sent";
        } else {
            System.out.println("Journey: " + currentJourney.getId() + " already active");
            return "Journey Already in progress";
        }
    }

    @PostMapping("/notificationUserIsRunning")
    public String notifyFollowersWhenUserIsRunning(String user, String followersID, @RequestParam long userID) throws FirebaseMessagingException {

        if (followersID != null) {

            User u = userRepository.findById(userID);

            List<User> followers = parseUserIdList(followersID);

            Journey journey = journeyRepository.findByUserAndActiveTrue(u);
            String messageContent = user + " is running! Click to give a call and check everything is fine";
            Notification notification = new Notification(followers, USER_RUNNING_NOTIFICATION_TYPE, messageContent);

            notificationRepository.save(notification);

            for (User followerId : followers) {

                // See documentation on defining a message payload.
                Message message = Message.builder()
                        .putData("message", user + " is running! Click to give a call and check everything is fine")
                        .putData("userId", String.valueOf(userID))
                        .putData("type", USER_RUNNING_NOTIFICATION_TYPE)
                        .putData("phoneNo", u.getPhoneNo())
                        .setTopic(String.valueOf(followerId.getPhoneNo()))
                        .build();

                // Send a message to the devices subscribed to the provided topic.
                String response = FirebaseMessaging.getInstance().send(message);


                // Response is a message ID string.
                System.out.println("Successfully sent message: " + response);
            }
            journey.getNotificationList().add(notification);

            journeyRepository.save(journey);
        }

        return "Sent";
    }


    @PostMapping("/notificationUserNotFollowingRoute")
    public String notifyFollowersWhenUserNotFollowingRoute(String user, String followersID, @RequestParam long userID) throws FirebaseMessagingException {
        if (followersID != null) {
            User u = userRepository.findById(userID);
            Journey journey = journeyRepository.findByUserAndActiveTrue(u);

            List<User> followerIds = parseUserIdList(followersID);

            String messageContent = user + " is not following the route! Click to give a call and check everything is fine";

            Notification notification = new Notification(followerIds,
                    USER_NOT_FOLLOWING_PATH_NOTIFICATION_TYPE,
                    messageContent);

            notificationRepository.save(notification);

            for (User followerId : followerIds) {
                Message message = Message.builder()
                        .putData("message", messageContent)
                        .putData("userId", String.valueOf(userID))
                        .putData("type", USER_NOT_FOLLOWING_PATH_NOTIFICATION_TYPE)
                        .putData("phoneNo", u.getPhoneNo())
                        .setTopic(String.valueOf(followerId.getPhoneNo()))
                        .build();

                // Send a message to the devices subscribed to the provided topic.
                String response = FirebaseMessaging.getInstance().send(message);
                // Response is a message ID string.
                System.out.println("Successfully sent message: " + response);
            }

            journey.getNotificationList().add(notification);
            journeyRepository.save(journey);
        }
        return "Sent";
    }

    @PostMapping("/notificationUserTerminatedRoute")
    public String notificationUserTerminatedRoute(String user, String followersID, @RequestParam long userID) throws FirebaseMessagingException {
        if (followersID != null) {
            User u = userRepository.findById(userID);
            Journey journey = journeyRepository.findByUserAndActiveTrue(u);

            List<User> followerIds = parseUserIdList(followersID);

            String messageContent = user + " has arrived at destination. Thanks for following!";

            Notification notification = new Notification(followerIds,
                    USER_NOT_FOLLOWING_PATH_NOTIFICATION_TYPE,
                    messageContent);

            notificationRepository.save(notification);

            for (User followerId : followerIds) {
                Message message = Message.builder()
                        .putData("message", messageContent)
                        .putData("userId", String.valueOf(userID))
                        .putData("type", USER_FINISHED_JOURNEY)
                        .putData("phoneNo", u.getPhoneNo())
                        .setTopic(String.valueOf(followerId.getPhoneNo()))
                        .build();

                // Send a message to the devices subscribed to the provided topic.
                String response = FirebaseMessaging.getInstance().send(message);
                // Response is a message ID string.
                System.out.println("Successfully sent message: " + response);
            }

            journey.getNotificationList().add(notification);
            journeyRepository.save(journey);
        }
        return "Sent";
    }


    private Double[] convert(String s) {
        Double[] res = new Double[2];
        String[] arr = s.substring(1, s.length() - 1).split(",");
        res[0] = Double.valueOf(arr[0]);
        res[1] = Double.valueOf(arr[1]);
        return res;
    }

    private List<User> parseUserIdList(String userIds) {
        List<User> result = new ArrayList<>();

        String[] userIdArray = userIds.split(",");

        for (String id : userIdArray) {
            result.add(userRepository.findByPhoneNo(id));
        }

        return result;
    }


}
