package com.example.ledgerly;

import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.*;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractIntegrationTest {

    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:15")
                    .withDatabaseName("ledgerly")
                    .withUsername("ledgerly")
                    .withPassword("ledgerly");

    static final KafkaContainer KAFKA =
            new KafkaContainer(org.testcontainers.utility.DockerImageName.parse("confluentinc/cp-kafka:7.5.3"));

    static final GenericContainer<?> REDIS =
            new GenericContainer<>(org.testcontainers.utility.DockerImageName.parse("redis:7"))
                    .withExposedPorts(6379);

    static final MongoDBContainer MONGO =
            new MongoDBContainer(org.testcontainers.utility.DockerImageName.parse("mongo:6"));

    static {
        POSTGRES.start();
        KAFKA.start();
        REDIS.start();
        MONGO.start();
    }

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        r.add("spring.datasource.username", POSTGRES::getUsername);
        r.add("spring.datasource.password", POSTGRES::getPassword);

        r.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);

        r.add("spring.redis.host", REDIS::getHost);
        r.add("spring.redis.port", () -> REDIS.getMappedPort(6379));

        r.add("spring.data.mongodb.uri", MONGO::getReplicaSetUrl);
    }
}
