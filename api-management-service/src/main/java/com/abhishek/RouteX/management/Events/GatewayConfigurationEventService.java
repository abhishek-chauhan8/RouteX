package com.abhishek.RouteX.management.Events;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service

// This is a LocalEvent that will be listened by the EventListener
public class GatewayConfigurationEventService {

    private ApplicationEventPublisher applicationEventPublisher;

    public GatewayConfigurationEventService(
            ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void publish(String changeType,
                        Long organizationId,
                        String routeId,
                        Long configurationVersion){
        applicationEventPublisher.publishEvent(new GatewayConfigurationChangeEvent(
                UUID.randomUUID().toString(),"GATEWAY_CONFIGURATION_CHANGE",
                changeType, routeId,
                organizationId,configurationVersion,
                LocalDateTime.now()
        ));    }
}
