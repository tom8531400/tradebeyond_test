package com.tradebeyond.backend.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsersBo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long userId;

    private String username;
}
