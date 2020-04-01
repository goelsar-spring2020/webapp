package edu.neu.cloudwebapp.utility;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.*;
import edu.neu.cloudwebapp.model.BillDetails;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;
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

    @PostConstruct
    private void init() {
        this.amazonSQS = AmazonSQSClientBuilder.standard().build();  //to create AmazonSQS
    }

    public void sendSQSMessage(List<BillDetails> list) {
        for (BillDetails bd : list) {
            String message = "http://" + domainName + "/v1/bill/" + bd.getId();
            SendMessageResult result = this.getAmazonSQS().sendMessage(this.urlSQS, message);
        }
    }

    @Scheduled(fixedRate=10000)
    public String receiveSQSMessage() {
        final ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(urlSQS).withMaxNumberOfMessages(1);
        String message = "";
        while (true) {
            final List<Message> messages = amazonSQS.receiveMessage(receiveMessageRequest).getMessages();
            for (Message messageObject : messages) {
                message = messageObject.getBody();
                deleteMessage(messageObject);
            }
            return message;
        }
    }

    private void deleteMessage(Message messageObject) {
        final String messageReceiptHandle = messageObject.getReceiptHandle();
        amazonSQS.deleteMessage(new DeleteMessageRequest(urlSQS, messageReceiptHandle));

    }
}
