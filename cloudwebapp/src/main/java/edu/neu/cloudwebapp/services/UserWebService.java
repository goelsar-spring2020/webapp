package edu.neu.cloudwebapp.services;

import edu.neu.cloudwebapp.model.UserRegistration;
import edu.neu.cloudwebapp.repository.UserRegistrationRepository;
import edu.neu.cloudwebapp.utility.UtilityClass;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class UserWebService {

    @Autowired
    private UserRegistrationRepository userRegistrationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UtilityClass utilityClass;

    private final static Logger logger = LoggerFactory.getLogger(UserWebService.class);

    //Method user by GET Request mapping RestController to get the user details
    public UserRegistration getUser(String email, String password) throws JSONException {
        UserRegistration usr = null;
        boolean flag = false;
        try {
            if ((email != null || !email.isEmpty()) && (password != null && !password.isEmpty())) {
                usr = userRegistrationRepository.findUserRegistrationByEmail(email);
                if (usr != null && passwordEncoder.matches(password, usr.getPassword())) {
                    return usr;
                } else
                    flag = true;
            } else
                flag = true;
            if (flag = true) {
                throw new BadCredentialsException("External user can not be validated");
            }
        } catch (Exception e) {
            logger.error("External user can not be validated");
            throw new BadCredentialsException("External user can not be validated");
        }
        return usr;
    }

    //Method user by POST Request mapping RestController to register the new user
    public String registerUser(UserRegistration ur) {

        String message = utilityClass.validateUserRequest(ur);
        if (message.contains("Success")) {
            if (isEmailPresent(ur.getEmail())) {
                logger.error("Email already exists in records. Please login ");
                return "Email already exists in records. Please login ";
            } else {
                ur.setId(java.util.UUID.randomUUID().toString());
                ur.setAccount_created(new Date());
                ur.setAccount_updated(new Date());
                ur.setPassword(passwordEncoder.encode(ur.getPassword()));
                userRegistrationRepository.save(ur);
            }
        }
        return message;
    }

    //Method user by PUT Request mapping RestController to update the user details
    public String updateUser(String email, String password, UserRegistration userRegistration) {
        String message = utilityClass.validateUserRequest(userRegistration);
        if (message.contains("Success")) {
            if (!userRegistration.getEmail().equalsIgnoreCase(email)) {
                logger.error("Token Credentials & user email do not match");
                return "Token Credentials & user email do not match";
            }
            UserRegistration usr = userRegistrationRepository.findUserRegistrationByEmail(userRegistration.getEmail());
            if (passwordEncoder.matches(password, usr.getPassword())) {
                usr.setPassword(passwordEncoder.encode(userRegistration.getPassword()));
                usr.setFirstName(userRegistration.getFirstName());
                usr.setLastName(userRegistration.getLastName());
                usr.setAccount_updated(new Date());
                userRegistrationRepository.save(usr);
            } else
                logger.error("Bad Request");
                return "Bad Request";
        }
        return message;
    }

    //Method to check if the entered email does not exist in the database
    private boolean isEmailPresent(String email) {
        if (userRegistrationRepository.findUserRegistrationByEmail(email) != null) {
            return true;
        }
        return false;
    }
}
