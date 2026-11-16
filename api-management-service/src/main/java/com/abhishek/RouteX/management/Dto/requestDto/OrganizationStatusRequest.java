package com.abhishek.RouteX.management.Dto.requestDto;

import com.abhishek.RouteX.management.Entity.OrganizationStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrganizationStatusRequest {
    private OrganizationStatus status;
}
