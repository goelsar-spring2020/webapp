package edu.neu.cloudwebapp.services;

import edu.neu.cloudwebapp.model.BillDetails;
import edu.neu.cloudwebapp.model.FileAttachment;
import edu.neu.cloudwebapp.repository.BillDetailsRepository;
import edu.neu.cloudwebapp.repository.UserRegistrationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;

@Service
public class FileWebService {

    @Autowired
    private BillDetailsRepository billDetailsRepository;

    @Autowired
    private UserRegistrationRepository userRegistrationRepository;

    public void addFileAttachment(BillDetails billDetails, String fileName) {
        if (billDetails != null && billDetails.getAttachment() == null) {
            FileAttachment file = new FileAttachment();
            file.setFileId(java.util.UUID.randomUUID().toString());
            file.setFileName(fileName);
            file.setUrl("/var/tmp/" + billDetails.getId() + "/" + fileName);
            file.setUploadDate(new Date());
            billDetails.setAttachment(file);
        } else {
            String message = "File is already exists"+ "\n" +"Delete the existing one to add the new file";
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }
}