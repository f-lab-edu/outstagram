package com.outstagram.outstagram.kafka.producer;

import com.outstagram.outstagram.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserProducer {

    private final KafkaTemplate<String, UserDTO> userKafkaTemplate;

    public void save(String topic, UserDTO user) {
        log.info("========== SEND USER SAVE user : {}", user);

        userKafkaTemplate.send(topic, user);
    }
}
