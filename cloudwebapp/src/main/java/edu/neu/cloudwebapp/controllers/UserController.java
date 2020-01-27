package edu.neu.cloudwebapp.controllers;

import edu.neu.cloudwebapp.model.UserRegistration;
import edu.neu.cloudwebapp.services.UserWebService;
import edu.neu.cloudwebapp.utility.UtilityClass;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
public class UserController {

    @Autowired
    private UserWebService userWebService;

    @Autowired
    private UtilityClass utilityClass;

    @RequestMapping(value = "/v1/user", method = RequestMethod.POST, produces = "application/json",
            consumes = "application/json")
    public ResponseEntity<String> registerUserDetails(@RequestBody UserRegistration userRegistration) throws JSONException {
        JSONObject entity = new JSONObject();
        String result = "";
        try {
            if (userRegistration != null) {
                String email = userRegistration.getEmail();
                String password = userRegistration.getPassword();
                result = userWebService.registerUser(userRegistration);
                if (result.contains("Success")) {
                    UserRegistration usr = userWebService.getUser(email, password);
                    entity = utilityClass.getUserRegistrationJSON(usr);
                    return new ResponseEntity<String>(entity.toString(), HttpStatus.CREATED);
                } else
                    entity.put("error", result);
                entity.put("status_code", HttpStatus.BAD_REQUEST);
                return new ResponseEntity<String>(entity.toString(), HttpStatus.BAD_REQUEST);
            } else {
                entity.put("error", "Post Request Can't Be Empty");
                entity.put("status_code", HttpStatus.BAD_REQUEST);
                return new ResponseEntity<String>(entity.toString(), HttpStatus.BAD_REQUEST);
            }
        } catch (Exception ex) {
            entity.put("error", "Post Request is invalid");
            entity.put("status_code", HttpStatus.BAD_REQUEST);
            return new ResponseEntity<String>(entity.toString(), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/v1/user/self", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<String> getUserDetails(@RequestHeader(value = "Authorization") String auth) throws JSONException {
        String authorization = utilityClass.authEncode(auth);
        String[] headerAuth = authorization.split(":");
        String email = headerAuth[0];
        String password = headerAuth[1];
        UserRegistration user = userWebService.getUser(email, password);
        JSONObject entity = utilityClass.getUserRegistrationJSON(user);
        return new ResponseEntity<String>(entity.toString(), HttpStatus.OK);
    }

    @RequestMapping(value = "/v1/user/self", method = RequestMethod.PUT)
    public ResponseEntity<String> updateUserDetails(@RequestHeader(value = "Authorization") String auth, @RequestBody UserRegistration userRegistration) throws Exception {
        String authorization = utilityClass.authEncode(auth);
        String[] headerAuth = authorization.split(":");
        String email = headerAuth[0];
        String password = headerAuth[1];
        String result = userWebService.updateUser(email, password, userRegistration);
        if (result.contains("Success"))
            return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
        else
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }
}