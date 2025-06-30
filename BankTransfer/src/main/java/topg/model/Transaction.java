package topg.model;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String recipientCode;

    private Long amount;

    private String reason;

    private String status;

    private String transferCode;

    private String reference;

    private String currency;

    private LocalDateTime createdAt;
}
