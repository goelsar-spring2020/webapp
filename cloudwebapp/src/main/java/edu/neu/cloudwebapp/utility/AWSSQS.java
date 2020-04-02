package edu.neu.cloudwebapp.utility;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import edu.neu.cloudwebapp.model.BillDetails;
import edu.neu.cloudwebapp.services.BillWebService;
import lombok.Getter;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Date;
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

    @Autowired
    private BillWebService billWebService;

    private final static Logger logger = LoggerFactory.getLogger(AWSSQS.class);

    @PostConstruct
    private void init() {
        this.amazonSQS = AmazonSQSClientBuilder.standard().build();  //to create AmazonSQS
    }

    public void sendSQSMessage(String userEmail, String dueDays) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(userEmail + "," + dueDays);
        String resultantString = stringBuilder.toString();
        logger.info("Sending Message to SQS" + resultantString);
        SendMessageResult result = this.getAmazonSQS().sendMessage(this.urlSQS, resultantString);
        logger.info("Message Sent to SQS");
    }

    public void receiveSQSMessage() throws JSONException {
        final ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(urlSQS).withMaxNumberOfMessages(1);
        String message = "";
        while (true) {
            final List<Message> messages = amazonSQS.receiveMessage(receiveMessageRequest).getMessages();
            for (Message messageObject : messages) {
                message = messageObject.getBody();
                if (!message.isEmpty() && message != null) {
                    String finalMessage = this.computeMessage(message);
                    if (finalMessage != null && !finalMessage.isEmpty()) {
                        logger.info("Retrieved Message from SQS" + message);
                        String result = awssns.publishSNSMessage(message);
                        deleteMessage(messageObject);
                    }
                }
            }
        }
    }

    private void deleteMessage(Message messageObject) {
        final String messageReceiptHandle = messageObject.getReceiptHandle();
        try {
            amazonSQS.deleteMessage(new DeleteMessageRequest(urlSQS, messageReceiptHandle));
        } catch (Exception e) {
        }
    }

    public String computeMessage(String message) throws JSONException {

        String[] messageDetails = message.split(",");
        String userEmail = messageDetails[0];
        String dueDays = messageDetails[1];

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(userEmail + ",");
        int count = 0;

        List<BillDetails> listEntity = billWebService.getUserBillDetails(userEmail);
        for (BillDetails bd : listEntity) {
            Date d1 = new Date();
            Date d2 = bd.getDue_date();
            long noOfDaysBetween = (d1.getTime() - d2.getTime()) / 86400000;
            if (Math.abs(noOfDaysBetween) <= Long.valueOf(dueDays)) {
                count++;
                String res = "http://" + domainName + "/v1/bill/" + bd.getId();
                stringBuilder.append(res + ",");
            }
        }
        if(count > 0){
            return stringBuilder.toString().substring(0, stringBuilder.length() - 1);
        } else{
            return null;
        }
    }
}
