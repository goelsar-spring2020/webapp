package edu.neu.cloudwebapp.services;

import edu.neu.cloudwebapp.model.BillDetails;
import edu.neu.cloudwebapp.model.FileAttachment;
import edu.neu.cloudwebapp.repository.BillDetailsRepository;
import edu.neu.cloudwebapp.utility.UtilityClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

@Service
@Profile("local")
public class FileWebService implements FileHandlerService {

    @Autowired
    private BillDetailsRepository billDetailsRepository;

    @Autowired
    private UtilityClass utilityClass;

    @Value("${path.to.file}")
    private String UPLOADED_FOLDER;

    private final static Logger logger = LoggerFactory.getLogger(FileWebService.class);

    @Override
    public FileAttachment uploadFile(MultipartFile attachment, BillDetails billDetails, String fileName) throws Exception {
        File targetFile = new File(UPLOADED_FOLDER + billDetails.getId() + "/" + fileName);
        File parent = targetFile.getParentFile();
        if (!parent.exists() && !parent.mkdirs()) {
            logger.error("Couldn't create dir");
            throw new IllegalStateException("Couldn't create dir: " + parent);
        }
        parent.mkdirs();
        targetFile.createNewFile();
        FileOutputStream fout = new FileOutputStream(targetFile);
        fout.write(attachment.getBytes());
        fout.close();
        String filepath = UPLOADED_FOLDER + billDetails.getId() + "/" + fileName;

        FileAttachment fileAttachment = new FileAttachment();
        fileAttachment.setId(java.util.UUID.randomUUID().toString());
        fileAttachment.setFile_name(fileName);
        fileAttachment.setUrl(filepath);
        fileAttachment.setUpload_date(new Date());
        fileAttachment.setContentType(attachment.getContentType());
        fileAttachment.setHashcode(String.valueOf(attachment.hashCode()));
        fileAttachment.setFilesize(String.valueOf(attachment.getSize()));
        fileAttachment.setMd5Hash(utilityClass.computeMD5Hash(attachment.getBytes()));
        billDetails.setAttachment(fileAttachment);
        billDetailsRepository.save(billDetails);
        return fileAttachment;
    }

    @Override
    public boolean deleteFile(BillDetails billDetails) throws Exception {
        File dir = new File(UPLOADED_FOLDER + billDetails.getId());

        if (dir.isDirectory() == false) {
            return false;
        }
        File[] listFiles = dir.listFiles();
        for (File file : listFiles) {
            file.delete();
        }
        dir.delete();
        return true;
    }
}