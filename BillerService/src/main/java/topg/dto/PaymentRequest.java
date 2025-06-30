package topg.dto;

public record PaymentRequest(
        String serviceID,
        String billersCode,
        String amount,
        String phone,
        Long userId
) {}
