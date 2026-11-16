package com.abhishek.RouteX.management.Controller;

import com.abhishek.RouteX.management.Dto.requestDto.OrganizationRequest;
import com.abhishek.RouteX.management.Dto.requestDto.OrganizationStatusRequest;
import com.abhishek.RouteX.management.Dto.responseDto.OrganizationResponse;
import com.abhishek.RouteX.management.Service.OrganizationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/platform/organization")
public class PlatformOrganizationController {
    private OrganizationService organizationService;

    public PlatformOrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    // CRUD operations

    // 1- Create
    @PostMapping
    public ResponseEntity<OrganizationResponse> create(@Valid @RequestBody
                                                       OrganizationRequest
                                                                   organizationRequest){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(organizationService.create(organizationRequest));
    }

    // 2- Read All
    @GetMapping
    public ResponseEntity<List<OrganizationResponse>> findAll(){
        return ResponseEntity.ok(organizationService.findAll());
    }

    // 3- Read one
    @GetMapping("/{id}")
    public ResponseEntity<OrganizationResponse> find(@PathVariable Long id){
        return ResponseEntity.ok(organizationService.findById(id));
    }

    // 4- Update
    @PutMapping("/{id}")
    public ResponseEntity<OrganizationResponse> update(@PathVariable Long id,
                                                       @Valid @RequestBody
                                                       OrganizationRequest request){
        return ResponseEntity.ok(organizationService.update(id, request));
    }

    // 5- Patch (for status)
    @PatchMapping("/{id}/status")
    public ResponseEntity<OrganizationResponse> changeStatus(@PathVariable Long id,
                                                             @Valid @RequestBody
                                                             OrganizationStatusRequest
                                                                     request){
        return ResponseEntity.ok(organizationService.changeStatus(id, request));
    }

    // 6 - Delete (soft delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        organizationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
