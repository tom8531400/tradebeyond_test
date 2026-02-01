package com.tradebeyond.backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tradebeyond.backend.dto.TaxRate;
import com.tradebeyond.backend.resp.Result;
import com.tradebeyond.backend.service.TaxRateWebhookService;
import com.tradebeyond.backend.vo.BaseResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping(value = "/api")
public class TaxRateWebhookController {
    @Autowired
    private TaxRateWebhookService taxRateWebhookService;

    @RequestMapping(value = "webhook/taxRates", method = RequestMethod.POST)
    public Result<BaseResp> taxRateWebhook(@RequestBody TaxRate taxRate, HttpServletRequest httpServletRequest) throws JsonProcessingException {
        log.info("Receive webhook taxRates, taxRate={}", taxRate);

        return Result.success(taxRateWebhookService.taxRateWebhook(taxRate.toBo(), httpServletRequest));
    }
}
