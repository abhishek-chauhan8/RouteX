package com.abhishek.RouteX.smart_gateway_service.Services;

import com.abhishek.RouteX.smart_gateway_service.Dto1.CompiledRoute;
import com.abhishek.RouteX.smart_gateway_service.Dto1.GatewayRoute;
import org.springframework.http.server.PathContainer;
import org.springframework.stereotype.Service;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import javax.xml.xpath.XPath;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class RouteRegistry {

    private final PathPatternParser parser = new PathPatternParser();

// AtomicReference provides thread-safe access and atomic replacement of the route list.
// New routes are prepared separately and then replaced in a single operation.
// Concurrent request threads see either the old routes or the new routes,
// preventing them from accessing a partially updated routing configuration.
// This allows the gateway to refresh routes without stopping incoming traffic.

// Stores the current gateway routes in JVM memory (RAM) for fast access.
// Routes are lost when the application stops or restarts.
// Provides atomic, thread-safe replacement of the route reference during configuration refresh.
    private final AtomicReference<List<CompiledRoute>> routes =
            new AtomicReference(Collections.emptyList());

    public void replaceAll(List<GatewayRoute> gatewayRoutes){
        List<CompiledRoute> compiledRoutes = new ArrayList<>();

        if(gatewayRoutes != null){
            for(GatewayRoute route : gatewayRoutes){
                PathPattern pattern = parser.parse(route.getPathPattern());

                compiledRoutes.add(new CompiledRoute(route, pattern,
                        sourcePrefix(route.getPathPattern())));
            }
        }

        Collections.sort(compiledRoutes, new Comparator<CompiledRoute>() {
            @Override
            public int compare(CompiledRoute o1, CompiledRoute o2) {
                return PathPattern.SPECIFICITY_COMPARATOR.compare(
                        o1.getPathPattern(), o2.getPathPattern()
                );
            }
        });
    }

    public Optional<CompiledRoute> find(String requestPath){
        PathContainer pathContainer = PathContainer.parsePath(requestPath);

        for(CompiledRoute route : routes.get()){
            if(route.getPathPattern().matches(pathContainer)){
                return Optional.of(route);
            }
        }

        return Optional.empty();
    }

    private String sourcePrefix(String pathPattern){
        if(pathPattern.endsWith("/**")){
            return pathPattern.substring(0, pathPattern.length() - 3);
        }

        return pathPattern;
    }
}
