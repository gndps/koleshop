package com.koleshop.appkoleshop.common.enums;

/**
 * Created by gundeepsingh on 11/10/14.
 */
public enum MessageType {
    SHOP_SETTINGS, BUYER_SETTINGS, ORDER, PRODUCT;

    public static MessageType getMessageType(String messageTypeString) {
        for (MessageType mt : MessageType.values()) {
            if (mt.name().equalsIgnoreCase(messageTypeString)) {
                return mt;
            }
        }
        return null;
    }
}
