package com.pooja.bankingapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bankingTransactionOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Banking Transaction API")
                        .version("1.0.0")
                        .description("REST API for account management, deposits, withdrawals, transfers, balances, and transaction history."));
    }
}
