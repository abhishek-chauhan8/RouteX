package com.abhishek.RouteX.management.Service;

import com.abhishek.RouteX.management.Dto.requestDto.OrganizationRequest;
import com.abhishek.RouteX.management.Dto.requestDto.OrganizationStatusRequest;
import com.abhishek.RouteX.management.Dto.requestDto.TenantAdminRequest;
import com.abhishek.RouteX.management.Dto.requestDto.TenantOrganizationRequest;
import com.abhishek.RouteX.management.Dto.responseDto.OrganizationResponse;
import com.abhishek.RouteX.management.Dto.responseDto.TenantAdminResponse;
import com.abhishek.RouteX.management.Entity.ManagementUsers;
import com.abhishek.RouteX.management.Entity.Organization;
import com.abhishek.RouteX.management.Entity.OrganizationStatus;
import com.abhishek.RouteX.management.Entity.Role;
import com.abhishek.RouteX.management.Exception.ApiException;
import com.abhishek.RouteX.management.Repository.ManagementUserRepository;
import com.abhishek.RouteX.management.Repository.OrganizationRepository;
import com.abhishek.RouteX.management.Repository.RoleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrganizationService {
    private final OrganizationRepository organizationRepository;
    private final ManagementUserRepository managementUserRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final TenantContext tenantContext;
    private final AuditService auditService;

    public OrganizationService(OrganizationRepository organizationRepository,
                               ManagementUserRepository managementUserRepository,
                               RoleRepository roleRepository,
                               PasswordEncoder passwordEncoder,
                               TenantContext tenantContext,
                               AuditService auditService) {
        this.organizationRepository = organizationRepository;
        this.managementUserRepository = managementUserRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.tenantContext = tenantContext;
        this.auditService =  auditService;
    }

    // <--- Methods for PlatformOrganizationController --->

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

        auditService.record(organization, "CREATE_ORGANIZATION", "ORGANIZATION",
                organization.getId().toString(), startedAt,
                "Organization creation is in PENDING state");

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

        auditService.record(organization, "UPDATE_ORGANIZATION", "ORGANIZATION",
                organization.getId().toString(), startedAt,
                "Organization name and slug updated");

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

        auditService.record(organization, "CHANGE_ORGANIZATION_STATUS", "ORGANIZATION",
                organization.getId().toString(), startedAt,
                "STATUS changed to : " + request.getStatus());

        return OrganizationResponse.from(organization);
    }

    @Transactional
    public void delete(Long id){
        long startedAt = System.nanoTime();

        Organization organization = getOrganization(id);
        organization.setStatus(OrganizationStatus.DELETED);
        organization.setDeletedAt(LocalDateTime.now());
        organization.setUpdatedAt(LocalDateTime.now());

        auditService.record(organization, "DELETE_ORGANIZATION", "ORGANIZATION",
                organization.getId().toString(), startedAt,
                "Organization soft-deleted");
    }


    // <--- Tenant Admin CRUD operations --->

    @Transactional
    public TenantAdminResponse createTenantAdmin(Long organizationId,
                                                 TenantAdminRequest request){
        long startedAt = System.nanoTime();

        Organization organization = getOrganization(organizationId);

        if(managementUserRepository.findByOrganizationId(organizationId).isPresent()){
            throw new ApiException(HttpStatus.CONFLICT,
                    "The organization is already has a Tenant administrator");
        }

        if(managementUserRepository.existsByUsername(request.getUsername())){
            throw new ApiException(HttpStatus.CONFLICT,
                    "The username is already exists");
        }

        Role role = roleRepository.findByName("TENANT_ADMIN")
                .orElseThrow( () ->
                        new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                                "TENANT_ADMIN role is missing"));

        ManagementUsers user = new ManagementUsers();

        user.setOrganization(organization);
        user.setRole(role);
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(request.isEnabled());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        managementUserRepository.save(user);
        return TenantAdminResponse.from(user);
    }

    @Transactional
    public TenantAdminResponse getTenantAdmin(Long organizationId){
        return TenantAdminResponse.from(getTenantAdminEntity(organizationId));
    }

    @Transactional
    public TenantAdminResponse updateTenantAdmin(Long organizationId,
                                                 TenantAdminRequest request){
        long startedAt = System.nanoTime();

        ManagementUsers users = getTenantAdminEntity(organizationId);

        if (!users.getUsername().equals(request.getUsername())
                && managementUserRepository.existsByUsername(request.getUsername())) {

            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Username already exists"
            );
        }

        users.setUsername(request.getUsername());
        users.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        users.setEnabled(request.isEnabled());
        users.setUpdatedAt(LocalDateTime.now());

        return TenantAdminResponse.from(users);
    }

    @Transactional
    public void deleteTenantAdmin(Long organizationId){
        long startedAt = System.nanoTime();

        ManagementUsers users = getTenantAdminEntity(organizationId);

        managementUserRepository.delete(users);
    }

    // <--- Methods for PlatformOrganizationController --->

    @Transactional(readOnly = true)
    public OrganizationResponse getOwnOrganization(){
        return OrganizationResponse.from(tenantContext.getOrganization());
    }

    @Transactional
    public OrganizationResponse updateOwnOrganization(TenantOrganizationRequest request){
        long startedAt = System.nanoTime();

        Organization organization = tenantContext.getOrganization();

        // Allow name changes only if the new name is not already used by another organization.
        if(!organization.getName().equalsIgnoreCase(request.getName())
                && organizationRepository.existsByNameIgnoreCase(request.getName())) {

            throw new ApiException(HttpStatus.CONFLICT, "Tenant name is already exists");
        }

        organization.setName(request.getName());
        organization.setUpdatedAt(LocalDateTime.now());

        return OrganizationResponse.from(organization);
    }



    // <--- Supportive functions --->

    public ManagementUsers getTenantAdminEntity(Long organizationId) {

        getOrganization(organizationId);

        return managementUserRepository.findByOrganizationId(organizationId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Tenant administrator not found"
                ));
    }

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
