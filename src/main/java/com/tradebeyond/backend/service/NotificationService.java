package com.tradebeyond.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tradebeyond.backend.bo.OutboxEventBo;

public interface NotificationService {

    void sendOrderCreated(OutboxEventBo outboxEventBo) throws JsonProcessingException;
}
