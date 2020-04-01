package edu.neu.cloudwebapp.utility;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishResult;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;

@Component
@Profile("!local")
public final class AWSSNS {

    @Value("${sns.topic.arn}")
    private String snsTopicARN;

    @Getter
    private AmazonSNS amazonSNS;

    @PostConstruct
    private void init() {
        this.amazonSNS = AmazonSNSClientBuilder.standard().build();  //to create AmazonSQS
    }

    public String publishSNSMessage(String message) {
        PublishRequest publishRequest = new PublishRequest(snsTopicARN, message);
        PublishResult publishResponse = this.amazonSNS.publish(publishRequest);
        return publishResponse.getMessageId();
    }
}
