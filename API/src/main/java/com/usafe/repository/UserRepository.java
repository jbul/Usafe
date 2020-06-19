package com.usafe.repository;

import com.usafe.entity.User;
import org.springframework.data.repository.CrudRepository;


public interface UserRepository extends CrudRepository<User, Long> {

    User findByEmail(String email);

    User findByPhoneNo(String phoneNo);

    User findById(long userId);


}
