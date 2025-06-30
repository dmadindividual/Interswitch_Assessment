package topg.interswitch_userservice.user.dto;

public record UserResponseDto(
        boolean success,
        UserDto userDto
) {
}
