package com.edu.oauth2;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("integrationtest")
@Import(Oauth2SpringSecurityIntegrationTestExampleApplicationTests.class)
class Oauth2SpringSecurityIntegrationTestExampleApplicationTests {
}
