package com.example.project_management_app.pipeline;



import com.example.project_management_app.pipeline.kafka.ActivityEventProducer;
import com.example.project_management_app.pipeline.model.UserActivityEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ActivityEventProducerTest {

    @Mock
    private KafkaTemplate<String, UserActivityEvent> kafkaTemplate;

    @InjectMocks
    private ActivityEventProducer activityEventProducer;

    private UserActivityEvent testEvent;

    @BeforeEach
    void setUp() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("priority", "HIGH");

        testEvent = UserActivityEvent.builder()
                .userId(1L)
                .username("Mahmoud")
                .action("TASK_CREATED")
                .entityType("TASK")
                .entityId(100L)
                .metadata(metadata)
                .timestamp(Instant.now())
                .build();
    }

    @Test
    void testSendActivityEvent() {
        activityEventProducer.sendActivityEvent(testEvent);

        verify(kafkaTemplate, times(1)).send(eq("user-activities"), eq("1"), eq(testEvent));
    }

    @Test
    void testSendActivityEventWithNullMetadata() {
        UserActivityEvent eventWithNullMetadata = UserActivityEvent.builder()
                .userId(2L)
                .username("Katya")
                .action("USER_LOGIN")
                .entityType("USER")
                .entityId(2L)
                .metadata(null)
                .timestamp(Instant.now())
                .build();

        activityEventProducer.sendActivityEvent(eventWithNullMetadata);

        verify(kafkaTemplate, times(1)).send(eq("user-activities"), eq("2"), eq(eventWithNullMetadata));
    }

    @Test
    void testSendMultipleEvents() {
        UserActivityEvent event2 = UserActivityEvent.builder()
                .userId(2L)
                .username("Katya")
                .action("TASK_COMPLETED")
                .entityType("TASK")
                .entityId(200L)
                .metadata(null)
                .timestamp(Instant.now())
                .build();

        activityEventProducer.sendActivityEvent(testEvent);
        activityEventProducer.sendActivityEvent(event2);

        verify(kafkaTemplate, times(1)).send(eq("user-activities"), eq("1"), eq(testEvent));
        verify(kafkaTemplate, times(1)).send(eq("user-activities"), eq("2"), eq(event2));
    }

    @Test
    void testSendActivityEventCapturesCorrectTopic() {
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<UserActivityEvent> eventCaptor = ArgumentCaptor.forClass(UserActivityEvent.class);

        activityEventProducer.sendActivityEvent(testEvent);

        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), eventCaptor.capture());

        assertEquals("user-activities", topicCaptor.getValue());
        assertEquals("1", keyCaptor.getValue());
        assertEquals(testEvent, eventCaptor.getValue());
    }
}
