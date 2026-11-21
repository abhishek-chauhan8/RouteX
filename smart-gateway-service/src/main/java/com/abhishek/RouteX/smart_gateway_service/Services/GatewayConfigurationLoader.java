package com.abhishek.RouteX.smart_gateway_service.Services;

import com.abhishek.RouteX.smart_gateway_service.Client.ManagementApiClient;
import com.abhishek.RouteX.smart_gateway_service.Dto1.GatewayRoute;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GatewayConfigurationLoader implements ApplicationRunner {

    private final ManagementApiClient client;

    public GatewayConfigurationLoader(ManagementApiClient client) {
        this.client = client;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        refresh();
    }

    public void refresh(){
        List<GatewayRoute> routes = client.fetchActiveRoutes();

        for(GatewayRoute  route : routes){
            System.out.println("Route : " + route.getTargetPathPrefix());
        }
    }
}
