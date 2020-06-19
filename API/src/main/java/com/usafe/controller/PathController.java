package com.usafe.controller;

import com.usafe.entity.Journey;
import com.usafe.entity.User;
import com.usafe.repository.JourneyRepository;
import com.usafe.repository.UserRepository;
import org.locationtech.jts.geom.Coordinate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("path")
public class PathController {

    private UserRepository userRepository;
    private JourneyRepository journeyRepository;

    @Autowired
    public PathController(UserRepository userRepository,
                          JourneyRepository journeyRepository) {
        this.userRepository = userRepository;
        this.journeyRepository = journeyRepository;
    }

    @PostMapping("getPathScore")
    public Double getPathScore(@RequestParam("gardaStations") Integer gardaStations,
                               @RequestParam("lights") Long lights,
                               @RequestParam("distance") Double distance,
                               @RequestParam("gardaWeight") int gardaWeight,
                               @RequestParam("lightWeight") int lightWeight ) {
        double lightScore = ((lights * 15) / distance) * (lightWeight/100.0);
        double gardaScore = ((gardaStations * 1900) / distance) * (gardaWeight/100.0);
        double totalScore = ((lightScore + gardaScore) * 5);
        System.out.println("garda weight: " + gardaWeight + " light w " + lightWeight + " distance " + distance);
        System.out.println("No of lights: " + lights + " No of garda " + gardaStations);
        System.out.println("Score is: " + totalScore);
        return totalScore > 5 ? 5 : totalScore;
    }


    @PostMapping("saveCurrentLocation")
    public void saveCurrentLocation(@RequestParam("userID") long userID, @RequestParam("lat") double latitude,
                                    @RequestParam("long") double longitude) {
        User currentUser = userRepository.findById(userID);
        Journey journey = journeyRepository.findByUserAndActiveTrue(currentUser);
        if (journey != null) {
            //update
            journey.setLatitude(latitude);
            journey.setLongitude(longitude);
            journeyRepository.save(journey);
        }

    }

    @PostMapping("getUserPath")
    public List<List<Double>> getUserPath(@RequestParam("userID") long userID) {
        User user = userRepository.findById(userID);
        Journey journey = journeyRepository.findByUserAndActiveTrue(user);
        List<Coordinate> coordinates = Arrays.asList(journey.getRoute().getCoordinates());
        List<List<Double>> result = new ArrayList<>();
        for (Coordinate c : coordinates) {
            result.add(Arrays.asList(c.x, c.y));
        }

        return result;

    }

    @PostMapping("getUserLocation")
    public List<Double> getUserLocation(@RequestParam("userID") long userID) {
        User user = userRepository.findById(userID);
        Journey journey = journeyRepository.findByUserAndActiveTrue(user);
        if (journey != null) {
            List<Double> location = new ArrayList<>();
            location.add(journey.getLatitude());
            location.add(journey.getLongitude());
            return location;
        }
        return new ArrayList<>();
    }

    @PostMapping("stopJourney")
    public void stopJourney(@RequestParam("userID") long userID) {
        User user = userRepository.findById(userID);
        Journey journey = journeyRepository.findByUserAndActiveTrue(user);
        journey.setActive(false);
        journeyRepository.save(journey);
    }

    @GetMapping("stopJourney")
    public void stopJourneyGet(@RequestParam("userID") long userID) {
        stopJourney(userID);
    }
}
