package com.abhishek.RouteX.smart_gateway_service.Dto1.requestDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class LoginRequest {
    private String username;
    private String password;
}
