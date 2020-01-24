package edu.neu.cloudwebapp.controllers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import edu.neu.cloudwebapp.model.UserRegistration;
import edu.neu.cloudwebapp.repository.UserRegistrationRepository;
import edu.neu.cloudwebapp.services.UserWebService;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserControllerTest {

    @InjectMocks
    UserController userController;

    @Mock
    UserWebService userWebService;

    @Mock
    UserRegistrationRepository userRegistrationRepository;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        UserRegistration user = new UserRegistration();
        user.setEmail("goelsarthak100@yahoo.com");
        user.setPassword("Sarthak@89");
        when(userRegistrationRepository.findUserRegistrationByEmail(user.getEmail())).thenReturn(user);
        when(userRegistrationRepository.findUserRegistrationByEmailAndPassword(user.getEmail(),user.getPassword())).thenReturn(user);
    }

    @Test
    public void BlankPostRequest() throws JSONException {
        UserRegistration user = new UserRegistration();
        ResponseEntity<String> responseEntity = userController.registerUserDetails(user);
        assertEquals(responseEntity.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void BlankGetRequest() throws JSONException {
        UserRegistration user = new UserRegistration();
        user.setEmail("goelsarthak100@yahoo.com");
        user.setPassword("Sarthak@89");
        userController.getUserDetails("");
    }

    @Test
    public void findUserByEmail() throws Exception {
        UserRegistration retrievedUser = userRegistrationRepository.findUserRegistrationByEmail("goelsarthak100@yahoo.com");
        assertEquals(retrievedUser.getEmail(),"goelsarthak100@yahoo.com");
    }

    @Test
    public void findUser() throws Exception {
        UserRegistration retrievedUser = userRegistrationRepository.findUserRegistrationByEmailAndPassword("goelsarthak100@yahoo.com","Sarthak@89");
        assertEquals(retrievedUser.getPassword(),"Sarthak@89");
    }

}
