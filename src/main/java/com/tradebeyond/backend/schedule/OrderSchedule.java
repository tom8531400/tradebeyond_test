package com.tradebeyond.backend.schedule;

import com.tradebeyond.backend.bo.OutboxEventBo;
import com.tradebeyond.backend.bo.SendMqOrderBo;
import com.tradebeyond.backend.config.RabbitMqConfig;
import com.tradebeyond.backend.enums.EventTypeEnum;
import com.tradebeyond.backend.enums.OutBoxStatusEnum;
import com.tradebeyond.backend.enums.RetryEnum;
import com.tradebeyond.backend.mapper.OutboxEventDao;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@Component
public class OrderSchedule {
    @Autowired
    private OutboxEventDao outboxDao;
    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;
    @Autowired
    private RabbitTemplate rabbitTemplateMain;
    @Autowired
    private RabbitTemplate rabbitTemplateDead;


    @Scheduled(fixedDelay = 5000)
    public void scanUnfinishedOrders() {
        SendMqOrderBo bo = new SendMqOrderBo();

        // 構建參數
        bo.setEventType(EventTypeEnum.ORDER_CREATED.getValue());
        bo.setRetryCount(RetryEnum.FOUR_TIMES.getCount());
        bo.setTime(new Date());

        // 撈出可發送的訂單
        List<OutboxEventBo> outboxEventBos = outboxDao.selectRetryEvents(bo);

        if (outboxEventBos != null && !outboxEventBos.isEmpty()) {
            // 訂單批次狀態更新防止重送 狀態 = 已鎖定發送
            outboxDao.batchUpdateStatus(outboxEventBos, OutBoxStatusEnum.IN_FLIGHT.getCode(), new Date());

            // 查出成功鎖到的訂單
            List<OutboxEventBo> eventBos = outboxDao.selectByIdsAndStatus(OutBoxStatusEnum.IN_FLIGHT.getCode(), outboxEventBos);

            // 異步送MQ
            CompletableFuture.runAsync(() -> {
                for (OutboxEventBo outboxBo : eventBos) {
                    String uuid = outboxBo.getEventId();

                    CorrelationData correlationData = new CorrelationData(uuid); // 確認機制冪等id

                    rabbitTemplateMain.convertAndSend(RabbitMqConfig.ORDER_EXCHANGE, RabbitMqConfig.ORDER_ROUTING_KEY,
                            outboxBo, message -> {
                                message.getMessageProperties().setMessageId(uuid); // 消費者冪等id
                                message.getMessageProperties().setCorrelationId(uuid); // 訊息追蹤id
                                return message;
                            }, correlationData);
                }
            }, threadPoolExecutor);
        }
    }

    @Scheduled(fixedDelay = 5000)
    public void scanUnfinishedDeadOrders() {
        // 撈出發送失敗訂單
        SendMqOrderBo bo = new SendMqOrderBo();
        bo.setEventType(EventTypeEnum.ORDER_CREATED.getValue());
        bo.setRetryCount(RetryEnum.THREE_TIMES.getCount());
        bo.setTime(new Date());

        List<OutboxEventBo> outboxEventBos = outboxDao.selectDeadEvents(bo);
        if (outboxEventBos != null && !outboxEventBos.isEmpty()) {
            outboxDao.batchUpdateDeadStatus(outboxEventBos, OutBoxStatusEnum.DEAD_IN_FLIGHT.getCode(), new Date());

            List<OutboxEventBo> eventBos = outboxDao.selectByIdsAndStatus(OutBoxStatusEnum.DEAD_IN_FLIGHT.getCode(), outboxEventBos);

            CompletableFuture.runAsync(() -> {
                for (OutboxEventBo outbox : eventBos) {
                    String uuid = outbox.getEventId();
                    CorrelationData correlationData = new CorrelationData(uuid);

                    rabbitTemplateDead.convertAndSend(RabbitMqConfig.ORDER_DEAD_EXCHANGE,
                            RabbitMqConfig.ORDER_DEAD_ROUTING_KEY, outbox, message -> {
                                message.getMessageProperties().setMessageId(uuid);
                                message.getMessageProperties().setCorrelationId(uuid);
                                return message;
                            }, correlationData);
                }
            }, threadPoolExecutor);
        }
    }
}
