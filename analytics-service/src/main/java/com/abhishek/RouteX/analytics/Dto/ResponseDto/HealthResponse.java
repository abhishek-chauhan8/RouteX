package com.abhishek.RouteX.analytics.Dto.ResponseDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HealthResponse {

    private String serviceName;
    private String serviceDescription;
    private String appStatus;

    public HealthResponse(String serviceName,
                          String serviceDescription,
                          String appStatus) {
        this.serviceName = serviceName;
        this.serviceDescription = serviceDescription;
        this.appStatus = appStatus;
    }
}


