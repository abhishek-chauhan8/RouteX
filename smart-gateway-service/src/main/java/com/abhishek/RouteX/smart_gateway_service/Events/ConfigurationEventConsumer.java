package com.abhishek.RouteX.smart_gateway_service.Events;

import com.abhishek.RouteX.smart_gateway_service.Services.GatewayConfigurationLoader;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ConfigurationEventConsumer {

    private final GatewayConfigurationLoader loader;

    public ConfigurationEventConsumer(GatewayConfigurationLoader loader) {
        this.loader = loader;
    }

    @KafkaListener(topics = "${gateway.kafka.configuration-topic}",
            groupId = "${gateway.kafka.configuration-group}")
    public void configurationChanged(String event){
        System.out.println("Gateway configuration notification recieved : " + event);
        loader.refresh();
        System.out.println("Gateway configuration notification updated : " + event);
    }
}
