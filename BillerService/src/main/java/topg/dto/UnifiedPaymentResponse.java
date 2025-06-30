package topg.dto;

public record UnifiedPaymentResponse(
        boolean success,
        String message,
        PaymentResponse data
) {}
