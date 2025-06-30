package topg.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder



public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String requestId;
    private String serviceID;
    private String billersCode;
    private String phone;

    private String responseCode;
    private String responseMessage;
    private String transactionStatus;
    private String productName;
    private String transactionType;
    private String vtpassTransactionId;

    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal totalAmount;
    private BigDecimal amount;

    private ZonedDateTime transactionDate;

    private String status; // success/failure
}
