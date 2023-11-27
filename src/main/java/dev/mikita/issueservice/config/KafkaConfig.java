package dev.mikita.issueservice.config;

import dev.mikita.issueservice.dto.ChangeIssueStatusNotificationDto;
import dev.mikita.issueservice.util.ChangeIssueStatusNotificationDtoDeserializer;
import dev.mikita.issueservice.util.ChangeIssueStatusNotificationDtoSerializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import java.util.HashMap;
import java.util.Map;

/**
 * The type Kafka config.
 */
@EnableKafka
@Configuration
public class KafkaConfig {
    /**
     * Producer factory.
     *
     * @return the producer factory
     */
    @Bean
    public ProducerFactory<String, ChangeIssueStatusNotificationDto> producerFactory() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka-service.kafka.svc.cluster.local:9092");
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ChangeIssueStatusNotificationDtoSerializer.class);
        return new DefaultKafkaProducerFactory<>(configs);
    }

    /**
     * Consumer factory.
     *
     * @return the consumer factory
     */
    @Bean
    public ConsumerFactory<String, ChangeIssueStatusNotificationDto> consumerFactory() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka-service.kafka.svc.cluster.local:9092");
        configs.put(ConsumerConfig.GROUP_ID_CONFIG, "notifications-group");
        configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ChangeIssueStatusNotificationDtoDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(configs);
    }

    /**
     * Kafka template kafka template.
     *
     * @return the kafka template
     */
    @Bean
    public KafkaTemplate<String, ChangeIssueStatusNotificationDto> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }


    /**
     * Kafka listener container factory concurrent kafka listener container factory.
     *
     * @return the concurrent kafka listener container factory
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ChangeIssueStatusNotificationDto> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ChangeIssueStatusNotificationDto> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}
