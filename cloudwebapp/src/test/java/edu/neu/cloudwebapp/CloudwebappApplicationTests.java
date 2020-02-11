package edu.neu.cloudwebapp;

import edu.neu.cloudwebapp.controllers.BillController;
import edu.neu.cloudwebapp.controllers.UserController;
import edu.neu.cloudwebapp.model.BillDetails;
import edu.neu.cloudwebapp.model.UserRegistration;
import edu.neu.cloudwebapp.repository.BillDetailsRepository;
import edu.neu.cloudwebapp.repository.UserRegistrationRepository;
import edu.neu.cloudwebapp.services.BillWebService;
import edu.neu.cloudwebapp.services.UserWebService;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
class CloudwebappApplicationTests {

    @InjectMocks
    UserController userController;

    @InjectMocks
    BillController billController;

    @Mock
    UserWebService userWebService;

    @Mock
    BillWebService billWebService;

    @Mock
    UserRegistrationRepository userRegistrationRepository;

    @Mock
    BillDetailsRepository billDetailsRepository;

    @Test
    void contextLoads() {
    }

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);

        UserRegistration user = new UserRegistration();
        user.setEmail("goelsarthak100@yahoo.com");
        user.setPassword("Sarthak@89");
        user.setId("bf726389-99e4-4583-81db-d02b7da8dcb5");
        when(userRegistrationRepository.findUserRegistrationByEmail(user.getEmail())).thenReturn(user);
        when(userRegistrationRepository.findUserRegistrationByEmailAndPassword(user.getEmail(), user.getPassword())).thenReturn(user);

        BillDetails bd = new BillDetails();
        bd.setId("bf726389-99e4-4583-81db-d02b7da8dcb5");
        when(billDetailsRepository.findBillDetailsById(bd.getId())).thenReturn(bd);
    }

    @Test
    public void BlankPostRequest() throws JSONException {
        UserRegistration user = new UserRegistration();
        ResponseEntity<String> responseEntity = userController.registerUserDetails(user);
        assertEquals(responseEntity.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void InvalidGetBillRequest() throws JSONException {
        String auth = "Basic Z29lbHNhcnRoYWs5M0BnbWFpbC5jb206U2FydGhha0A4OQ==";
        String billID = "s123";
        //ResponseEntity<String> responseEntity = billController.getBillById(billID, auth);
        //assertEquals(responseEntity.getStatusCode(), HttpStatus.NOT_FOUND);
    }

    @Test
    public void BlankTokenRequest() throws JSONException {
        Throwable thrown = assertThrows(NullPointerException.class, () -> userController.getUserDetails(""));
        UserRegistration user = new UserRegistration();
        user.setEmail("goelsarthak100@yahoo.com");
        user.setPassword("Sarthak@89");
    }

    @Test
    public void findUserByEmail() throws Exception {
        UserRegistration retrievedUser = userRegistrationRepository.findUserRegistrationByEmail("goelsarthak100@yahoo.com");
        assertEquals(retrievedUser.getEmail(), "goelsarthak100@yahoo.com");
    }

    @Test
    public void findUser() throws Exception {
        UserRegistration retrievedUser = userRegistrationRepository.findUserRegistrationByEmailAndPassword("goelsarthak100@yahoo.com", "Sarthak@89");
        assertEquals(retrievedUser.getPassword(), "Sarthak@89");
    }

    @Test
    public void findBillDetailsByBillID() {
        BillDetails bill = billDetailsRepository.findBillDetailsById("bf726389-99e4-4583-81db-d02b7da8dcb5");
        assertEquals(bill.getId(), "bf726389-99e4-4583-81db-d02b7da8dcb5");
    }

    @Test
    public void registerBill() throws JSONException {
        String auth = "Basic Z29lbHNhcnRoYWs5M0BnbWFpbC5jb206U2FydGhha0A4OQ==";
        BillDetails bd = new BillDetails();
        //ResponseEntity<String> responseEntity = billController.addBillDetails(auth, bd);
        //assertEquals(responseEntity.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void updateBill() throws JSONException {
        BillDetails bd = new BillDetails();
        String auth = "Basic Z29lbHNhcnRoYWs5M0BnbWFpbC5jb206U2FydGhha0A4OQ==";
        String billID = java.util.UUID.randomUUID().toString();
        Throwable thrown = assertThrows(NullPointerException.class, () -> billController.updateBillByID(bd, billID, auth));
    }
}
