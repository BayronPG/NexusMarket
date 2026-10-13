package com.nexusmarket;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Verifies that the Spring context of the application loads correctly.
 *
 * <p>Traceability: evidencias 1 and 2 (executable Spring Boot project with
 * the domain and its application services wired through their ports).</p>
 *
 * <p>Runs under the "test" profile: the context loads against the embedded H2
 * database instead of the real MySQL datasource (Fase 1.4 of docs/PLAN_API_WEB.md).
 * H2 is only a test tool; it does not replace MySQL.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
class NexusMarketApplicationTests {

    @Test
    void contextLoads() {
    }
}
