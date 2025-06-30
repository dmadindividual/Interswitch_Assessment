package topg.config;

import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "vtpass")
public class VtpassConfig {
    private String baseUrl;
    private String apiKey;
    private String publicKey;
    private String secretKey;


}
