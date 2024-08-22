package com.example.dswan.configuration;

import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.cloud.spring.pubsub.integration.AckMode;
import com.google.cloud.spring.pubsub.integration.inbound.PubSubInboundChannelAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.messaging.MessageChannel;

@Configuration
@Profile("!localdb")
@Slf4j
public class PubSubConfiguration {

    @Value("${pubsub.subscribe.example.name}")
    private String subscriptionTopicName;

    @Value("${pubsub.subscribe.example.dlq-name}")
    private String subscriptionDLQTopicName;

    @Bean(name = "pubsubReceiverChannel")
    public MessageChannel inputMessageChannel() {
        return new PublishSubscribeChannel();
    }

    // Create an inbound channel adapter to listen to the subscription `subscription-transaction-created`
    @Bean(name = "channelAdapter")
    public PubSubInboundChannelAdapter inboundChannelAdapter(@Qualifier("pubsubReceiverChannel") MessageChannel messageChannel, PubSubTemplate pubSubTemplate) {
        PubSubInboundChannelAdapter adapter = new PubSubInboundChannelAdapter(pubSubTemplate, subscriptionTopicName);
        adapter.setOutputChannel(messageChannel);
        adapter.setAckMode(AckMode.MANUAL);
        adapter.setPayloadType(String.class);
        return adapter;
    }

    @Bean(name = "dlqReceiverChannel")
    public MessageChannel dlqMessageChannel() {
        return new PublishSubscribeChannel();
    }

    @Bean
    public PubSubInboundChannelAdapter dlqInboundAdapter(@Qualifier("dlqReceiverChannel") MessageChannel messageChannel, PubSubTemplate pubSubTemplate) {
        PubSubInboundChannelAdapter adapter = new PubSubInboundChannelAdapter(pubSubTemplate, subscriptionDLQTopicName);
        adapter.setOutputChannel(messageChannel);
        adapter.setAckMode(AckMode.AUTO_ACK);
        adapter.setPayloadType(String.class);
        return adapter;
    }
}
