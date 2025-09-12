package com.fiap.techchallenge.external.cognito;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

@Service
public class CognitoService {

    private final CognitoIdentityProviderClient cognitoClient;
    
    @Value("${COGNITO_USER_POOL_ID:}")
    private String userPoolId;

    public CognitoService(CognitoIdentityProviderClient cognitoClient) {
        this.cognitoClient = cognitoClient;
    }

    public void createUser(String cpf, String email, String name) {
        try {
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

        } catch (UsernameExistsException e) {
            // Usuário já existe no Cognito, não faz nada
        } catch (Exception e) {
            throw new RuntimeException("Erro ao criar usuário no Cognito: " + e.getMessage(), e);
        }
    }
}