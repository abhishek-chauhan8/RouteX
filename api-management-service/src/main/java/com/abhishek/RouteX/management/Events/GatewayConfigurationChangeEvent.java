package com.abhishek.RouteX.management.Events;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class GatewayConfigurationChangeEvent {
    private String eventId;
    private String eventType;
    private String changeType;
    private String routeId;
    private Long OrganizationId;
    private Long configurationVersion;
    private LocalDateTime localDateTime;

}
