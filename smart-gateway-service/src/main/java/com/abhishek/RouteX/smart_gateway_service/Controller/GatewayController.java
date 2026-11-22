package com.abhishek.RouteX.smart_gateway_service.Controller;

import com.abhishek.RouteX.smart_gateway_service.Services.GatewayService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/**")
public class GatewayController {
    private GatewayService gatewayService;

    public GatewayController(GatewayService gatewayService) {
        this.gatewayService = gatewayService;
    }

    // A proxy receives a request on behalf of the client,
    // forwards it to another server, and returns that server's response to the client.
    @RequestMapping("/**")
    public ResponseEntity<byte[]> proxy(HttpServletRequest request){
        return gatewayService.handle(request);
    }
}
