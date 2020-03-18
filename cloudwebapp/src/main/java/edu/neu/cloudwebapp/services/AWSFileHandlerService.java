package edu.neu.cloudwebapp.services;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.timgroup.statsd.StatsDClient;
import edu.neu.cloudwebapp.model.BillDetails;
import edu.neu.cloudwebapp.model.FileAttachment;
import edu.neu.cloudwebapp.repository.BillDetailsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

@Service
@Profile("!local")
public class AWSFileHandlerService implements FileHandlerService {

    @Autowired
    private BillDetailsRepository billDetailsRepository;

    @Autowired
    private StatsDClient statsDClient;

    private AmazonS3 s3client;

    @Value("${amazon.s3.bucketName}")
    private String bucketName;

    private final static Logger logger = LoggerFactory.getLogger(AWSFileHandlerService.class);

    @PostConstruct
    private void initializeAmazon() {
        this.s3client = AmazonS3ClientBuilder.standard().build();
    }

    @Override
    public FileAttachment uploadFile(MultipartFile attachment, BillDetails billDetails, String fileName) throws Exception {
        String name = billDetails.getId() + "/" + fileName;
        InputStream inputStream = null;
        StopWatch stopWatchS3 = new StopWatch();
        stopWatchS3.start();
        ObjectMetadata metadata = new ObjectMetadata();
        try {
            inputStream = attachment.getInputStream();
            metadata.setContentLength(attachment.getSize());
            metadata.setContentType(attachment.getContentType());
        } catch (IOException e) {
            logger.error("Error in uploading File to S3: "+e.getMessage());
            e.printStackTrace();
        }
        PutObjectResult result = s3client.putObject(bucketName, name, attachment.getInputStream(), metadata);
        S3Object obj = s3client.getObject(bucketName, name);
        stopWatchS3.stop();
        statsDClient.recordExecutionTime("timer.s3.v1.bill.id.file.api.post",stopWatchS3.getLastTaskTimeMillis());

        FileAttachment fileAttachment = new FileAttachment();
        fileAttachment.setId(java.util.UUID.randomUUID().toString());
        fileAttachment.setFile_name(fileName);
        fileAttachment.setUrl(name);
        fileAttachment.setUpload_date(new Date());
        fileAttachment.setContentType(obj.getObjectMetadata().getContentType());
        fileAttachment.setHashcode(String.valueOf(obj.getObjectContent().hashCode()));
        fileAttachment.setFilesize(String.valueOf(obj.getObjectMetadata().getContentLength()));
        fileAttachment.setMd5Hash(result.getContentMd5());
        billDetails.setAttachment(fileAttachment);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        billDetailsRepository.save(billDetails);
        stopWatch.stop();
        statsDClient.recordExecutionTime("timer.db.v1.bill.id.file.api.post",stopWatch.getLastTaskTimeMillis());
        logger.info("Successfully uploaded object from S3");
        return fileAttachment;
    }

    @Override
    public boolean deleteFile(BillDetails billDetails) throws Exception {
        String fileName = billDetails.getAttachment().getUrl().toString();
        StopWatch stopWatchS3 = new StopWatch();
        stopWatchS3.start();
        s3client.deleteObjects(new DeleteObjectsRequest(bucketName).withKeys(fileName));
        stopWatchS3.stop();
        statsDClient.recordExecutionTime("timer.s3.v1.bill.id.file.api.delete",stopWatchS3.getLastTaskTimeMillis());
        logger.info("Successfully deleted object from S3");
        return true;
    }
}
