package com.tradebeyond.backend.domain;

import java.io.Serializable;
import lombok.Data;

/**
 * users
 */
@Data
public class Users implements Serializable {
    private Long userId;

    private String username;

    private static final long serialVersionUID = 1L;
}