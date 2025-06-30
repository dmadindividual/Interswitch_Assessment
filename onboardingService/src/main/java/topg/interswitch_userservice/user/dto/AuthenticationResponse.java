package topg.interswitch_userservice.user.dto;

public record AuthenticationResponse(
        boolean success,
        String token) {
}
