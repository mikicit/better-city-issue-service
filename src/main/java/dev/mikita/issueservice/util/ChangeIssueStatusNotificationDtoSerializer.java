package dev.mikita.issueservice.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.mikita.issueservice.dto.ChangeIssueStatusNotificationDto;
import org.apache.kafka.common.serialization.Serializer;

/**
 * The type Change issue status notification dto serializer.
 */
public class ChangeIssueStatusNotificationDtoSerializer implements Serializer<ChangeIssueStatusNotificationDto> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public byte[] serialize(String topic, ChangeIssueStatusNotificationDto data) {
        try {
            return objectMapper.writeValueAsBytes(data);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }
}