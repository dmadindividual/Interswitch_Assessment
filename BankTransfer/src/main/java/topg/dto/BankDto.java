package topg.dto;

import lombok.Data;

@Data
public class BankDto {
    private String name;
    private String code;
    private String type;
    private String currency;
    private String country;
    private boolean supportsTransfer;
}
