package topg.dto;

import lombok.Data;

@Data
public class PaymentRequest {
    private Long userId;
    private Long amount; // In kobo
    private String reason;
    private String accountNumber;
    private String bankCode;
    private String recipientName;
}

