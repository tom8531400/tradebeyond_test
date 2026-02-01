package com.tradebeyond.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tradebeyond.backend.bo.TaxRateBo;
import com.tradebeyond.backend.vo.BaseResp;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface TaxRateWebhookService {


    BaseResp taxRateWebhook(TaxRateBo taxRate, HttpServletRequest httpServletRequest) throws JsonProcessingException;
}
