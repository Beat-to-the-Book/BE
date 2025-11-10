package org.be.decoration.model;

import lombok.Getter;

@Getter
public enum DecorationType {
    TYPE1(1, 10),
    FIGURE(2, 20),
    STAR(3, 10);

    private final int code;
    private final int price;

    DecorationType(int code, int price) {
        this.code = code;
        this.price = price;
    }

    public static DecorationType fromCode(Integer code) {
        if (code == null) return null;
        for (var t : values()) {
            if (t.code == code) return t;
        }
        return null;
    }
}