package com.abhishek.RouteX.management.Controller;

import com.abhishek.RouteX.management.Dto.requestDto.RouteRequest;
import com.abhishek.RouteX.management.Dto.requestDto.RouteStatusRequest;
import com.abhishek.RouteX.management.Dto.responseDto.AuditRecordResponse;
import com.abhishek.RouteX.management.Dto.responseDto.RouteResponse;
import com.abhishek.RouteX.management.Dto.responseDto.RouteVersionResponse;
import com.abhishek.RouteX.management.Service.RouteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tenant")
public class TenantRouteController {

    private final RouteService routeService;

    public TenantRouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    // <--- CRUD operations for ROUTES --->

    // 1- CREATE

    @PostMapping("/routes")
    public ResponseEntity<RouteResponse> create(@Valid @RequestBody RouteRequest request){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(routeService.create(request));
    }

    @PostMapping("/routes/bulk")
    public ResponseEntity<List<RouteResponse>> createBulk(
            @Valid @RequestBody List<@Valid RouteRequest> requests) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(routeService.createBulk(requests));
    }

    // 2- GET / GET ALL

    @GetMapping("/routes")
    public ResponseEntity<List<RouteResponse>> findAll(){
        return ResponseEntity.ok(routeService.findAllForTenant());
    }

    @GetMapping("/routes/{id}")
    public ResponseEntity<RouteResponse> findById(@PathVariable Long id){
        return ResponseEntity.ok(routeService.findById(id));
    }

    // 3- UPDATE

    @PutMapping("/routes/{id}")
    public ResponseEntity<RouteResponse> update(@PathVariable Long id,
                                                @Valid @RequestBody RouteRequest request){
        return ResponseEntity.ok(routeService.update(id, request));
    }

    // 4- DELETE

    @DeleteMapping("/routes/{id}")
    public  ResponseEntity<Void> delete(@PathVariable Long id){
        routeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // 5- PATCH (update status)

    @PatchMapping(("/routes/{id}/status"))
    public ResponseEntity<RouteResponse> changeStatus(@PathVariable Long id,
                                                      @Valid @RequestBody RouteStatusRequest routeStatusRequest){
        return ResponseEntity.ok(routeService.changeStatus(id, routeStatusRequest));
    }

    @GetMapping("/route-versions/{routeId}")
    public ResponseEntity<List<RouteVersionResponse>> getVersions(
            @PathVariable String routeId){
        return ResponseEntity.ok(routeService.getVersions(routeId));
    }

    @GetMapping("/audit-records")
    public ResponseEntity<List<AuditRecordResponse>> getAuditRecords(){
        return  ResponseEntity.ok(routeService.getAuditRecords());
    }

}
