package com.tradebeyond.backend.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradebeyond.backend.bo.RateBo;
import com.tradebeyond.backend.bo.TaxRateBo;
import com.tradebeyond.backend.bo.TaxRateSnapshotBo;
import com.tradebeyond.backend.domain.TaxRate;
import com.tradebeyond.backend.enums.RedisEnum;
import com.tradebeyond.backend.enums.StatusCode;
import com.tradebeyond.backend.exception.BusinessException;
import com.tradebeyond.backend.mapper.TaxRateDao;
import com.tradebeyond.backend.mapper.TaxRateSnapshotDao;
import com.tradebeyond.backend.redis.RedisUtils;
import com.tradebeyond.backend.service.TaxRateWebhookService;
import com.tradebeyond.backend.vo.BaseResp;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class TaxRateWebhookServiceImpl implements TaxRateWebhookService {
    @Autowired
    private TaxRateDao taxRateDao;
    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    private TaxRateSnapshotDao taxRateSnapshotDao;
    @Autowired
    private RedissonClient redissonClient;


    @Override
    public BaseResp taxRateWebhook(TaxRateBo taxRate, HttpServletRequest httpServletRequest) throws InterruptedException {
        String eventId = taxRate.getEventId();
        Integer version = taxRate.getVersion();
        List<TaxRate> list = new ArrayList<>();

        // 設置以event_id為維度的分布式鎖 防止重複發送
        final String lockEventIdKey = RedisEnum.LOCK_EVENT_ID_KEY.getKey() + ":" + eventId;
        RLock lock = redissonClient.getLock(lockEventIdKey);
        boolean b = lock.tryLock(2, TimeUnit.SECONDS);
        if (!b) {
            log.warn("event id {} lock fail", eventId);
            throw new BusinessException(StatusCode.LOCK_FAILED.getCode());
        }

        try {
            // 組參數
            List<RateBo> rateBos = taxRate.getRateBos();
            for (RateBo rateBo : rateBos) {
                TaxRate rate = new TaxRate();
                rate.setEventId(eventId);
                rate.setVersion(version);
                rate.setRegion(rateBo.getRegion());
                rate.setRate(rateBo.getRate());
                rate.setCreateTime(new Date());
                list.add(rate);
            }

            // 事務內存入事件版本表 唯一鍵(event_id + version) + 存入稅率表
            transactionTemplate.executeWithoutResult(status -> {
                try {
                    // 事件版本 先判斷是否是最新版本號再存入
                    int insert = taxRateSnapshotDao.insertIfVersionMatch(eventId, version, new Date());
                    if (insert != 1) {
                        log.error("insert :{} send repeatedly", insert);
                        throw new BusinessException(StatusCode.FAIL.getCode());
                    }
                    // 批次insert稅務
                    int updated = taxRateDao.batchInsertTaxRate(list);
                    if (updated == 0) {
                        log.warn("batchInsertTaxRate insert is 0 rows");
                    }
                    log.warn("update :{} is save", updated);
                } catch (Exception e) {
                    log.error("transaction error", e);
                    throw new BusinessException(StatusCode.FAIL.getCode());
                }
            });
        } catch (Exception e) {
            log.error("taxRateWebhook event id {} is fail", eventId, e);
            throw new BusinessException(StatusCode.FAIL.getCode(), e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return BaseResp.success();
    }
}
