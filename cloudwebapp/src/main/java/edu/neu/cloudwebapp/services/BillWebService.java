package edu.neu.cloudwebapp.services;

import edu.neu.cloudwebapp.model.BillDetails;
import edu.neu.cloudwebapp.model.UserRegistration;
import edu.neu.cloudwebapp.repository.BillDetailsRepository;
import edu.neu.cloudwebapp.repository.UserRegistrationRepository;
import edu.neu.cloudwebapp.utility.UtilityClass;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class BillWebService {

    @Autowired
    private UserRegistrationRepository userRegistrationRepository;

    @Autowired
    private BillDetailsRepository billDetailsRepository;

    @Autowired
    private UtilityClass utilityClass;

    //Method to add Bills in the database
    public BillDetails addBill(String userEmail, BillDetails bill) {
        String message = utilityClass.validateBillRequest(bill);
        try {
            if (message.contains("Success")) {
                bill.setId(java.util.UUID.randomUUID().toString());
                UserRegistration user = userRegistrationRepository.findUserRegistrationByEmail(userEmail);
                bill.setOwner_id(user.getId());
                bill.setCreated_ts(new Date());
                bill.setUpdated_ts(new Date());
                bill.setVendor(bill.getVendor());
                bill.setBill_date(bill.getBill_date());
                bill.setDue_date(bill.getDue_date());
                bill.setAmount_due(bill.getAmount_due());
                bill.setCategories(bill.getCategories());
                bill.setPaymentStatus(bill.getPaymentStatus());
                billDetailsRepository.save(bill);
                return billDetailsRepository.findBillDetailsById(bill.getId());
            } else {
                throw new Exception();
            }
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }


    public BillDetails getBillDetailsByUserId(String billId, String email) {
        BillDetails billDetails = billDetailsRepository.findBillDetailsById(billId);
        UserRegistration userRegistration = userRegistrationRepository.findUserRegistrationByEmail(email);
        if (billDetails != null) {
            if (billDetails.getOwner_id().equalsIgnoreCase(userRegistration.getId())) {
                return billDetails;
            } else {
                throw new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Not authorized to access the bill details");
            }
        } else {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "No Bill for this id");
        }
    }

    public List<JSONObject> getUserBillDetails(String email) throws JSONException {
        UserRegistration user = userRegistrationRepository.findUserRegistrationByEmail(email);
        Iterable<BillDetails> bd = billDetailsRepository.findAll();
        List<JSONObject> listEntity = new ArrayList<>();
        for (BillDetails b : bd) {
            if(b.getOwner_id().equalsIgnoreCase(user.getId())){
                listEntity.add(utilityClass.getBillDetailJSON(b));
            }
        }
        return listEntity;
    }

    public boolean deleteBillDetailsByUserId(String billId, String email) {
        BillDetails billDetails = billDetailsRepository.findBillDetailsById(billId);
        UserRegistration userRegistration = userRegistrationRepository.findUserRegistrationByEmail(email);
        if (billDetails != null) {
            if (billDetails.getOwner_id().equalsIgnoreCase(userRegistration.getId())) {
                billDetailsRepository.deleteById(billId);
                return true;
            } else {
                throw new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Not authorized to delete the bill details");
            }
        } else {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "No Bill Details Found for bill id " + billId);
        }
    }

    public String updateBillDetailsByID(BillDetails billDetails, String email, String billID) {
        String message = utilityClass.validateBillRequest(billDetails);
        if (message.contains("Success")) {
            UserRegistration user = userRegistrationRepository.findUserRegistrationByEmail(email);
            BillDetails bill = billDetailsRepository.findBillDetailsById(billID);
            if (bill != null) {
                if (bill.getOwner_id().equalsIgnoreCase(user.getId())) {
                    bill.setVendor(billDetails.getVendor());
                    bill.setBill_date(billDetails.getBill_date());
                    bill.setDue_date(billDetails.getDue_date());
                    bill.setAmount_due(billDetails.getAmount_due());
                    bill.setCategories(billDetails.getCategories());
                    bill.setUpdated_ts(new Date());
                    bill.setPaymentStatus(billDetails.getPaymentStatus());
                    billDetailsRepository.save(bill);
                } else {
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authorized to update the bill details");
                }
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No Bill Details Found for bill id " + billID);
            }
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
        return "Success";
    }
}
