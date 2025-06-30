package topg.dto;

public record UserResponseDto(
        boolean success,
        UserDto userDto
) {}
