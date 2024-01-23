package com.edu.oauth2;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.reactive.server.ReactiveWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Test configuration class responsible for defining a primary ReactiveWebServerFactory bean
 * specifically tailored for testing purposes. This configuration is intended to be used in
 * conjunction with JUnit tests to provide a consistent environment for testing reactive web servers.
 *
 * <p>The primary bean is annotated with {@code @Primary}, indicating that it should take precedence
 * when multiple beans of the same type exist in the application context.
 *
 * <p>Example usage in a JUnit test class:
 * <pre>{@code
 * @SpringBootTest
 * @ContextConfiguration(classes = ServerTestConfiguration.class)
 * public class MyServerTest {
 *
 *     @Autowired
 *     private ReactiveWebServerFactory reactiveWebServerFactory;
 *
 *     // Test methods go here
 * }
 * }</pre>
 *
 * <p>This test configuration sets up the NettyReactiveWebServerFactory as the primary
 * ReactiveWebServerFactory for testing purposes. It allows JUnit tests to use this configuration
 * to ensure consistent behavior of reactive web servers during testing.
 */
@TestConfiguration
public class ServerTestConfiguration {

    @Bean
    @Primary
    public ReactiveWebServerFactory reactiveWebServerFactory() {
        return new NettyReactiveWebServerFactory();
    }
}
