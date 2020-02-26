package edu.neu.cloudwebapp.services;

import edu.neu.cloudwebapp.model.BillDetails;
import edu.neu.cloudwebapp.model.FileAttachment;
import org.springframework.web.multipart.MultipartFile;

public interface FileHandlerService {

    public FileAttachment uploadFile(MultipartFile attachment, BillDetails billDetails, String fileName) throws Exception;

    public boolean deleteFile(BillDetails billDetails) throws Exception;

}
