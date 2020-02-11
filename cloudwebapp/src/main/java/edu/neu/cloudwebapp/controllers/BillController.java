package edu.neu.cloudwebapp.controllers;

import edu.neu.cloudwebapp.model.BillDetails;
import edu.neu.cloudwebapp.repository.BillDetailsRepository;
import edu.neu.cloudwebapp.services.BillWebService;
import edu.neu.cloudwebapp.utility.UtilityClass;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@CrossOrigin
public class BillController {

    @Autowired
    private BillWebService billWebService;
    @Autowired
    private UtilityClass utilityClass;
    @Autowired
    private BillDetailsRepository billDetailsRepository;

    @RequestMapping(value = "/v1/bill/", method = RequestMethod.POST, produces = "application/json",
            consumes = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public BillDetails addBillDetails(@RequestHeader(value = "Authorization") String auth, @RequestBody BillDetails bill) throws JSONException {
        JSONObject entity = new JSONObject();
        String result = "";
        if (bill != null) {
            String authorization = utilityClass.authEncode(auth);
            String[] headerAuth = authorization.split(":");
            String email = headerAuth[0];
            String password = headerAuth[1];
            BillDetails bd = billWebService.addBill(email, bill);
            if (bd != null) {
                entity = utilityClass.getBillDetailJSON(bd);
                return bd;
            }
        }
        String message = "Invalid POST HTTP Request";
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }


    @RequestMapping(value = "/v1/bills", method = RequestMethod.GET, produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public List<BillDetails> getBillDetails(@RequestHeader(value = "Authorization") String auth) throws JSONException {
        JSONObject entity = new JSONObject();
        String message = "";
        try {
            String authorization = utilityClass.authEncode(auth);
            String[] headerAuth = authorization.split(":");
            String email = headerAuth[0];
            String password = headerAuth[1];
            List<BillDetails> listEntity = billWebService.getUserBillDetails(email);
            if (listEntity.size() > 0) {
                return listEntity;
            }
            message = "No Bills Found for this user";
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
        } catch (Exception ex) {
            if (message.contains("No Bills"))
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
            ;
            message = "Invalid GET HttpRequest";
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }

    @RequestMapping(value = "/v1/bill/{id}", method = RequestMethod.GET, produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public BillDetails getBillById(@PathVariable(value = "id") String billId, @RequestHeader(value = "Authorization") String auth) throws JSONException {
        JSONObject entity = new JSONObject();
        String authorization = utilityClass.authEncode(auth);
        String[] headerAuth = authorization.split(":");
        String email = headerAuth[0];
        String password = headerAuth[1];
        BillDetails billDetails = billWebService.getBillDetailsByUserId(billId, email);
        if (billDetails != null) {
            return billDetails;
        } else {
            String message = "No Bill Found for this Id";
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
        }
    }

    @RequestMapping(value = "/v1/bill/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<String> deleteBillByID(@PathVariable(value = "id") String billId, @RequestHeader(value = "Authorization") String auth) throws JSONException {
        String authorization = utilityClass.authEncode(auth);
        String[] headerAuth = authorization.split(":");
        String email = headerAuth[0];
        String password = headerAuth[1];
        if (billWebService.deleteBillDetailsByUserId(billId, email))
            return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);

        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value = "/v1/bill/{id}", method = RequestMethod.PUT, consumes = "application/json")
    public ResponseEntity<String> updateBillByID(@RequestBody BillDetails billDetails, @PathVariable(value = "id") String billId, @RequestHeader(value = "Authorization") String auth) throws JSONException {
        String authorization = utilityClass.authEncode(auth);
        String[] headerAuth = authorization.split(":");
        String email = headerAuth[0];
        String password = headerAuth[1];
        if (billDetails != null) {
            String result = billWebService.updateBillDetailsByID(billDetails, email, billId);
            if (result.contains("Success")) {
                return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
            }
        }
        return new ResponseEntity<>("Invalid PUT Request", HttpStatus.BAD_REQUEST);
    }
}