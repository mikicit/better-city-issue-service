package dev.mikita.issueservice.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.mikita.issueservice.dto.ChangeIssueStatusNotificationDto;
import org.apache.kafka.common.serialization.Deserializer;
import java.io.IOException;

/**
 * The type Change issue status notification dto deserializer.
 */
public class ChangeIssueStatusNotificationDtoDeserializer implements Deserializer<ChangeIssueStatusNotificationDto> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ChangeIssueStatusNotificationDto deserialize(String topic, byte[] data) {
        try {
            return objectMapper.readValue(data, ChangeIssueStatusNotificationDto.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
