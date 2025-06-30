package topg.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import topg.config.UserClient;
import topg.dto.BankDto;
import topg.dto.BanksResponse;
import topg.dto.PaymentRequest;
import topg.dto.PaymentResponse;
import topg.model.Transaction;
import topg.repository.TransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaystackService {

    private final WebClient paystackClient;
    private final UserClient userClient;
    private final TransactionRepository txRepo;

    public Mono<BanksResponse> getAllBanks() {
        return paystackClient.get()
                .uri("/bank?country=nigeria")
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(json -> {
                    List<BankDto> banks = new ArrayList<>();
                    JsonNode data = json.get("data");

                    if (data.isArray()) {
                        for (JsonNode node : data) {
                            BankDto dto = new BankDto();
                            dto.setName(node.get("name").asText());
                            dto.setCode(node.get("code").asText());
                            dto.setType(node.get("type").asText());
                            dto.setCurrency(node.get("currency").asText());
                            dto.setCountry(node.get("country").asText());
                            dto.setSupportsTransfer(node.get("supports_transfer").asBoolean());
                            banks.add(dto);
                        }
                    }

                    BanksResponse response = new BanksResponse();
                    response.setStatus(true);
                    response.setMessage("Banks retrieved successfully");
                    response.setBanks(banks);
                    return response;
                });
    }

    public Mono<PaymentResponse> initializePayment(PaymentRequest request, String token) {
        log.info("Initializing payment for user with token: {}", token);

        return userClient.getAuthenticatedUser(token)
                .flatMap(user -> {
                    log.info("Authenticated user fetched: ID={}, balance={}, active={}",
                            user.userId(), user.balance(), user.is_active());

                    BigDecimal amountInNaira = BigDecimal.valueOf(request.getAmount())
                            .divide(BigDecimal.valueOf(100));

                    log.info("Amount to transfer (NGN): {}", amountInNaira);

                    if (user.balance().compareTo(amountInNaira) < 0) {
                        log.warn("Insufficient balance for user ID={}", user.userId());
                        return Mono.just(PaymentResponse.builder()
                                .success(false)
                                .message("Insufficient balance")
                                .build());
                    }

                    log.info("Deducting balance for user ID={}...", user.userId());
                    return userClient.deductBalance(token, amountInNaira)
                            .then(createRecipient(request))
                            .flatMap(recipientCode -> initiateTransfer(recipientCode, request)
                                    .flatMap(response -> saveTransaction(response, user.userId())));
                })
                .onErrorResume(e -> {
                    log.error("Payment initialization failed", e);
                    return Mono.just(PaymentResponse.builder()
                            .success(false)
                            .message("Payment failed: " + e.getMessage())
                            .build());
                });
    }

    private Mono<String> createRecipient(PaymentRequest request) {
        var recipientBody = Map.of(
                "type", "nuban",
                "name", request.getRecipientName(),
                "account_number", request.getAccountNumber(),
                "bank_code", request.getBankCode(),
                "currency", "NGN"
        );

        log.info("Creating Paystack recipient with: {}", recipientBody);

        return paystackClient.post()
                .uri("/transferrecipient")
                .bodyValue(recipientBody)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    log.error("Paystack error response: {}", errorBody);
                                    return Mono.error(new RuntimeException("Paystack rejected recipient: " + errorBody));
                                }))
                .bodyToMono(JsonNode.class)
                .doOnNext(json -> log.info("Recipient created. Response: {}", json))
                .map(json -> json.get("data").get("recipient_code").asText());
    }

    private final boolean devMode = true; // toggle this to false in production

    private Mono<JsonNode> initiateTransfer(String recipientCode, PaymentRequest request) {
        var transferBody = Map.of(
                "source", "balance",
                "amount", request.getAmount(),
                "recipient", recipientCode,
                "reason", request.getReason(),
                "currency", "NGN"
        );

        log.info("Initiating transfer to recipientCode={} for amount={}...", recipientCode, request.getAmount());

        if (devMode) {
            log.warn("MOCK MODE ENABLED: Simulating transfer without calling Paystack.");

            String mockJson = """
                    {
                      "status": true,
                      "message": "Transfer queued successfully (mock)",
                      "data": {
                        "amount": %d,
                        "currency": "NGN",
                        "status": "success",
                        "reference": "mock_ref_%d",
                        "transfer_code": "mock_tx_code_%d",
                        "reason": "%s",
                        "recipient": "%s"
                      }
                    }
                    """.formatted(
                    request.getAmount(),
                    System.currentTimeMillis(),
                    System.currentTimeMillis(),
                    request.getReason(),
                    recipientCode
            );

            try {
                ObjectMapper mapper = new ObjectMapper();
                return Mono.just(mapper.readTree(mockJson));
            } catch (Exception e) {
                return Mono.error(new RuntimeException("Failed to parse mock JSON"));
            }
        }

        return paystackClient.post()
                .uri("/transfer")
                .bodyValue(transferBody)
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class).flatMap(body -> {
                            log.error("Transfer failed. Status: {}, Body: {}", response.statusCode(), body);
                            return Mono.error(new RuntimeException("Paystack transfer failed: " + body));
                        })
                )
                .bodyToMono(JsonNode.class)
                .doOnNext(json -> log.info("Transfer successful. Response: {}", json));
    }

    private Mono<PaymentResponse> saveTransaction(JsonNode response, Long userId) {
        JsonNode data = response.get("data");

        log.info("Saving transaction for user ID={}, reference={}",
                userId, data.get("reference").asText());

        Transaction tx = Transaction.builder()
                .userId(userId)
                .amount(data.get("amount").asLong())
                .status(data.get("status").asText())
                .reason(data.get("reason").asText())
                .transferCode(data.get("transfer_code").asText())
                .reference(data.get("reference").asText())
                .recipientCode(data.get("recipient").asText())
                .currency("NGN")
                .createdAt(LocalDateTime.now())
                .build();

        txRepo.save(tx);

        return Mono.just(PaymentResponse.builder()
                .success(true)
                .message("Transfer successful")
                .reference(tx.getReference())
                .build());
    }
}
