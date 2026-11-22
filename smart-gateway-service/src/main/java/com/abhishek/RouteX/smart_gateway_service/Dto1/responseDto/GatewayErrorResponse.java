package com.abhishek.RouteX.smart_gateway_service.Dto1.responseDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class GatewayErrorResponse {
    private int status;
    private String message;
    private String correlationId;
    private LocalDateTime timeStamp;
}
