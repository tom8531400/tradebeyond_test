package com.tradebeyond.backend.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaseResp implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean isSuccess;

    private Integer errorCode;

    public static BaseResp success(){
        return new BaseResp(true, null);
    }

}
