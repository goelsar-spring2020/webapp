package edu.neu.cloudwebapp.controllers;

import com.timgroup.statsd.StatsDClient;
import edu.neu.cloudwebapp.model.BillDetails;
import edu.neu.cloudwebapp.services.BillWebService;
import edu.neu.cloudwebapp.utility.AWSSQS;
import edu.neu.cloudwebapp.utility.UtilityClass;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@CrossOrigin
public class BillController {

    private final static Logger logger = LoggerFactory.getLogger(BillController.class);

    @Autowired
    private BillWebService billWebService;

    @Autowired
    private UtilityClass utilityClass;

    @Autowired
    private StatsDClient statsDClient;

    @Value("${spring.profiles.active}")
    private String profile;

    @Autowired(required = false)
    private AWSSQS awssqs;

    @RequestMapping(value = "/v1/bill/", method = RequestMethod.POST, produces = "application/json",
            consumes = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public BillDetails addBillDetails(@RequestHeader(value = "Authorization") String auth, @RequestBody BillDetails bill) throws JSONException {
        statsDClient.incrementCounter("endpoint.v1.bill.api.post");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        logger.info("Post Bill request : \"/v1/bill/\"");
        if (bill != null) {
            String authorization = utilityClass.authEncode(auth);
            String[] headerAuth = authorization.split(":");
            String email = headerAuth[0];
            String password = headerAuth[1];
            BillDetails bd = billWebService.addBill(email, bill);
            if (bd != null) {
                logger.debug("HTTP : 201 Created");
                stopWatch.stop();
                statsDClient.recordExecutionTime("timer.v1.bill.api.post", stopWatch.getLastTaskTimeMillis());
                return bd;
            }
        }
        String message = "Invalid POST HTTP Request";
        logger.error("Post BILL Request - Invalid POST HTTP Request : /v1/bill/");
        stopWatch.stop();
        statsDClient.recordExecutionTime("timer.v1.bill.api.post", stopWatch.getLastTaskTimeMillis());
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }


    @RequestMapping(value = "/v1/bills", method = RequestMethod.GET, produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public List<BillDetails> getBillDetails(@RequestHeader(value = "Authorization") String auth) throws JSONException {
        statsDClient.incrementCounter("endpoint.v1.bills.api.get");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        String message = "";
        logger.info("Get All Bill request : \"/v1/bills\"");
        try {
            String authorization = utilityClass.authEncode(auth);
            String[] headerAuth = authorization.split(":");
            String email = headerAuth[0];
            String password = headerAuth[1];
            List<BillDetails> listEntity = billWebService.getUserBillDetails(email);
            if (listEntity.size() > 0) {
                logger.debug("HTTP : 200 OK");
                stopWatch.stop();
                statsDClient.recordExecutionTime("timer.v1.bills.api.get", stopWatch.getLastTaskTimeMillis());
                return listEntity;
            }
            message = "No Bills Found for this user";
            logger.error("Get BILL Request - No Bills Found for this user : /v1/bills");
            stopWatch.stop();
            statsDClient.recordExecutionTime("timer.v1.bills.api.get", stopWatch.getLastTaskTimeMillis());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
        } catch (Exception ex) {
            if (message.contains("No Bills")) {
                logger.error("Get BILL Request - No Bills : /v1/bills");
                stopWatch.stop();
                statsDClient.recordExecutionTime("timer.v1.bills.api.get", stopWatch.getLastTaskTimeMillis());
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
            }

            message = "Invalid GET HttpRequest";
            logger.error("Get BILL Request - Invalid GET HttpRequest : /v1/bills");
            stopWatch.stop();
            statsDClient.recordExecutionTime("timer.v1.bills.api.get", stopWatch.getLastTaskTimeMillis());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }

    @RequestMapping(value = "/v1/bill/{id}", method = RequestMethod.GET, produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public BillDetails getBillById(@PathVariable(value = "id") String billId, @RequestHeader(value = "Authorization") String auth) throws JSONException {
        statsDClient.incrementCounter("endpoint.v1.bill.id.api.get");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        logger.info("Get Bill request : \"/v1/bill/{id}\"");
        String authorization = utilityClass.authEncode(auth);
        String[] headerAuth = authorization.split(":");
        String email = headerAuth[0];
        String password = headerAuth[1];
        BillDetails billDetails = billWebService.getBillDetailsByUserId(billId, email);
        if (billDetails != null) {
            logger.debug("HTTP : 200 OK");
            stopWatch.stop();
            statsDClient.recordExecutionTime("timer.v1.bill.id.api.get", stopWatch.getLastTaskTimeMillis());
            return billDetails;
        } else {
            String message = "No Bill Found for this Id";
            logger.error("Get BILL Request - No Bill Found for this Id : /v1/bill/{id}");
            stopWatch.stop();
            statsDClient.recordExecutionTime("timer.v1.bill.id.api.get", stopWatch.getLastTaskTimeMillis());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
        }
    }

    @RequestMapping(value = "/v1/bill/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<String> deleteBillByID(@PathVariable(value = "id") String billId, @RequestHeader(value = "Authorization") String auth) throws Exception {
        statsDClient.incrementCounter("endpoint.v1.bill.id.api.delete");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        logger.info("Delete Bill request : \"/v1/bill/{id}\"");
        String authorization = utilityClass.authEncode(auth);
        String[] headerAuth = authorization.split(":");
        String email = headerAuth[0];
        String password = headerAuth[1];
        if (billWebService.deleteBillDetailsByUserId(billId, email)) {
            logger.debug("HTTP : 204 No_Content");
            stopWatch.stop();
            statsDClient.recordExecutionTime("timer.v1.bill.id.api.delete", stopWatch.getLastTaskTimeMillis());
            return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
        }
        logger.error("Delete BILL Request - Invalid bill request : /v1/bill/{id}");
        stopWatch.stop();
        statsDClient.recordExecutionTime("timer.v1.bill.id.api.delete", stopWatch.getLastTaskTimeMillis());
        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value = "/v1/bill/{id}", method = RequestMethod.PUT, consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public BillDetails updateBillByID(@RequestBody BillDetails billDetails, @PathVariable(value = "id") String billId, @RequestHeader(value = "Authorization") String auth) throws JSONException {
        statsDClient.incrementCounter("endpoint.v1.bill.id.api.put");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        logger.info("Put Bill request : \"/v1/bill/{id}\"");
        String authorization = utilityClass.authEncode(auth);
        String[] headerAuth = authorization.split(":");
        String email = headerAuth[0];
        String password = headerAuth[1];
        if (billDetails != null) {
            BillDetails bill = billWebService.updateBillDetailsByID(billDetails, email, billId);
            if (bill != null) {
                logger.debug("HTTP : 200 OK");
                stopWatch.stop();
                statsDClient.recordExecutionTime("timer.v1.bill.id.api.put", stopWatch.getLastTaskTimeMillis());
                return bill;
            }
        }
        String message = "Invalid PUT Request";
        logger.error("Put BILL Request - Invalid bill request : /v1/bill/{id}");
        stopWatch.stop();
        statsDClient.recordExecutionTime("timer.v1.bill.id.api.put", stopWatch.getLastTaskTimeMillis());
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }


    @RequestMapping(value = "/v1/bills/due/{x}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<String> getDueDate(@PathVariable(value = "x") String noOfdays, @RequestHeader(value = "Authorization") String auth) throws JSONException {
        statsDClient.incrementCounter("endpoint.v1.bills.due.x.api.get");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        logger.info("Get Bill request : \"/v1/bills/due/{x}\"");
        String authorization = utilityClass.authEncode(auth);
        String[] headerAuth = authorization.split(":");
        String email = headerAuth[0];
        String password = headerAuth[1];
        List<BillDetails> listEntity = billWebService.getUserBillDetails(email);
        List<BillDetails> list = new ArrayList<>();
        if (listEntity.size() > 0) {
            logger.debug("HTTP : 200 OK");
            for (BillDetails bd : listEntity) {
                Date d1 = new Date();
                Date d2 = bd.getDue_date();
                long noOfDaysBetween = (d1.getTime() - d2.getTime()) / 86400000;
                if (Math.abs(noOfDaysBetween) <= Long.valueOf(noOfdays)) {
                    list.add(bd);
                }
            }
            if (list.size() > 0) {
                if(!profile.equalsIgnoreCase("local")){
                    awssqs.sendSQSMessage(list);
                }
                return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
            } else {
                return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
            }
        } else {
            // No Bills Found
            String message = "No Bill Found for this User";
            logger.info("Get Due BILL Request - No bills Found : /v1/bills/due/{x}");
            stopWatch.stop();
            statsDClient.recordExecutionTime("timer.v1.bills.due.x.api.get", stopWatch.getLastTaskTimeMillis());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
        }
    }
}
