package com.abhishek.RouteX.management.Entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "api_routes")
@NoArgsConstructor
public class ApiRoute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    private String routeId;
    private String name;
    private String pathPattern;
    private String targetBaseUrl;
    private String targetPathPrefix;
    private Integer requestsPerMinute;
    private Integer responseTimeoutMs;
    private boolean idempotencyEnabled;
    private boolean active;
    private Long configurationVersion;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


//- @ElementCollection: Hibernate ko batata hai ki ye basic values ki collection hai, separate entity nahi.
//- fetch = FetchType.EAGER: Parent entity load hote hi allowed methods bhi load ho jayenge.
//- @CollectionTable: Collection store karne ke liye table ka naam define karta hai.
//- @JoinColumn(name = "route_pk"): Collection table mein parent route ko identify karne ke liye foreign key column banata hai.
//- @Column(name = "http_method"): Collection ki individual value kis column mein store hogi, ye batata hai.
//  Important: allowedMethods ka apna separate entity ID nahi hota. Ye values parent entity ke saath associated hoti hain.
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "route_allowed_methods",
            joinColumns = @JoinColumn(name = "route_pk")
    )
    @Column(name = "http_method")
    private Set<String> allowedMethods = new HashSet<>();

    @OneToMany(
            mappedBy = "route",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<RouteHeaderRule> headerRules = new ArrayList<>();
}
