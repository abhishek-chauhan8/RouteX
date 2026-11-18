package com.abhishek.RouteX.management.Controller;

import com.abhishek.RouteX.management.Dto.requestDto.TenantOrganizationRequest;
import com.abhishek.RouteX.management.Dto.responseDto.OrganizationResponse;
import com.abhishek.RouteX.management.Dto.responseDto.TenantAdminResponse;
import com.abhishek.RouteX.management.Entity.Organization;
import com.abhishek.RouteX.management.Service.OrganizationService;
import jakarta.validation.Valid;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tenant/organization")
public class TenantOrganizationController {
    private final OrganizationService organizationService;

    public TenantOrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @GetMapping
    public ResponseEntity<OrganizationResponse> getOwnOrganization(){
        return ResponseEntity.ok(organizationService.getOwnOrganization());
    }

    @PutMapping
    public ResponseEntity<OrganizationResponse> updateOwnOrganization(
            @Valid @RequestBody TenantOrganizationRequest request
    ){
        return ResponseEntity.ok(organizationService.updateOwnOrganization(request));
    }
}
