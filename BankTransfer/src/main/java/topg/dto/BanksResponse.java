package topg.dto;

import lombok.Data;
import java.util.List;

@Data
public class BanksResponse {
    private boolean status;
    private String message;
    private List<BankDto> banks;
}
