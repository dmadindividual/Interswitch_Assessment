package topg.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import topg.config.UserClient;
import topg.config.VtpassConfig;
import topg.dto.PaymentRequest;
import topg.dto.PaymentResponse;
import topg.dto.UnifiedPaymentResponse;
import topg.model.PaymentTransaction;
import topg.repository.PaymentRepository;
import topg.utils.VtpassUtil;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final UserClient userClient;
    private final VtpassConfig config;
    private final PaymentRepository transactionRepository;

    public Mono<UnifiedPaymentResponse> makePayment(PaymentRequest request, String token) {
        BigDecimal amount = new BigDecimal(request.amount());

        return userClient.getAuthenticatedUser(token)
                .flatMap(user -> {
                    BigDecimal currentBalance = user.balance() != null ? user.balance() : BigDecimal.ZERO;
                    if (currentBalance.compareTo(amount) < 0) {
                        return Mono.error(new RuntimeException("Insufficient balance"));
                    }

                    return userClient.deductBalance(token, amount)
                            .then(callVtpassAndSaveTransaction(request, amount));
                })
                .map(data -> new UnifiedPaymentResponse(true, "Payment successful", data))
                .onErrorResume(e -> Mono.just(new UnifiedPaymentResponse(false, e.getMessage(), null)));
    }

    private Mono<PaymentResponse> callVtpassAndSaveTransaction(PaymentRequest request, BigDecimal amount) {
        String requestId = VtpassUtil.generateRequestId();

        String hash = DigestUtils.sha256Hex(
                config.getPublicKey() + config.getApiKey() + requestId + request.serviceID()
        );

        Map<String, Object> payload = Map.of(
                "request_id", requestId,
                "serviceID", request.serviceID(),
                "amount", request.amount(),
                "phone", request.phone()
        );

        return WebClient.create()
                .post()
                .uri(config.getBaseUrl() + "/pay")
                .header("api-key", config.getApiKey())
                .header("secret-key", config.getSecretKey())
                .header("hash", hash)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(PaymentResponse.class)
                .doOnNext(resp -> {
                    Object txObject = resp.content().get("transactions");

                    if (txObject instanceof Map<?, ?> txInfoRaw) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> txInfo = (Map<String, Object>) txInfoRaw;

                        PaymentTransaction tx = PaymentTransaction.builder()
                                .userId(request.userId())
                                .requestId(resp.requestId())
                                .serviceID(request.serviceID())
                                .billersCode(request.billersCode())
                                .phone(request.phone())
                                .responseCode(resp.code())
                                .responseMessage(resp.response_description())
                                .transactionStatus((String) txInfo.get("status"))
                                .productName((String) txInfo.get("product_name"))
                                .transactionType((String) txInfo.get("type"))
                                .vtpassTransactionId((String) txInfo.get("transactionId"))
                                .unitPrice(new BigDecimal(txInfo.get("unit_price").toString()))
                                .quantity((Integer) txInfo.get("quantity"))
                                .totalAmount(new BigDecimal(txInfo.get("total_amount").toString()))
                                .amount(new BigDecimal(resp.amount()))
                                .transactionDate(ZonedDateTime.parse(resp.transaction_date()))
                                .status("success")
                                .build();

                        transactionRepository.save(tx);
                    } else {
                        System.err.println("Unexpected transaction response format: " + txObject);
                    }
                });
    }
}
