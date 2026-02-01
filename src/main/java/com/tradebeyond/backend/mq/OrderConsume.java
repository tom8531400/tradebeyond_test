package com.tradebeyond.backend.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.tradebeyond.backend.bo.OutboxEventBo;
import com.tradebeyond.backend.enums.EventTypeEnum;
import com.tradebeyond.backend.enums.OutBoxStatusEnum;
import com.tradebeyond.backend.enums.RetryEnum;
import com.tradebeyond.backend.mapper.OutboxEventDao;
import com.tradebeyond.backend.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class OrderConsume {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private OutboxEventDao outboxEventDao;


    @RabbitListener(queues = "order_queue", concurrency = "3-5")
    public void onPayEvent(Message message, Channel channel) throws IOException {
        MessageProperties messageProperties = message.getMessageProperties();

        long tag = messageProperties.getDeliveryTag();
        String messageId = messageProperties.getMessageId();

        // 獲取重試次數
        Integer count = getDeathCount(message, "order_queue");
        // 判斷最多重試三次
        if (RetryEnum.THREE_TIMES.getCount() < count) {
            // 更新狀態 = 超過重試次數
            outboxEventDao.updateStatusByEventId(OutBoxStatusEnum.DEAD.getCode(), messageId, new Date(), OutBoxStatusEnum.PUBLISHED.getCode());
            // 移除ack
            channel.basicAck(tag, false);
            return;
        }

        OutboxEventBo outboxEventBo = objectMapper.readValue(message.getBody(), OutboxEventBo.class);
        try {
            // 事件更新狀態 防止重送(訂單建立 -> 準備發送)
            int up = outboxEventDao.updateOrderEventStatus(EventTypeEnum.SENDING.getValue(), new Date(), messageId, EventTypeEnum.ORDER_CREATED.getValue());
            if(up != 1){
                // 已有發送 移除ack
                channel.basicAck(tag, false);
                return;
            }
            // 執行發簡訊方法
            notificationService.sendOrderCreated(outboxEventBo);
            // 更新事件(準備發送 -> 發送通知)
            int updated = outboxEventDao.updateOrderEventStatus(EventTypeEnum.NOTIFY_USER.getValue(), new Date(), messageId, EventTypeEnum.SENDING.getValue());
            if (updated != 1) {
                log.warn("update order id:{} event status failed", messageId);
            }
            // 移除ack
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("onPayEvent id:{} error ", outboxEventBo.getEventId(), e);
            // 重試消息
            channel.basicReject(tag, false);
        }
    }

    @RabbitListener(queues = "order_dead_queue")
    public void onOrderDeadEvent(Message message, Channel channel) throws IOException {
        long tag = message.getMessageProperties().getDeliveryTag();
        String messageId = message.getMessageProperties().getMessageId();

        int updated = outboxEventDao.updateOrderEventStatus(EventTypeEnum.FAILED.getValue(), new Date(), messageId, EventTypeEnum.ORDER_CREATED.getValue());
        if (updated != 1) {
            log.warn("onOrderDeadEvent update order id:{} event status failed", messageId);
        }
        channel.basicAck(tag, false);
    }

    private Integer getDeathCount(Message message, String queueName) {
        Map<String, Object> headers = message.getMessageProperties().getHeaders();
        Object obj = headers.get("x-death");

        if (!(obj instanceof List<?>)) {
            return 0;
        }
        List<Map<String, Object>> maps = (List<Map<String, Object>>) obj;

        for (Map<String, Object> map : maps) {
            Object queue = map.get("queue");
            Object reason = map.get("reason");
            if (queueName.equals(queue) && "rejected".equals(reason)) {
                Object count = map.get("count");
                if (count instanceof Number) {
                    return ((Number) count).intValue();
                }
            }
        }
        return 0;
    }
}
