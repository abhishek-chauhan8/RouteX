package com.abhishek.RouteX.management.Dto.responseDto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter
@Setter
public class HealthResponse {

    private String serviceStatus;
    private String dbStatus;
    private LocalDateTime timeStamp;

    public HealthResponse(String serviceStatus,
                          String dbStatus,
                          LocalDateTime timeStamp) {
        this.serviceStatus = serviceStatus;
        this.dbStatus = dbStatus;
        this.timeStamp = timeStamp;
    }
}

