package edu.neu.cloudwebapp.controllers;

import edu.neu.cloudwebapp.model.BillDetails;
import edu.neu.cloudwebapp.model.FileAttachment;
import edu.neu.cloudwebapp.repository.BillDetailsRepository;
import edu.neu.cloudwebapp.services.BillWebService;
import edu.neu.cloudwebapp.services.FileWebService;
import edu.neu.cloudwebapp.utility.UtilityClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

@RestController
@CrossOrigin
public class FileController {

    private static String UPLOADED_FOLDER = "/var/tmp/";
    @Autowired
    private BillWebService billWebService;
    @Autowired
    private FileWebService fileWebService;
    @Autowired
    private UtilityClass utilityClass;
    @Autowired
    private BillDetailsRepository billDetailsRepository;

    @RequestMapping(value = "/v1/bill/{id}/file", method = RequestMethod.POST, produces = "application/json",
            consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    public FileAttachment postFileAttachment(@PathVariable(value = "id") String billId, @RequestHeader(value = "Authorization") String auth, @RequestParam("file") MultipartFile attachment) throws IOException, NoSuchAlgorithmException {
        if (attachment != null && auth != null) {
            String fileName = attachment.getOriginalFilename();
            if (fileName.endsWith(".pdf") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png")) {
                String authorization = UtilityClass.authEncode(auth);
                String[] headerAuth = authorization.split(":");
                String email = headerAuth[0];
                String password = headerAuth[1];
                BillDetails billDetails = billWebService.getBillDetailsByUserId(billId, email);
                if (billDetails != null) {
                    File targetFile = new File(UPLOADED_FOLDER + billDetails.getId() + "/" + fileName);
                    File parent = targetFile.getParentFile();
                    if (!parent.exists() && !parent.mkdirs()) {
                        throw new IllegalStateException("Couldn't create dir: " + parent);
                    }
                    parent.mkdirs();
                    targetFile.createNewFile();
                    FileOutputStream fout = new FileOutputStream(targetFile);
                    fout.write(attachment.getBytes());
                    fout.close();
                    return fileWebService.addFileAttachment(billDetails, fileName, attachment.getContentType(), attachment.hashCode(), attachment.getSize(), attachment.getBytes());
                } else {
                    String message = "No Bill Id found";
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
                }
            } else {
                String message = "Unsupportive File Type";
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
            }
        } else {
            String message = "No valid File Attached";
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }

    @RequestMapping(value = "/v1/bill/{billId}/file/{fileId}", method = RequestMethod.GET, produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public FileAttachment getFileAttachment(@PathVariable(value = "billId") String
                                                    billId, @PathVariable(value = "fileId") String fileId, @RequestHeader(value = "Authorization") String auth) {
        if (billId != null && fileId != null) {
            String authorization = UtilityClass.authEncode(auth);
            String[] headerAuth = authorization.split(":");
            String email = headerAuth[0];
            String password = headerAuth[1];
            BillDetails billDetails = billWebService.getBillDetailsByUserId(billId, email);
            if (billDetails.getAttachment().getId().equalsIgnoreCase(fileId)) {
                return billDetails.getAttachment();
            } else {
                String message = "No Attachments found for this Bill ID";
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
            }
        } else {
            String message = "No File found for this Bill ID";
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
        }
    }

    @RequestMapping(value = "/v1/bill/{billId}/file/{fileId}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFileAttachment(@PathVariable(value = "billId") String
                                             billId, @PathVariable(value = "fileId") String fileId, @RequestHeader(value = "Authorization") String auth) {
        if (billId != null) {
            String authorization = UtilityClass.authEncode(auth);
            String[] headerAuth = authorization.split(":");
            String email = headerAuth[0];
            String password = headerAuth[1];
            BillDetails billDetails = billWebService.getBillDetailsByUserId(billId, email);
            if (billDetails.getAttachment() != null && billDetails.getAttachment().getId().equalsIgnoreCase(fileId)) {
                utilityClass.deleteFile(billId);
                billDetails.setAttachment(null);
                billDetailsRepository.save(billDetails);
            } else {
                String message = "File Attachment Does Not exists";
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
            }
        } else {
            String message = "Bill ID is mandatory";
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
        }
    }
}
