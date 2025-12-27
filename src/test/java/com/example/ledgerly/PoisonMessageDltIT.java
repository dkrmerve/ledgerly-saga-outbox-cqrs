package com.example.ledgerly;

import com.example.ledgerly.infra.kafka.KafkaTopics;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Duration;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public class PoisonMessageDltIT extends AbstractIntegrationTest {

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    @Test
    void poisonMessage_shouldGoToDlt() {
        String topic = KafkaTopics.topicSagaCommands();
        String dltTopic = topic + ".DLT";

        // Missing headers + invalid JSON payload triggers listener failure
        kafkaTemplate.send(topic, "bad-key", "{ this is not valid json ").completable().join();

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, System.getProperty("spring.kafka.bootstrap-servers", ""));
        // fallback: use container directly
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, AbstractIntegrationTest.KAFKA.getBootstrapServers());

        props.put(ConsumerConfig.GROUP_ID_CONFIG, "dlt-test-" + UUID.randomUUID());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        try (KafkaConsumer<String, String> c = new KafkaConsumer<>(props)) {
            c.subscribe(List.of(dltTopic));

            boolean found = false;
            long deadline = System.currentTimeMillis() + 10_000;

            while (System.currentTimeMillis() < deadline && !found) {
                ConsumerRecords<String, String> records = c.poll(Duration.ofMillis(500));
                for (ConsumerRecord<String, String> r : records) {
                    if (r.value() != null && r.value().contains("this is not valid")) {
                        found = true;
                        break;
                    }
                }
            }

            assertThat(found).isTrue();
        }
    }
}
