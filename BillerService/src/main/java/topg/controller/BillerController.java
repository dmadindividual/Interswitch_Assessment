package topg.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import topg.dto.*;
import topg.service.PaymentService;
import topg.service.VtPassService;

@RestController
@RequestMapping("/api/v1/billers")
@RequiredArgsConstructor
public class BillerController {
private final PaymentService paymentService;
    private final VtPassService vtpassService;

    @GetMapping
    public Mono<ServiceResponse> getAllBillers() {
        return vtpassService.getAllBillers();
    }

    @GetMapping("/{serviceID}/variations")
    public Mono<VariationResponse> getServiceVariations(@PathVariable String serviceID) {
        return vtpassService.getServiceVariations(serviceID);
    }

    @GetMapping("/category/{identifier}")
    public Mono<FilteredServiceResponse> getByCategory(@PathVariable String identifier) {
        return vtpassService.getServicesByIdentifier(identifier);
    }

    @PostMapping("/pay")
    public Mono<ResponseEntity<UnifiedPaymentResponse>> makePayment(
            @RequestBody PaymentRequest request,
            @RequestHeader("Authorization") String authHeader
    ) {
        String token = authHeader.replace("Bearer ", "");

        return paymentService.makePayment(request, token)
                .map(ResponseEntity::ok);
    }



}
