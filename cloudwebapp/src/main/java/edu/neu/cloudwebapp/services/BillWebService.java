package edu.neu.cloudwebapp.services;

import edu.neu.cloudwebapp.model.BillDetails;
import edu.neu.cloudwebapp.model.UserRegistration;
import edu.neu.cloudwebapp.repository.BillDetailsRepository;
import edu.neu.cloudwebapp.repository.UserRegistrationRepository;
import edu.neu.cloudwebapp.utility.UtilityClass;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    @Autowired
    private FileHandlerService fileHandlerService;

    private final static Logger logger = LoggerFactory.getLogger(BillWebService.class);

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
                bill.setAttachment(null);
                billDetailsRepository.save(bill);
                return billDetailsRepository.findBillDetailsById(bill.getId());
            } else {
                throw new Exception();
            }
        } catch (Exception ex) {
            logger.error("Invalid Post Bill Request");
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
                logger.error("Not authorized to access the bill details");
                throw new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Not authorized to access the bill details");
            }
        } else {
            logger.error("No Bills Found for this id");
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "No Bill for this id");
        }
    }

    public List<BillDetails> getUserBillDetails(String email) throws JSONException {
        UserRegistration user = userRegistrationRepository.findUserRegistrationByEmail(email);
        Iterable<BillDetails> bd = billDetailsRepository.findAll();
        List<BillDetails> listEntity = new ArrayList<>();
        for (BillDetails b : bd) {
            if (b.getOwner_id().equalsIgnoreCase(user.getId())) {
                listEntity.add(b);
            }
        }
        return listEntity;
    }

    public boolean deleteBillDetailsByUserId(String billId, String email) throws Exception {
        BillDetails billDetails = billDetailsRepository.findBillDetailsById(billId);
        UserRegistration userRegistration = userRegistrationRepository.findUserRegistrationByEmail(email);
        if (billDetails != null) {
            if (billDetails.getOwner_id().equalsIgnoreCase(userRegistration.getId())) {
                if (billDetails.getAttachment() != null) {
                    fileHandlerService.deleteFile(billDetails);
                }
                billDetailsRepository.deleteById(billId);
                return true;
            } else {
                logger.error("Not authorized to access the bill details");
                throw new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Not authorized to delete the bill details");
            }
        } else {
            logger.error("No Bill Details Found for bill id");
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "No Bill Details Found for bill id " + billId);
        }
    }

    public BillDetails updateBillDetailsByID(BillDetails billDetails, String email, String billID) {
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
                    bill.setAttachment(bill.getAttachment());
                    billDetailsRepository.save(bill);
                    return billDetailsRepository.findBillDetailsById(bill.getId());
                } else {
                    logger.error("User not authorized to update the bill details");
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authorized to update the bill details");
                }
            } else {
                logger.error("No Bill Details Found for bill id");
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No Bill Details Found for bill id " + billID);
            }
        } else {
            logger.error("Bad Put Bill Request: " + message);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }
}
