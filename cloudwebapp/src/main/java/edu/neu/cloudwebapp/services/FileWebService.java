package edu.neu.cloudwebapp.services;

import edu.neu.cloudwebapp.model.BillDetails;
import edu.neu.cloudwebapp.model.FileAttachment;
import edu.neu.cloudwebapp.repository.BillDetailsRepository;
import edu.neu.cloudwebapp.repository.UserRegistrationRepository;
import edu.neu.cloudwebapp.utility.UtilityClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.NoSuchAlgorithmException;
import java.util.Date;

@Service
public class FileWebService {

    @Autowired
    private BillDetailsRepository billDetailsRepository;

    @Autowired
    private UserRegistrationRepository userRegistrationRepository;

    @Autowired
    private UtilityClass utilityClass;

    @Value("${path.to.file}")
    private String UPLOADED_FOLDER;

    public FileAttachment addFileAttachment(BillDetails billDetails, String fileName, String contentType, int hashcode, long filesize, byte[] getbytes) throws NoSuchAlgorithmException {
        if (billDetails != null && billDetails.getAttachment() == null) {
            FileAttachment file = new FileAttachment();
            file.setId(java.util.UUID.randomUUID().toString());
            file.setFile_name(fileName);
            file.setUrl(UPLOADED_FOLDER + billDetails.getId() + "/" + fileName);
            file.setUpload_date(new Date());
            file.setContentType(contentType);
            file.setHashcode(String.valueOf(hashcode));
            file.setFilesize(String.valueOf(filesize));
            file.setMd5Hash(utilityClass.computeMD5Hash(getbytes));
            billDetails.setAttachment(file);
            billDetailsRepository.save(billDetails);
            return file;
        } else {
            String message = "A File Attachment already exists";
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }
}