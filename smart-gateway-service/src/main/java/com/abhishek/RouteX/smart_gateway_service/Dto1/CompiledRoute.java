package com.abhishek.RouteX.smart_gateway_service.Dto1;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.util.pattern.PathPattern;

@Getter
@AllArgsConstructor
public class CompiledRoute {
    private GatewayRoute gatewayRoute;
    // Defines URL path patterns to match incoming HTTP requests
    // and identify the corresponding gateway route.
    private PathPattern pathPattern;
    private String sourcePathPrefix;

}
