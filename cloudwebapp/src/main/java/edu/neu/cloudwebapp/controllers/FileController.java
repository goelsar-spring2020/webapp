package edu.neu.cloudwebapp.controllers;

import edu.neu.cloudwebapp.model.BillDetails;
import edu.neu.cloudwebapp.services.BillWebService;
import edu.neu.cloudwebapp.services.FileWebService;
import edu.neu.cloudwebapp.utility.UtilityClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@RestController
@CrossOrigin
public class FileController {

    @Autowired
    private BillWebService billWebService;

    @Autowired
    private FileWebService fileWebService;

    @Autowired
    private UtilityClass utilityClass;

    @RequestMapping(value = "/v1/bill/{id}/file", method = RequestMethod.POST, produces = "application/json",
            consumes = "multipart/form-data")
    public void postFileAttachment(@PathVariable(value = "id") String billId, @RequestHeader(value = "Authorization") String auth, @RequestParam MultipartFile attachment) throws IOException {
        if(attachment != null && auth != null){
            String authorization = utilityClass.authEncode(auth);
            String[] headerAuth = authorization.split(":");
            String email = headerAuth[0];
            String password = headerAuth[1];
            BillDetails billDetails = billWebService.getBillDetailsByUserId(billId, email);
            if(billDetails != null){
                File targetFile = new File("/var/tmp/" + billDetails.getId() + "/" + attachment.getOriginalFilename());
                File parent = targetFile.getParentFile();
                if (!parent.exists() && !parent.mkdirs()) {
                    throw new IllegalStateException("Couldn't create dir: " + parent);
                }
                parent.mkdirs();
                targetFile.createNewFile();
                FileOutputStream fout = new FileOutputStream(targetFile);
                fout.write(attachment.getBytes());
                fout.close();
            }
        }
    }

    @RequestMapping(value = "/v1/bill/{id}/file", method = RequestMethod.GET, produces = "application/json")
    public void getFileAttachment(){

    }

    @RequestMapping(value = "/v1/bill/{id}/file", method = RequestMethod.DELETE, produces = "application/json")
    public void deleteFileAttachment(){

    }
}
