package com.abhishek.RouteX.management.Dto.requestDto;

import com.abhishek.RouteX.management.Entity.OrganizationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RouteStatusRequest {
    @NotNull
    private Boolean active;
}
