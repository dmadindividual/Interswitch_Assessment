package topg.interswitch_userservice.user.dto;

import java.math.BigDecimal;
import java.util.Date;

public record UserDto(
        Long userId,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        String bvn,
        String nin,
        BigDecimal balance,
        Date createdAt,
        Date updatedAt,
        boolean is_active
) {
}
