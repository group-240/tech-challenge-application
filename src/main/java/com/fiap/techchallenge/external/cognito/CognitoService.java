package com.fiap.techchallenge.external.cognito;

import com.fiap.techchallenge.infrastructure.logging.LogCategory;
import com.fiap.techchallenge.infrastructure.logging.StructuredLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

@Service
public class CognitoService {

    private static final Logger logger = LoggerFactory.getLogger(CognitoService.class);
    private final CognitoIdentityProviderClient cognitoClient;
    
    @Value("${COGNITO_USER_POOL_ID:}")
    private String userPoolId;

    public CognitoService(CognitoIdentityProviderClient cognitoClient) {
        this.cognitoClient = cognitoClient;
    }

    public void createUser(String cpf, String email, String name) {
        long startTime = System.currentTimeMillis();
        
        try {
            StructuredLogger.setCategory(LogCategory.INTEGRATION);
            StructuredLogger.setOperation("CreateCognitoUser");
            StructuredLogger.put("cpf", cpf);
            StructuredLogger.put("email", email);
            
            logger.info("Cognito user creation started: cpf={}, email={}", cpf, email);
            
            AdminCreateUserRequest createUserRequest = AdminCreateUserRequest.builder()
                    .userPoolId(userPoolId)
                    .username(cpf)
                    .userAttributes(
                            AttributeType.builder()
                                    .name("email")
                                    .value(email)
                                    .build(),
                            AttributeType.builder()
                                    .name("name")
                                    .value(name)
                                    .build(),
                            AttributeType.builder()
                                    .name("custom:cpf")
                                    .value(cpf)
                                    .build()
                    )
                    .temporaryPassword("TempPassword123!")
                    .messageAction(MessageActionType.SUPPRESS) // Não envia email de boas-vindas
                    .build();

            cognitoClient.adminCreateUser(createUserRequest);

            // Define senha permanente
            AdminSetUserPasswordRequest setPasswordRequest = AdminSetUserPasswordRequest.builder()
                    .userPoolId(userPoolId)
                    .username(cpf)
                    .password("TempPassword123!")
                    .permanent(true)
                    .build();

            cognitoClient.adminSetUserPassword(setPasswordRequest);
            
            long duration = System.currentTimeMillis() - startTime;
            StructuredLogger.setDuration(duration);
            
            logger.info("Cognito user created successfully: cpf={}, duration={}ms", cpf, duration);

        } catch (UsernameExistsException e) {
            logger.warn("Cognito user already exists: cpf={}", cpf);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            StructuredLogger.setDuration(duration);
            StructuredLogger.setError("COGNITO_USER_CREATION_FAILED", e.getMessage());
            logger.error("Failed to create Cognito user: cpf={}, duration={}ms", cpf, duration, e);
            throw new RuntimeException("Erro ao criar usuário no Cognito: " + e.getMessage(), e);
        } finally {
            StructuredLogger.clear();
        }
    }
}