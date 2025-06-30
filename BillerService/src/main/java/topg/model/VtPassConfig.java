package topg.model;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "vtpass")
@Data
public class VtPassConfig {
    private String baseUrl;
    private String username;
    private String password;
    private String apiKey;
}
