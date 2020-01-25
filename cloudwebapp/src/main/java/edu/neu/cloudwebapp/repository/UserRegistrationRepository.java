package edu.neu.cloudwebapp.repository;

import edu.neu.cloudwebapp.model.UserRegistration;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface UserRegistrationRepository extends CrudRepository<UserRegistration, Long> {

    @Query("Select user from UserRegistration user where user.email=?1 and user.password=?2")
    public UserRegistration findUserRegistrationByEmailAndPassword(String email, String pwd);

    @Query("Select user from UserRegistration user where user.email=?1")
    public UserRegistration findUserRegistrationByEmail(String email);

}
