package com.abhishek.RouteX.management.Dto.responseDto;

import com.abhishek.RouteX.management.Entity.RouteHeaderRule;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class HeaderRuleResponse {

    private Long id;
    private String direction;
    private String action;
    private String headerName;
    private String headerValue;

    public static HeaderRuleResponse from(RouteHeaderRule rule) {
        return new HeaderRuleResponse(
                rule.getId(),
                rule.getDirection(),
                rule.getAction(),
                rule.getHeaderName(),
                rule.getHeaderValue()
        );
    }
}
