package topg.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // User Service Routes
                .route("onboarding", r -> r
                        .path("/onboarding/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .addRequestHeader("X-Gateway-Request", "true")
                        )
                        .uri("http://localhost:8081")
                )

                // Billing Service Routes
                .route("billingservice", r -> r
                        .path("/billingservice/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .addRequestHeader("X-Gateway-Request", "true")
                        )
                        .uri("http://localhost:8083")
                )


                // bank transfer Service Routes
                .route("banktransfer", r -> r
                        .path("/banktransfer/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .addRequestHeader("X-Gateway-Request", "true")
                        )
                        .uri("http://localhost:8084")
                )


                .build();
    }
}
