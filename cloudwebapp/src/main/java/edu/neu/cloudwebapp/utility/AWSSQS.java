package edu.neu.cloudwebapp.utility;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.*;
import edu.neu.cloudwebapp.model.BillDetails;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
@Scope("singleton")
@Profile("!local")
public final class AWSSQS {

    @Getter
    private AmazonSQS amazonSQS;

    @Value("${amazon.domain.name}")
    private String domainName;

    @Value("${amazon.sqs.url}")
    private String urlSQS;

    @Autowired
    private AWSSNS awssns;

    private final static Logger logger = LoggerFactory.getLogger(AWSSQS.class);

    @PostConstruct
    private void init() {
        this.amazonSQS = AmazonSQSClientBuilder.standard().build();  //to create AmazonSQS
    }

    public void sendSQSMessage(List<BillDetails> list, String email) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(email+";");
        for (BillDetails bd : list) {
            String message = "http://" + domainName + "/v1/bill/" + bd.getId();
            stringBuilder.append(message+";");
        }
        String resultantString = stringBuilder.toString().substring(0, stringBuilder.length()-1);
        logger.info("Sending Message to SQS" + resultantString);
        SendMessageResult result = this.getAmazonSQS().sendMessage(this.urlSQS, resultantString);
        logger.info("Message Sent to SQS");
    }

    public void receiveSQSMessage() {
        final ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(urlSQS).withMaxNumberOfMessages(1);
        String message = "";
        while (true) {
            final List<Message> messages = amazonSQS.receiveMessage(receiveMessageRequest).getMessages();
            for (Message messageObject : messages) {
                message = messageObject.getBody();
                if(!message.isEmpty() && message!=null){
                    logger.info("Retrieved Message from SQS" + message);
                    String result = awssns.publishSNSMessage(message);
                    deleteMessage(messageObject);
                }
            }
        }
    }

    private void deleteMessage(Message messageObject) {
        final String messageReceiptHandle = messageObject.getReceiptHandle();
        try{
            amazonSQS.deleteMessage(new DeleteMessageRequest(urlSQS, messageReceiptHandle));
        }catch (Exception e){
        }
    }
}
