package com.abhishek.RouteX.management.Dto.responseDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class ErrorResponse {
    // we can have Status, Message, Error, Timestamps etc

    private int status;
    private String message;
    private Map<String, String> errors;
    private LocalDateTime  timestamp;
}
