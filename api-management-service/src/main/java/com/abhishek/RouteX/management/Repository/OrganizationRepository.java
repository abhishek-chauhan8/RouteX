package com.abhishek.RouteX.management.Repository;

import com.abhishek.RouteX.management.Entity.Organization;
import com.abhishek.RouteX.management.Entity.OrganizationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrganizationRepository extends JpaRepository<Organization, Long>
{
    boolean existsByNameIgnoreCase(String name);

    boolean existsBySlugIgnoreCase(String slug);

    List<Organization> findAllByStatusNot(OrganizationStatus status);

    Optional<Organization> findByIdAndStatusNot(Long id,OrganizationStatus status);

}
