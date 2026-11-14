package com.autota.server.domain.submission.messaging;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

/** 큐는 선언하지 않는다(definitions 파일이 만든다). 기본 exchange에 라우팅 키 = 큐 이름으로 보낸다. */
@Component
@RequiredArgsConstructor
public class JudgePublisher {
    private static final String JUDGE_QUEUE = "judge.queue";

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public void publish(JudgeRequest request) {
        MessageProperties props = new MessageProperties();
        props.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        props.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
        // 워커는 __TypeId__ 헤더가 아니라 AMQP type 속성으로 메시지 종류를 구분한다(contracts)
        props.setType("JudgeRequest");
        props.setMessageId(UUID.randomUUID().toString());
        rabbitTemplate.send(JUDGE_QUEUE, new Message(objectMapper.writeValueAsBytes(request), props));
    }
}
