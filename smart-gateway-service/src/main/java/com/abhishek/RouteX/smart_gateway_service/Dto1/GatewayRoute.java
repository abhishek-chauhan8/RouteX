package com.abhishek.RouteX.smart_gateway_service.Dto1;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class GatewayRoute {

    private Long id;
    private Long organizationId;
    private String routeId;
    private String name;
    private String pathPattern;
    private String targetBaseUrl;
    private String targetPathPrefix;
    private Integer requestsPerMinute;
    private Integer responseTimeoutMs;
    private boolean idempotencyEnabled;
    private boolean active;
    private Long configurationVersion;

    private Set<String> allowedMethods = new HashSet<>();
    private List<HeaderRule> headerRules = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


}