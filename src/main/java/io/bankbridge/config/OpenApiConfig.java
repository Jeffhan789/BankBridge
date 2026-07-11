package io.bankbridge.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    OpenAPI bankBridgeOpenApi() {
        return new OpenAPI().info(new Info()
                .title("BankBridge API")
                .version("0.1.0")
                .description("Independent educational API for synthetic cross-border payment processing."));
    }
}
