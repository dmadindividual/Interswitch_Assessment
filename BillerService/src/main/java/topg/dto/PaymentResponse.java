package topg.dto;

import java.util.Map;

public record PaymentResponse(
        String code,
        String response_description,
        String requestId,
        String amount,
        String purchased_code,
        String transaction_date,
        Map<String, Object> content
) {}
