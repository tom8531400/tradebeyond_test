package com.tradebeyond.backend.dto;

import com.tradebeyond.backend.bo.RateBo;
import com.tradebeyond.backend.bo.TaxRateBo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaxRate implements Serializable {
    private static final long serialVersionUID = 1L;

    private String eventId;

    private List<RateBo> rateBos;

    private Integer version;


    public TaxRateBo toBo(){
        TaxRateBo bo = new TaxRateBo();
        bo.setEventId(eventId);
        bo.setRateBos(rateBos);
        bo.setVersion(version);
        return bo;
    }

}
