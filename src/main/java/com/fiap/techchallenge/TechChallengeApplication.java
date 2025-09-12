package com.fiap.techchallenge;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Classe principal da aplicação Tech Challenge
 * <p>
 * Esta é a classe de inicialização da aplicação Spring Boot que implementa
 * um sistema de serviço de alimentação utilizando clean architecture.
  * A documentação da API está disponível através do Swagger UI em:
 * <code>http://localhost:8080/api/swagger-ui.html</code>
 * </p>
 */
@SpringBootApplication
@OpenAPIDefinition(
    info = @Info(
        title = "Tech Challenge - Food Service API",
        version = "1.0.0",
        description = "API para sistema de serviço de alimentação desenvolvida com clean architecture"
    )
)
public class TechChallengeApplication {

    /**
     * Método principal que inicia a aplicação Spring Boot
     *
     * @param args Argumentos de linha de comando
     */
    public static void main(String[] args) {
        SpringApplication.run(TechChallengeApplication.class, args);
    }
}
