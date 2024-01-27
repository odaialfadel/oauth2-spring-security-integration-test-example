package com.edu.oauth2;

import com.github.tomakehurst.wiremock.http.Body;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.Collections;
import java.util.Date;
import java.util.List;


import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integrationtest")
public class OAuth2ServerSecurityIntegrationTest {

    @RegisterExtension
    static final WireMockExtension WIRE_MOCK_SERVER = WireMockExtension.newInstance().build();

    @RegisterExtension
    static final JwtGeneratorExtension JWT_GENERATOR = new JwtGeneratorExtension("AUDIENCE", "ISSUER");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri", WIRE_MOCK_SERVER::baseUrl);
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", JWT_GENERATOR::issuer);
        registry.add("spring.security.oauth2.resourceserver.jwt.audiences", JWT_GENERATOR::audience);
    }

    @LocalServerPort
    private int port;

    private String endpointUrl;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    void beforeEach() {
        WIRE_MOCK_SERVER.stubFor(
                get("/").willReturn(
                        aResponse()
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withResponseBody(new Body(JWT_GENERATOR.jwks()))));

        endpointUrl = "http://localhost:" + port + "/api/example";
    }

    @Test
    void testSecuredEndpointWithValidToken() throws Exception {
        // Arrange
        String validToken = JWT_GENERATOR.token("VALID_TOKEN");
        restTemplate.getRestTemplate().setInterceptors(getInterceptors(validToken));

        // Process
        ResponseEntity<String> response = restTemplate.getForEntity(endpointUrl, String.class);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testSecuredEndpointWithInvalidToken() {
        // Arrange
        String INVALID_TOKEN = "INVALID_TOKEN";
        restTemplate.getRestTemplate().setInterceptors(getInterceptors(INVALID_TOKEN));

        // Process
        ResponseEntity<String> response = restTemplate.getForEntity(endpointUrl, String.class);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testSecuredEndpointWithExpiredToken() throws Exception {
        // Arrange
        String expiredToken = JWT_GENERATOR.token("EXPIRED_TOKEN", new Date(new Date().getTime() - 60 * 1000));
        restTemplate.getRestTemplate().setInterceptors(getInterceptors(expiredToken));

        // Process
        // Make the request to the secured endpoint
        ResponseEntity<String> response = restTemplate.getForEntity(endpointUrl, String.class);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testSecuredEndpointWithoutToken() {
        // Arrange
        restTemplate.getRestTemplate().setInterceptors(Collections.emptyList());

        // Process
        ResponseEntity<String> response = restTemplate.getForEntity(endpointUrl, String.class);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testSecuredEndpointWithTamperedToken() throws Exception {
        // Arrange
        String tamperedToken = JWT_GENERATOR.token("TAMPERED_TOKEN");
        if (!tamperedToken.isBlank())
            tamperedToken = tamperedToken.replace(tamperedToken.charAt(0), 'A');

        restTemplate.getRestTemplate().setInterceptors(getInterceptors(tamperedToken));

        // Process
        ResponseEntity<String> response = restTemplate.getForEntity(endpointUrl, String.class);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }


    private List<ClientHttpRequestInterceptor> getInterceptors(String token) {
        return Collections.singletonList((request, body, execution) -> {
            request.getHeaders().add(HttpHeaders.AUTHORIZATION, "Bearer " + token);
            return execution.execute(request, body);
        });
    }
}
