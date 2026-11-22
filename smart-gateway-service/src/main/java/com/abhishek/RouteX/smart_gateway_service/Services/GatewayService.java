package com.abhishek.RouteX.smart_gateway_service.Services;

import com.abhishek.RouteX.smart_gateway_service.Dto1.CompiledRoute;
import com.abhishek.RouteX.smart_gateway_service.Dto1.GatewayRoute;
import com.abhishek.RouteX.smart_gateway_service.Dto1.responseDto.GatewayErrorResponse;
import com.abhishek.RouteX.smart_gateway_service.Model.RateLimitResult;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class GatewayService {

    private RouteRegistry routeRegistry;
    private final ObjectMapper objectMapper;
    private final RateLimitingService rateLimitingService;

    public GatewayService(ObjectMapper objectMapper,
                          RateLimitingService rateLimitingService,
                          RouteRegistry routeRegistry) {
        this.objectMapper = objectMapper;
        this.rateLimitingService = rateLimitingService;
        this.routeRegistry = routeRegistry;
    }

    public ResponseEntity<byte[]> handle(HttpServletRequest request){

        long startedAt = System.nanoTime();
        String correlationID = correlationId(request);

        String outcome = "TARGET_ERROR";
        int statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
        boolean rateLimited = false;
        HttpHeaders policyHeaders = new HttpHeaders();

//  1. Find the matching CompiledRoute for the incoming HTTP request.
        Optional<CompiledRoute> matchedRoutes = routeRegistry.find(request.getRequestURI());

        // the route that client want to access is not present in the
        // AllowedRoute set then throw an error
        if(!matchedRoutes.isPresent()){
            outcome = "ROUTE_NOT_FOUND";
            statusCode = HttpStatus.NOT_FOUND.value();

            return error(HttpStatus.NOT_FOUND,
                    "No active route match found",
                    correlationID,null);
        }

        CompiledRoute compiledRoute = matchedRoutes.get();
        GatewayRoute gatewayRoute = compiledRoute.getGatewayRoute();

        // if the route is present but the Method client want to acces is not
        // present in the MethodsAllowed set then also throw an error
        if(!gatewayRoute.getAllowedMethods().contains(request.getMethod())){
            outcome = "METHOD_NOT_ALLOWED";
            statusCode = HttpStatus.METHOD_NOT_ALLOWED.value();

            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.ALLOW, String.join(", ", gatewayRoute.getAllowedMethods() ));

            return error(HttpStatus.METHOD_NOT_ALLOWED,
                    "HTTP method is not allowed for this route",
                    correlationID, headers);
        }

//  2. Identify and apply rate-limiting rules.

        RateLimitResult rateLimit = rateLimitingService.check(gatewayRoute, request.getRemoteAddr());
        policyHeaders = rateLimitHeaders(rateLimit);

        if(!rateLimit.isAllowed()){
            rateLimited = true;
            outcome = "RATE_LIMITED";
            statusCode = HttpStatus.TOO_MANY_REQUESTS.value();

            policyHeaders.set(HttpHeaders.RETRY_AFTER, String.valueOf(rateLimit.getRetryAfterSeconds()));

            return error(HttpStatus.TOO_MANY_REQUESTS,
                    "Rate Limit Exceeded",
                    correlationID, policyHeaders);
        }

//  3. Perform idempotency checks, if enabled



        // 4. Apply configured request header rules.

        // 5. Forward the request to the target backend service.

        // 6. Apply configured response header rules.

        // 7. Store the response if idempotency is enabled.

        // 8. Return the backend response to the client.
    }

    // Supporting methods

    // check if the correlationId is given by client
    // if not then generate it and attach it with the request
    private String correlationId(HttpServletRequest request){
        String correlationId = request.getHeader("X-Correlation-Id");

        if(correlationId == null || correlationId.trim().isEmpty()){
            return UUID.randomUUID().toString();
        }

        return correlationId;
    }

    // Attaching the headers
    private HttpHeaders rateLimitHeaders(RateLimitResult result){
        HttpHeaders headers = new HttpHeaders();
        if(result.getLimit() > 0){
            headers.set("X-RateLimit-Limit", String.valueOf(result.getLimit()));
            headers.set("X-RateLimti-Remaining", String.valueOf(result.getRemaining()));
        }

        return headers;
    }

    // Form the error when any kind of Error occurs
    private ResponseEntity<byte[]> error(HttpStatus status,
                                         String message,
                                         String correlationId,
                                         HttpHeaders additionalHeaders){
        GatewayErrorResponse gatewayErrorResponse = new GatewayErrorResponse(
                status.value(),
                message,
                correlationId,
                LocalDateTime.now());

        HttpHeaders headers = additionalHeaders == null ? new HttpHeaders() : additionalHeaders;

        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Correlation-Id", correlationId);

        return ResponseEntity.status(status).headers(headers)
                .body(objectMapper.writeValueAsString(gatewayErrorResponse).getBytes(StandardCharsets.UTF_8));
    }

}
