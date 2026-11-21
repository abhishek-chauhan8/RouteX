package com.abhishek.RouteX.smart_gateway_service.Dto1;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class HeaderRule {
    private Long id;
    private String direction;
    private String action;
    private String headerName;
    private String headerValue;
}
