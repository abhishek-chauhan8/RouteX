package com.abhishek.RouteX.smart_gateway_service.Client;

import com.abhishek.RouteX.smart_gateway_service.Dto1.GatewayRoute;
import com.abhishek.RouteX.smart_gateway_service.Dto1.requestDto.LoginRequest;
import com.abhishek.RouteX.smart_gateway_service.Dto1.responseDto.LoginResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.List;

@Component
public class ManagementApiClient {

    private final RestClient restClient;
    private final String username;
    private final String password;
    private String token;
    private Instant expiresAt;

    public ManagementApiClient(@Value("${management.api.base-url}") String baseUrl,
                               @Value("${management.api.username}") String username,
                               @Value("${management.api.password}") String password) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.username = username;
        this.password = password;
    }

    public List<GatewayRoute> fetchActiveRoutes(){
        ensureToken();
        try{
            return requestRoutes();
        }
        catch(HttpClientErrorException exception){
            if(exception.getStatusCode() != HttpStatus.UNAUTHORIZED){
                throw  exception;
            }
            authenticate();
            return requestRoutes();
        }
    }

    // "Synchronized"	Allows only one thread at a time to execute this method on the same object instance.
    private synchronized void ensureToken(){
        if (token == null || expiresAt == null ||
                Instant.now().plusSeconds(30).isAfter(expiresAt)){
            authenticate();
        }
    }

    private synchronized void authenticate(){
        // Initiates an HTTP POST request.
        LoginResponse loginResponse = restClient.post()

                // Specifies the login endpoint.
                .uri("api/auth/login")

                // Sends username and password as the request body.
                .body(new LoginRequest(username, password))

                // Executes the request and prepares response retrieval.
                .retrieve()

                // Converts the response body into a Java LoginResponse object.
                .body(LoginResponse.class);

        if(loginResponse == null || loginResponse.getToken() == null || !"GATEWAY_SERVICE".equals(loginResponse.getRole())){
            throw new IllegalStateException("Api Management did not return a GATEWAY SERVICE TOKEN");
        }

        token = loginResponse.getToken();
        expiresAt = Instant.now().plusSeconds(loginResponse.getExpiresInSeconds());
    }

    private List<GatewayRoute> requestRoutes(){
        return restClient.get()
                .uri("/api/gateway/routes")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(new ParameterizedTypeReference<List<GatewayRoute>>() {});
    }
}
