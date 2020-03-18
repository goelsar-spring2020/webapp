package edu.neu.cloudwebapp.controllers;

import com.timgroup.statsd.StatsDClient;
import edu.neu.cloudwebapp.model.UserRegistration;
import edu.neu.cloudwebapp.services.UserWebService;
import edu.neu.cloudwebapp.utility.UtilityClass;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@CrossOrigin
public class UserController {

    private final static Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserWebService userWebService;

    @Autowired
    private UtilityClass utilityClass;

    @Autowired
    private StatsDClient statsDClient;

    @RequestMapping(value = "/v1/user", method = RequestMethod.POST, produces = "application/json",
            consumes = "application/json")
    public ResponseEntity<String> registerUserDetails(@RequestBody UserRegistration userRegistration) throws JSONException {
        statsDClient.incrementCounter("endpoint.v1.user.api.post");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        logger.info("POST request : \"/v1/user\"");
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
                    logger.debug("HTTP : 201 created");
                    stopWatch.stop();
                    statsDClient.recordExecutionTime("timer.v1.user.api.post",stopWatch.getLastTaskTimeMillis());
                    return new ResponseEntity<String>(entity.toString(), HttpStatus.CREATED);
                } else
                    entity.put("error", result);
                entity.put("status_code", HttpStatus.BAD_REQUEST);
                logger.error("Post Request failed Weak password/ Email already exist : /v1/user");
                stopWatch.stop();
                statsDClient.recordExecutionTime("timer.v1.user.api.post",stopWatch.getLastTaskTimeMillis());
                return new ResponseEntity<String>(entity.toString(), HttpStatus.BAD_REQUEST);
            } else {
                entity.put("error", "Post Request Can't Be Empty");
                entity.put("status_code", HttpStatus.BAD_REQUEST);
                logger.error("Empty Post Request for create user : /v1/user");
                stopWatch.stop();
                statsDClient.recordExecutionTime("timer.v1.user.api.post",stopWatch.getLastTaskTimeMillis());
                return new ResponseEntity<String>(entity.toString(), HttpStatus.BAD_REQUEST);
            }
        } catch (Exception ex) {
            entity.put("error", "Post Request is invalid");
            entity.put("status_code", HttpStatus.BAD_REQUEST);
            logger.error("Invalid Post Request for create user : /v1/user");
            stopWatch.stop();
            statsDClient.recordExecutionTime("timer.v1.user.api.post",stopWatch.getLastTaskTimeMillis());
            return new ResponseEntity<String>(entity.toString(), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/v1/user/self", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<String> getUserDetails(@RequestHeader(value = "Authorization") String auth) throws JSONException {
        statsDClient.incrementCounter("endpoint.v1.user.self.api.get");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        logger.info("Get User request : \"/v1/user/self\"");
        String authorization = utilityClass.authEncode(auth);
        String[] headerAuth = authorization.split(":");
        String email = headerAuth[0];
        String password = headerAuth[1];
        UserRegistration user = userWebService.getUser(email, password);
        JSONObject entity = utilityClass.getUserRegistrationJSON(user);
        logger.debug("HTTP : 200 OK");
        stopWatch.stop();
        statsDClient.recordExecutionTime("timer.v1.user.self.api.get",stopWatch.getLastTaskTimeMillis());
        return new ResponseEntity<String>(entity.toString(), HttpStatus.OK);
    }

    @RequestMapping(value = "/v1/user/self", method = RequestMethod.PUT)
    public ResponseEntity<String> updateUserDetails(@RequestHeader(value = "Authorization") String auth, @RequestBody UserRegistration userRegistration) throws Exception {
        statsDClient.incrementCounter("endpoint.v1.user.self.api.put");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        logger.info("Put User request : \"/v1/user/self\"");
        String authorization = utilityClass.authEncode(auth);
        String[] headerAuth = authorization.split(":");
        String email = headerAuth[0];
        String password = headerAuth[1];
        String result = userWebService.updateUser(email, password, userRegistration);
        if (result.contains("Success")) {
            logger.debug("HTTP : 204 No_Content");
            stopWatch.stop();
            statsDClient.recordExecutionTime("timer.v1.user.self.api.put",stopWatch.getLastTaskTimeMillis());
            return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
        } else {
            logger.error("Invalid Put Request for create user : /v1/user/self");
            stopWatch.stop();
            statsDClient.recordExecutionTime("timer.v1.user.self.api.put",stopWatch.getLastTaskTimeMillis());
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }
}