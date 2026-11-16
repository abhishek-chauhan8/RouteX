package com.abhishek.RouteX.management.Service;

import com.abhishek.RouteX.management.Dto.requestDto.LoginRequest;
import com.abhishek.RouteX.management.Dto.responseDto.LoginResponse;
import com.abhishek.RouteX.management.Entity.ManagementUsers;
import com.abhishek.RouteX.management.Entity.OrganizationStatus;
import com.abhishek.RouteX.management.Exception.ApiException;
import com.abhishek.RouteX.management.Repository.ManagementUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class AuthenticationService {
    private final ManagementUserRepository managementUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final Long expirationSeconds;

    public AuthenticationService(ManagementUserRepository managementUserRepository,
                                 PasswordEncoder passwordEncoder,
                                 JwtEncoder jwtEncoder,
                                 @Value("${security.jwt.expiration-seconds}")
                                 Long expirationSeconds) {
        this.managementUserRepository = managementUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.expirationSeconds = expirationSeconds;
    }



    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest loginRequest){

        ManagementUsers user = managementUserRepository
                .findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> invalidCredentials());

        if (!user.isEnabled() || !passwordEncoder.
                matches(loginRequest.getPassword(), user.getPasswordHash())) {
            throw invalidCredentials();
        }

        if (user.getOrganization() != null &&
                user.getOrganization().getStatus() != OrganizationStatus.ACTIVE) {
            throw  new com.abhishek.RouteX.management.Exception.ApiException(
                    HttpStatus.FORBIDDEN,
                    "The organization is not active"
            );
        }

        Instant issuedAt = Instant.now();
        String role = user.getRole().getName();
        Long organizationId = user.getOrganization() == null ?
                null : user.getOrganization().getId();


//  Complete JWT generation flow :->

        // building payload or we can say JwtClaimsSet
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer("api-management-service")
                .issuedAt(issuedAt)
                .expiresAt(issuedAt.plusSeconds(expirationSeconds))
                .subject(user.getUsername())
                .claim("role", role);

        // Adding organizationId conditionally
        if(organizationId != null){
            claims.claim("organizationId", organizationId);
        }

        // Defining the signing algorithm to JwsHeader
        // JWS = JSON Web Signature
        JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();

        // Combines the header and claims for encoding. In 3 Steps :-
        // 1- claims.build() = Converts your JwtClaimsSet.Builder into an actual JwtClaimsSet object.
        // 2- JwtEncoderParameters.from = Packages two things:
        //      a- JWS Header
        //      b- JWT Claims
        // 3- .encode() = Encoder generates the signed JWT using the configured cryptographic key.
        // 4- .getTokenValue() = Extracts the final JWT string.
        String token = jwtEncoder.encode(
                JwtEncoderParameters.from(jwsHeader, claims.build())).getTokenValue();

        // Encodes and signs the JWT using the configured cryptographic key.
        // and return the LoginResponse
        return new LoginResponse(token, "Bearer",
                expirationSeconds, role, organizationId);
    }

    private RuntimeException invalidCredentials() {
        return new ApiException(
                HttpStatus.UNAUTHORIZED,
                "Invalid username or password"
        );
    }
}
