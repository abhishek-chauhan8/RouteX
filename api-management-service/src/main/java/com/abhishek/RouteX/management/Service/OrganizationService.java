package com.abhishek.RouteX.management.Service;

import com.abhishek.RouteX.management.Dto.requestDto.OrganizationRequest;
import com.abhishek.RouteX.management.Dto.requestDto.OrganizationStatusRequest;
import com.abhishek.RouteX.management.Dto.responseDto.OrganizationResponse;
import com.abhishek.RouteX.management.Entity.Organization;
import com.abhishek.RouteX.management.Entity.OrganizationStatus;
import com.abhishek.RouteX.management.Exception.ApiException;
import com.abhishek.RouteX.management.Repository.OrganizationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrganizationService {
    private final OrganizationRepository organizationRepository;

    public OrganizationService(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    @Transactional
    public OrganizationResponse  create(OrganizationRequest request){
        long startedAt = System.nanoTime();

        checkUnique(request.getName(), request.getSlug(), null);

        Organization organization = new Organization();
        organization.setName(request.getName());
        organization.setSlug(request.getSlug());
        organization.setStatus(OrganizationStatus.PENDING);
        organization.setCreatedAt(LocalDateTime.now());
        organization.setUpdatedAt(LocalDateTime.now());

        organizationRepository.save(organization);

        // This is a AuditService layer which audit/store every action performed
//        auditService.record(organization, "CREATE_ORGANIZATION", "ORGANIZATION",
//                organization.getId().toString(), startedAt,
//                "Organization creation is in PENDING state");

        return OrganizationResponse.from(organization);
    }

    @Transactional(readOnly = true)
    public List<OrganizationResponse> findAll(){
        List<OrganizationResponse> result = new ArrayList<>();

        for(Organization organization : organizationRepository.
                findAllByStatusNot(OrganizationStatus.DELETED)){
            result.add(OrganizationResponse.from(organization));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public OrganizationResponse findById(Long id){
        return OrganizationResponse.from(getOrganization(id));
    }

    @Transactional
    public OrganizationResponse update(Long id, OrganizationRequest request){
        long startedAt = System.nanoTime();

        Organization organization = getOrganization(id);
        checkUnique(request.getName(), request.getSlug(), id);
        organization.setName(request.getName());
        organization.setSlug(request.getSlug());
        organization.setUpdatedAt(LocalDateTime.now());

        return OrganizationResponse.from(organization);
    }

    @Transactional
    public OrganizationResponse changeStatus(Long id,
                                             OrganizationStatusRequest request) {

        long startedAt = System.nanoTime();

        Organization organization = getOrganization(id);

        if (request.getStatus() == OrganizationStatus.DELETED) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Use DELETE to soft-delete an organization"
            );
        }

        organization.setStatus(request.getStatus());
        organization.setUpdatedAt(LocalDateTime.now());

        return OrganizationResponse.from(organization);
    }

    @Transactional
    public void delete(Long id){
        long startedAt = System.nanoTime();

        Organization organization = getOrganization(id);
        organization.setStatus(OrganizationStatus.DELETED);
        organization.setDeletedAt(LocalDateTime.now());
        organization.setUpdatedAt(LocalDateTime.now());
    }


    // Supportive functions

    private Organization getOrganization(Long id){
        return organizationRepository.findByIdAndStatusNot(id, OrganizationStatus.DELETED)
                .orElseThrow(() ->
                        new ApiException(
                                HttpStatus.NOT_FOUND,
                                "Organization not found"));
    }

    private void checkUnique(String name, String slug, Long currentId){
        if(organizationRepository.existsByNameIgnoreCase(name)){
            Organization current = currentId == null ?
                    null : organizationRepository.findById(currentId).orElse(null);

            if(current == null || !current.getName().equalsIgnoreCase(name)){
                throw new ApiException(
                        HttpStatus.CONFLICT,
                        "Organization name already exists");
            }
        }

        if(organizationRepository.existsBySlugIgnoreCase(slug)){
            Organization current = currentId == null ?
                    null : organizationRepository.findById(currentId).orElse(null);

            if(current == null || !current.getSlug().equalsIgnoreCase(slug)){
                throw new ApiException(
                        HttpStatus.CONFLICT,
                        "Organization slug name already exists");
            }
        }


    }
}
