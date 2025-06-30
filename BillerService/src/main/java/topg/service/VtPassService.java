package topg.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import topg.dto.FilteredServiceResponse;
import topg.dto.ServiceResponse;
import topg.dto.VariationResponse;
import topg.model.VtPassConfig;

@Service
@RequiredArgsConstructor
public class VtPassService {

    private final WebClient.Builder webClientBuilder;
    private final VtPassConfig config;

    public Mono<ServiceResponse> getAllBillers() {
        return webClientBuilder.build()
                .get()
                .uri(config.getBaseUrl() + "/service-categories")
                .retrieve()
                .bodyToMono(ServiceResponse.class);
    }

    public Mono<VariationResponse> getServiceVariations(String serviceID) {
        return webClientBuilder.build()
                .get()
                .uri(config.getBaseUrl() + "/service-variations?serviceID=" + serviceID)
                .retrieve()
                .bodyToMono(VariationResponse.class);
    }

    public Mono<FilteredServiceResponse> getServicesByIdentifier(String identifier) {
        return webClientBuilder.build()
                .get()
                .uri(config.getBaseUrl() + "/services?identifier=" + identifier)
                .retrieve()
                .bodyToMono(FilteredServiceResponse.class);
    }


}
