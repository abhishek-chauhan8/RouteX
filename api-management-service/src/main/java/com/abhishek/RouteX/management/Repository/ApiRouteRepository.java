package com.abhishek.RouteX.management.Repository;

import com.abhishek.RouteX.management.Entity.ApiRoute;
import com.abhishek.RouteX.management.Entity.OrganizationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApiRouteRepository extends JpaRepository<ApiRoute, Long> {
    boolean existsByOrganizationIdAndRouteId(Long id, @NotBlank @Size(max = 100) String routeId);

    List<ApiRoute> findAllByOrganizationId(Long id);

    Optional<ApiRoute> findByIdAndOrganizationId(Long id, Long organizationId);

    List<ApiRoute> findAllByActiveTrueAndOrganizationStatus(OrganizationStatus organizationStatus);
}
