package topg.dto;

import java.math.BigDecimal;

public record UserDto(
        Long userId,
        String firstName,
        String lastName, // <== Add this to match actual response
        String email,
        String phoneNumber,
        String bvn,
        String nin,
        BigDecimal balance,
        boolean is_active
) {}
