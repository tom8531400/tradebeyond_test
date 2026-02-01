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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class TaxRateWebhookServiceImpl implements TaxRateWebhookService {
    @Autowired
    private TaxRateDao taxRateDao;
    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private TaxRateSnapshotDao taxRateSnapshotDao;
    @Autowired
    private ObjectMapper objectMapper;


    @Override
    public BaseResp taxRateWebhook(TaxRateBo taxRate, HttpServletRequest httpServletRequest) throws JsonProcessingException {
        String eventId = taxRate.getEventId();
        Integer version = taxRate.getVersion();
        List<TaxRate> list = new ArrayList<>();

        // 判斷Redis是否存在事件id
        final String cacheEventKey = RedisEnum.CACHE_EVENT_ID_KEY.getKey();
        Boolean aBoolean = redisUtils.isMember(cacheEventKey, eventId);
        if (Boolean.TRUE.equals(aBoolean)) {
            log.warn("eventId :{} send repeatedly", eventId);
            return BaseResp.success();
        }
        // 查詢緩存此版本是否為最新 是就存入
        final String cacheVersionKey = RedisEnum.CACHE_LATEST_VERSION_KEY.getKey();
        Object latestVersion = redisUtils.getLatestVersion(cacheVersionKey);
        if (latestVersion != null) {
            int dbVersion = Integer.parseInt(String.valueOf(latestVersion));
            if (dbVersion >= version) {
                log.warn("version :{} send repeatedly", version);
                return BaseResp.success();
            }
        }

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

        transactionTemplate.executeWithoutResult(status -> {
            try {
                // 存入事件版本表 確保事件唯一性入庫db
                TaxRateSnapshotBo snapshotBo = new TaxRateSnapshotBo();
                snapshotBo.setEventId(eventId);
                snapshotBo.setVersion(version);
                snapshotBo.setCreatedAt(new Date());
                int insert = taxRateSnapshotDao.insert(snapshotBo);
                if (insert != 1) {
                    log.error("insert :{} send repeatedly", insert);
                    throw new BusinessException(StatusCode.FAIL.getCode());
                }

                // 批次執行insert把每個地區跟稅率存進去db
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
        // 存eventId
        redisUtils.addSetValue(cacheEventKey, eventId);

        // 成功後覆蓋redis緩存 string數據
        final String cacheTaxRatKey = RedisEnum.CACHE_TAX_RAT_KEY.getKey();
        String value = objectMapper.writeValueAsString(list);
        redisUtils.setValue(cacheTaxRatKey, value);

        return BaseResp.success();
    }
}
