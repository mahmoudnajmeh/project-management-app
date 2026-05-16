package com.example.project_management_app.pipeline.kafka;

import com.example.project_management_app.pipeline.model.UserActivityEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ActivityEventProducer {

    @Autowired
    private KafkaTemplate<String, UserActivityEvent> kafkaTemplate;

    private static final String TOPIC = "user-activities";

    public void sendActivityEvent(UserActivityEvent event) {
        kafkaTemplate.send(TOPIC, event.getUserId().toString(), event);
    }
}