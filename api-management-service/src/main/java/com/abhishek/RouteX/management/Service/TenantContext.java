package com.abhishek.RouteX.management.Service;

import com.abhishek.RouteX.management.Entity.Organization;
import com.abhishek.RouteX.management.Entity.OrganizationStatus;
import com.abhishek.RouteX.management.Exception.ApiException;
import com.abhishek.RouteX.management.Repository.OrganizationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class TenantContext {

    private final OrganizationRepository organizationRepository;

    public TenantContext(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    public Organization getOrganization(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(!(authentication instanceof JwtAuthenticationToken)){
            throw new ApiException(HttpStatus.FORBIDDEN, "Tenant identity is missing");
        }

        Long orgnaizationId = ((JwtAuthenticationToken) authentication).getToken().getClaim("organizationId");

        if(orgnaizationId == null){
            throw new ApiException(HttpStatus.FORBIDDEN, "Tenant identity is missing");
        }

        Organization organization = organizationRepository.findById(orgnaizationId)
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "Organization doesn't exists"));

        if(organization.getStatus() != OrganizationStatus.ACTIVE){
            throw new ApiException(HttpStatus.FORBIDDEN, "Organization is not ACTIVE");
        }

        return organization;
    }

}