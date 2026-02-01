package com.tradebeyond.backend.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradebeyond.backend.bo.OutboxEventBo;
import com.tradebeyond.backend.bo.Payload;
import com.tradebeyond.backend.enums.PayloadEnum;
import com.tradebeyond.backend.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {
    @Autowired
    private ObjectMapper objectMapper;


    @Override
    public void sendOrderCreated(OutboxEventBo outboxEventBo) throws JsonProcessingException {

        Object message = outboxEventBo.getMessage();
        Payload payload = objectMapper.readValue(message.toString(), Payload.class);
        Long userId = payload.getUserId();
        Integer type = payload.getType();

        log.info("模擬發送簡訊 : user:{}, type:{}", userId, PayloadEnum.getDesc(type));


    }
}
