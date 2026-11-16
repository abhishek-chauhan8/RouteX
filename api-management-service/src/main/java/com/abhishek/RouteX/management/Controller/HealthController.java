package com.abhishek.RouteX.management.Controller;

import com.abhishek.RouteX.management.Dto.responseDto.HealthResponse;
import com.abhishek.RouteX.management.Service.HealthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/health")
public class HealthController {

    public HealthService healthService;

    public HealthController(HealthService healthService) {
        this.healthService = healthService;
    }

    @GetMapping
    public ResponseEntity<HealthResponse> health() {
        String status = healthService.getHealth();

        return ResponseEntity.ok(new HealthResponse(
                "UP",
                status,
                LocalDateTime.now()
        ));
    }
}
