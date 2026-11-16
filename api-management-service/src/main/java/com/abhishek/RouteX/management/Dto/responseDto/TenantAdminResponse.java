package com.abhishek.RouteX.management.Dto.responseDto;

import com.abhishek.RouteX.management.Entity.ManagementUsers;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TenantAdminResponse {
    private Long id;
    private String username;
    private Long organizationId;
    private boolean enabled;

    public static TenantAdminResponse from(ManagementUsers managementUsers){
        return new TenantAdminResponse(
                managementUsers.getId(),
                managementUsers.getUsername(),
                managementUsers.getOrganization().getId(),
                managementUsers.isEnabled());
    }
}
