package com.example.dswan.services;

import com.example.dswan.util.CustomHeader;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;

@Service
@Slf4j
public class PubSubService {

    private final ObjectMapper mapper;
    private final PubSubTemplate pubSubTemplate;

    @Getter
    public final String eventTopicName;

    @Autowired
    public PubSubService(final ObjectMapper mapper,
                         final PubSubTemplate pubSubTemplate,
                         final @Value("${pubsub.publish.example.processed}") String exampleTopicName) {
        this.mapper = mapper;
        this.pubSubTemplate = pubSubTemplate;
        this.eventTopicName = exampleTopicName;
    }

    public void publishMessage(String topic, Object payload) throws JsonProcessingException {
        log.info("Publishing Payload: {} to Pub/Sub Topic: {}", mapper.writeValueAsString(payload), topic);
        try {
            HashMap<String, String> attributes = new HashMap<>(CustomHeader.injectTraceHeaders());
            attributes.put("topic", topic);

            String transactionData = mapper.writeValueAsString(payload);
            PubsubMessage pubSubMessage = PubsubMessage.newBuilder()
                                                       .putAllAttributes(attributes)
                                                       .setData(ByteString.copyFromUtf8(transactionData))
                                                       .build();

            String messageId = pubSubTemplate.publish(topic, pubSubMessage).get();
            log.info("Pub/Sub publish success.  Topic: {}, MessageId: {}", topic, messageId);
        } catch (Exception ex) {
            log.error("Failed to parse and publish subscription transaction: ", ex);
        }
    }
}
