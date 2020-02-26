package edu.neu.cloudwebapp.utility;

import edu.neu.cloudwebapp.model.BillDetails;
import edu.neu.cloudwebapp.model.PaymentStatus;
import edu.neu.cloudwebapp.model.UserRegistration;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.regex.Pattern;

@Component
public class UtilityClass {

    @Value("${path.to.file}")
    private String UPLOADED_FOLDER;


    //Method to Decode the Base64 Token saved in Header Authorization
    public static String authEncode(String authorization) {
        if (authorization != null && !authorization.isEmpty()) {
            //assert authorization.substring(0, 6).equals("Basic");
            String basicAuthEncoded = authorization.substring(6);
            String basicAuthAsString = new String(Base64.getDecoder().decode(basicAuthEncoded.getBytes()));
            return basicAuthAsString;
        } else {
            throw new NullPointerException("Authorization value Null");
        }
    }

    //Method to check if paymentStatus exists in the Enum
    public static boolean contains(String paymentStatus) {
        for (PaymentStatus ps : PaymentStatus.values()) {
            if (ps.name().equalsIgnoreCase(paymentStatus)) {
                return true;
            }
        }
        return false;
    }

    //Validate the incoming Bill POST/PUT HttpRequests
    public static String validateBillRequest(BillDetails bill) {
        if (bill.getVendor() == null || bill.getVendor().isEmpty()) {
            return "Vendor details are required and can not be left empty";
        }
        if (bill.getAmount_due() == null || bill.getAmount_due() <= 0.0) {
            return "Amount details are required and can not be left empty";
        }
        if (bill.getCategories() == null || bill.getCategories().size() <= 0) {
            return "Categories details are required and can not be left empty";
        }
        if (bill.getPaymentStatus() == null) {
            return "Payment Status details are required and can not be left empty";
        }
        if (bill.getBill_date() == null) {
            return "Bill Date can not be left empty";
        }
        if (bill.getDue_date() == null) {
            return "Due Date can not be left empty";
        }
        if (bill.getId() != null || bill.getCreated_ts() != null || bill.getUpdated_ts() != null || bill.getOwner_id() != null) {
            return "INVALID REQUEST : READ ONLY FIELDS NOT ALLOWED IN THE REQUEST";
        }
        return "Success";
    }

    public String validateUserRequest(UserRegistration userRegistration) {
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
        if (userRegistration.getId() != null || userRegistration.getAccount_created() != null || userRegistration.getAccount_updated() != null) {
            return "Invalid HttpRequest readOnly fields not allowed";
        }
        return "Success";
    }

    //Method to check if the entered password adheres to the NIST standard
    private boolean isPasswordValid(String pwd) {
        String pattern = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,}$";
        if (Pattern.matches(pattern, pwd)) {
            return true;
        }
        return false;
    }

    //Method to return the JSON Object that contains all the bill details
    public JSONObject getBillDetailJSON(BillDetails bill) throws JSONException {
        JSONObject entity = new JSONObject();
        entity.put("id", bill.getId());
        entity.put("created_ts", bill.getCreated_ts());
        entity.put("updated_ts", bill.getUpdated_ts());
        entity.put("owner_id", bill.getOwner_id());
        entity.put("vendor", bill.getVendor());
        entity.put("bill_date", bill.getBill_date());
        entity.put("due_date", bill.getDue_date());
        entity.put("amount_due", bill.getAmount_due());
        entity.put("categories", bill.getCategories());
        entity.put("paymentStatus", bill.getPaymentStatus());
        entity.put("attachment", bill.getAttachment());
        return entity;
    }

    //Method to return the JSON Object that contains all the user details except the password
    public JSONObject getUserRegistrationJSON(UserRegistration usr) throws JSONException {
        JSONObject entity = new JSONObject();
        entity.put("id", usr.getId());
        entity.put("first_name", usr.getFirstName());
        entity.put("last_name", usr.getLastName());
        entity.put("email_address", usr.getEmail());
        entity.put("account_created", usr.getAccount_created());
        entity.put("account_updated", usr.getAccount_updated());
        return entity;
    }

    public String computeMD5Hash(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");

        byte[] digest = messageDigest.digest(data);

        StringBuffer sb = new StringBuffer();
        for (byte b : digest) {
            sb.append(Integer.toHexString((int) (b & 0xff)));
        }
        return sb.toString();
    }
}
