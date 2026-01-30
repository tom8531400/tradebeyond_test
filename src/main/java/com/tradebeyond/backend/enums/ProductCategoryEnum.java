package com.tradebeyond.backend.enums;

import lombok.Getter;

@Getter
public enum ProductCategoryEnum {

    FOOD(1, "foodFactory");

    private final long code;

    private final String category;

    ProductCategoryEnum(long code, String category) {
        this.code = code;
        this.category = category;
    }

    public static String getCategoryByCode(long code) {
        for(ProductCategoryEnum productCategoryEnum : ProductCategoryEnum.values()) {
            if(code == productCategoryEnum.getCode()) {
                return productCategoryEnum.getCategory();
            }
        }
        return null;
    }
}
