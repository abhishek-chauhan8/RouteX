package com.abhishek.RouteX.management.Dto.requestDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
@Setter
public class TenantOrganizationRequest {
    @NotBlank
    @Size(max = 150)
    private String name;
}
