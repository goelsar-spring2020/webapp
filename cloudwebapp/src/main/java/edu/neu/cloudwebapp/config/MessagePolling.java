package edu.neu.cloudwebapp.config;

import edu.neu.cloudwebapp.utility.AWSSNS;
import edu.neu.cloudwebapp.utility.AWSSQS;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@Profile("!local")
public class MessagePolling {

    @Autowired
    private AWSSNS awssns;

    @Autowired
    private AWSSQS awssqs;

    @Scheduled(fixedRate = 10000)
    public void pollMessage() throws JSONException {
        awssqs.receiveSQSMessage();
    }
}
