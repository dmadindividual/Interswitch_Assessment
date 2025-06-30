package topg.interswitch_userservice.user.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;


public record UserRequest(
        @NotBlank(message = "First name is required")
        String firstName,

        @NotBlank(message = "Last name is required")
        String lastName,

        @Email(message = "Invalid email format")
        @NotBlank(message = "Email is required")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters long")
        String password,

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number format")
        String phoneNumber,

        @NotBlank(message = "BVN is required")
        @Pattern(regexp = "^[0-9]{11}$", message = "BVN must be 11 digits")
        String bvn,

        @NotBlank(message = "NIN is required")
        @Pattern(regexp = "^[0-9a-zA-Z]{11}$", message = "NIN must be 11 characters")
        String nin,

        @DecimalMin(value = "0.0", inclusive = true, message = "Balance must be non-negative")
        BigDecimal balance


) {}
