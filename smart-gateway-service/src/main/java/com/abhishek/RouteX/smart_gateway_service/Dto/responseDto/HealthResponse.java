package com.abhishek.RouteX.smart_gateway_service.Dto.responseDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class HealthResponse {

    private String serviceName;
    private String serviceDescription;
    private String appStatus;

    public HealthResponse(String serviceName, String serviceDescription, String appStatus) {
        this.serviceName = serviceName;
        this.serviceDescription = serviceDescription;
        this.appStatus = appStatus;
    }
}
