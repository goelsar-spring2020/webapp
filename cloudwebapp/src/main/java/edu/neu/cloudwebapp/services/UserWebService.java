package edu.neu.cloudwebapp.services;

import edu.neu.cloudwebapp.repository.UserRegistrationRepository;
import edu.neu.cloudwebapp.model.UserRegistration;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Date;
import java.util.regex.Pattern;

@Service
public class UserWebService {

    @Autowired
    private UserRegistrationRepository userRegistrationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
            if (flag = true)
                throw new BadCredentialsException("External user can not be validated");
        } catch (Exception e) {
            throw new BadCredentialsException("External user can not be validated");
        }
        return usr;
    }

    //Method user by POST Request mapping RestController to register the new user
    public String registerUser(UserRegistration ur) {

        String message = validateRequest(ur);
        if (message.contains("Success")) {
            if (isEmailPresent(ur.getEmail())) {
                return "Email already exists in records. Please login ";
            } else {
                ur.setUserID(java.util.UUID.randomUUID().toString());
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
        String message = validateRequest(userRegistration);
        if (message.contains("Success")) {
            if (!userRegistration.getEmail().equalsIgnoreCase(email)) {
                return "Token Credentials & user email do not match";
            }
            UserRegistration usr = userRegistrationRepository.findUserRegistrationByEmail(userRegistration.getEmail());
            if (passwordEncoder.matches(userRegistration.getPassword(), usr.getPassword())) {
                usr.setPassword(passwordEncoder.encode(userRegistration.getPassword()));
                usr.setFirstName(userRegistration.getFirstName());
                usr.setLastName(userRegistration.getLastName());
                usr.setAccount_updated(new Date());
                userRegistrationRepository.save(usr);
            } else
                return "Bad Request";
        }
        return message;
    }

    //Method to return the JSON Object that contains all the user details except the password
    public JSONObject getJSON(UserRegistration usr) throws JSONException {
        JSONObject entity = new JSONObject();
        entity.put("id", usr.getUserID());
        entity.put("first_name", usr.getFirstName());
        entity.put("last_name", usr.getLastName());
        entity.put("email_address", usr.getEmail());
        entity.put("account_created", usr.getAccount_created());
        entity.put("account_updated", usr.getAccount_updated());
        return entity;
    }

    //Method to check if the entered password adheres to the NIST standard
    private boolean isPasswordValid(String pwd) {
        String pattern = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,}$";
        if (Pattern.matches(pattern, pwd)) {
            return true;
        }
        return false;
    }

    //Method to check if the entered email does not exist in the database
    private boolean isEmailPresent(String email) {
        if (userRegistrationRepository.findUserRegistrationByEmail(email) != null) {
            return true;
        }
        return false;
    }

    private String validateRequest(UserRegistration userRegistration) {
        if (userRegistration.getEmail() == null || userRegistration.getEmail().isEmpty()) {
            return "Email Can Not Be Empty";
        }
        if (userRegistration.getPassword() == null || userRegistration.getPassword().isEmpty()) {
            return "Password Can Not Be Empty";
        }
        if (userRegistration.getFirstName() == null || userRegistration.getFirstName().isEmpty()) {
            return "First Name Can Not Be Empty";
        }
        if (userRegistration.getLastName() == null || userRegistration.getLastName().isEmpty()) {
            return "Last Name Can Not Be Empty";
        }
        if (!isPasswordValid(userRegistration.getPassword())) {
            return "Password doesn't adhere to NIST standards";
        }
        return "Success";
    }
}
