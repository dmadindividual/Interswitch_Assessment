package topg.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import topg.dto.BanksResponse;
import topg.dto.PaymentRequest;
import topg.dto.PaymentResponse;
import topg.service.PaystackService;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/paystack")
public class PaystackController {
    private final PaystackService paystackService;

    @GetMapping("/banks")
    public Mono<ResponseEntity<BanksResponse>> getBanks() {
        return paystackService.getAllBanks()
                .map(ResponseEntity::ok);
    }

    @PostMapping("/pay")
    public Mono<ResponseEntity<PaymentResponse>> makePayment(
            @RequestBody PaymentRequest request,
            @RequestHeader("Authorization") String authHeader
    ) {
        String token = authHeader.replace("Bearer ", "");
        return paystackService.initializePayment(request, token)
                .map(ResponseEntity::ok);
    }


}

