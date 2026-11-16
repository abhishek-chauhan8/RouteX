package com.abhishek.RouteX.management.Repository;

import com.abhishek.RouteX.management.Entity.ManagementUsers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ManagementUserRepository
        extends JpaRepository<ManagementUsers,Long> {
    Optional<ManagementUsers> findByUsername(String username);
}
