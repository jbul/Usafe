package com.usafe.repository;

import com.usafe.entity.Journey;
import com.usafe.entity.User;
import org.springframework.data.repository.CrudRepository;

public interface JourneyRepository extends CrudRepository<Journey, Long> {
    Journey getByUser(User u);

    Journey findByUserAndActiveTrue(User u);

}
