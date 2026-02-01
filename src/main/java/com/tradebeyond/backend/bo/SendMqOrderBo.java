package com.tradebeyond.backend.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendMqOrderBo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String eventType;

    private Integer retryCount;

    private Date time;
}
