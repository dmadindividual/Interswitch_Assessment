package topg.config;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import topg.dto.UserDto;
import topg.dto.UserResponseDto;

import java.math.BigDecimal;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserClient {

    private final WebClient.Builder webClientBuilder;

    public Mono<UserDto> getAuthenticatedUser(String token) {
        return webClientBuilder.build()
                .get()
                .uri("http://localhost:8081/api/v1/user/me")
                .headers(headers -> headers.setBearerAuth(token))
                .retrieve()
                .bodyToMono(UserResponseDto.class)
                .map(UserResponseDto::userDto);  // <- extract the nested userDto
    }


    public Mono<Void> deductBalance(String token, BigDecimal amount) {
        return webClientBuilder.build()
                .post()
                .uri("http://localhost:8081/api/v1/user/me/deduct")
                .headers(headers -> headers.setBearerAuth(token))
                .bodyValue(Map.of("amount", amount))
                .retrieve()
                .bodyToMono(Void.class);
    }
}
