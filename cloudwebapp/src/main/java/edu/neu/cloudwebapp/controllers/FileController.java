package edu.neu.cloudwebapp.controllers;

import com.timgroup.statsd.StatsDClient;
import edu.neu.cloudwebapp.model.BillDetails;
import edu.neu.cloudwebapp.model.FileAttachment;
import edu.neu.cloudwebapp.repository.BillDetailsRepository;
import edu.neu.cloudwebapp.services.BillWebService;
import edu.neu.cloudwebapp.services.FileHandlerService;
import edu.neu.cloudwebapp.utility.UtilityClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@CrossOrigin
public class FileController {

    private final static Logger logger = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private FileHandlerService fileHandlerService;
    @Autowired
    private BillWebService billWebService;
    @Autowired
    private BillDetailsRepository billDetailsRepository;
    @Autowired
    private StatsDClient statsDClient;

    @RequestMapping(value = "/v1/bill/{id}/file", method = RequestMethod.POST, produces = "application/json",
            consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    public FileAttachment postFileAttachment(@PathVariable(value = "id") String billId, @RequestHeader(value = "Authorization") String auth, @RequestParam("file") MultipartFile attachment) throws Exception {
        statsDClient.incrementCounter("endpoint.v1.bill.id.file.api.post");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        logger.info("Post File request : \"/v1/bill/{id}/file\"");
        if (attachment != null && auth != null) {
            String fileName = attachment.getOriginalFilename();
            if (fileName.endsWith(".pdf") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png")) {
                String authorization = UtilityClass.authEncode(auth);
                String[] headerAuth = authorization.split(":");
                String email = headerAuth[0];
                String password = headerAuth[1];
                BillDetails billDetails = billWebService.getBillDetailsByUserId(billId, email);
                if (billDetails != null) {
                    if (billDetails.getAttachment() == null) {
                        FileAttachment fileAttachment = fileHandlerService.uploadFile(attachment, billDetails, fileName);
                        if (fileAttachment != null) {
                            logger.debug("HTTP : 201 Created");
                            stopWatch.stop();
                            statsDClient.recordExecutionTime("timer.v1.bill.id.file.api.post",stopWatch.getLastTaskTimeMillis());
                            return fileAttachment;
                        }
                    } else {
                        String message = "A File Attachment already exists";
                        logger.error("Post File Request - File attachment already exists : /v1/bill/{id}/file");
                        stopWatch.stop();
                        statsDClient.recordExecutionTime("timer.v1.bill.id.file.api.post",stopWatch.getLastTaskTimeMillis());
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
                    }
                } else {
                    String message = "No Bill Id found";
                    logger.error("Post File Request - No Bill Id found : /v1/bill/{id}/file");
                    stopWatch.stop();
                    statsDClient.recordExecutionTime("timer.v1.bill.id.file.api.post",stopWatch.getLastTaskTimeMillis());
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
                }
            } else {
                String message = "Unsupportive File Type";
                logger.error("Post File Request - Unsupported file type : /v1/bill/{id}/file");
                stopWatch.stop();
                statsDClient.recordExecutionTime("timer.v1.bill.id.file.api.post",stopWatch.getLastTaskTimeMillis());
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
            }
        } else {
            String message = "No valid File Attached";
            logger.error("Post File Request - No valid file attached : /v1/bill/{id}/file");
            stopWatch.stop();
            statsDClient.recordExecutionTime("timer.v1.bill.id.file.api.post",stopWatch.getLastTaskTimeMillis());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
        return null;
    }

    @RequestMapping(value = "/v1/bill/{billId}/file/{fileId}", method = RequestMethod.GET, produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public FileAttachment getFileAttachment(@PathVariable(value = "billId") String
                                                    billId, @PathVariable(value = "fileId") String fileId, @RequestHeader(value = "Authorization") String auth) {
        statsDClient.incrementCounter("endpoint.v1.bill.billId.file.fileId.api.get");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        logger.info("Get File request : \"/v1/bill/{billId}/file/{fileId}\"");
        if (billId != null && fileId != null) {
            String authorization = UtilityClass.authEncode(auth);
            String[] headerAuth = authorization.split(":");
            String email = headerAuth[0];
            String password = headerAuth[1];
            BillDetails billDetails = billWebService.getBillDetailsByUserId(billId, email);
            if (billDetails.getAttachment() != null && billDetails.getAttachment().getId().equalsIgnoreCase(fileId)) {
                logger.debug("HTTP : 200 OK");
                stopWatch.stop();
                statsDClient.recordExecutionTime("timer.v1.bill.billId.file.fileId.api.get",stopWatch.getLastTaskTimeMillis());
                return billDetails.getAttachment();
            } else {
                String message = "No Attachments found for this Bill ID";
                logger.error("Get File Request - No attachment for Bill ID : /v1/bill/{billId}/file/{fileId}");
                stopWatch.stop();
                statsDClient.recordExecutionTime("timer.v1.bill.billId.file.fileId.api.get",stopWatch.getLastTaskTimeMillis());
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
            }
        } else {
            String message = "No File found for this Bill ID";
            logger.error("Get File Request - No File ID for Bill ID : /v1/bill/{billId}/file/{fileId}");
            stopWatch.stop();
            statsDClient.recordExecutionTime("timer.v1.bill.billId.file.fileId.api.get",stopWatch.getLastTaskTimeMillis());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
        }
    }

    @RequestMapping(value = "/v1/bill/{billId}/file/{fileId}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFileAttachment(@PathVariable(value = "billId") String
                                             billId, @PathVariable(value = "fileId") String fileId, @RequestHeader(value = "Authorization") String auth) throws Exception {
        statsDClient.incrementCounter("endpoint.v1.bill.billId.file.fileId.api.delete");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        logger.info("Delete File request : \"/v1/bill/{billId}/file/{fileId}\"");
        if (billId != null) {
            String authorization = UtilityClass.authEncode(auth);
            String[] headerAuth = authorization.split(":");
            String email = headerAuth[0];
            String password = headerAuth[1];
            BillDetails billDetails = billWebService.getBillDetailsByUserId(billId, email);
            if (billDetails.getAttachment() != null && billDetails.getAttachment().getId().equalsIgnoreCase(fileId)) {
                fileHandlerService.deleteFile(billDetails);
                billDetails.setAttachment(null);
                logger.debug("HTTP : 204 No_Content");
                stopWatch.stop();
                statsDClient.recordExecutionTime("timer.v1.bill.billId.file.fileId.api.delete",stopWatch.getLastTaskTimeMillis());
                billDetailsRepository.save(billDetails);
            } else {
                String message = "File Attachment Does Not exists";
                logger.error("Delete File Request - File Attachment doesn't exist : /v1/bill/{billId}/file/{fileId}");
                stopWatch.stop();
                statsDClient.recordExecutionTime("timer.v1.bill.billId.file.fileId.api.delete",stopWatch.getLastTaskTimeMillis());
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
            }
        } else {
            String message = "Bill ID is mandatory";
            logger.error("Delete File Request - BillID is mandatory : /v1/bill/{billId}/file/{fileId}");
            stopWatch.stop();
            statsDClient.recordExecutionTime("timer.v1.bill.billId.file.fileId.api.delete",stopWatch.getLastTaskTimeMillis());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
        }
    }
}
