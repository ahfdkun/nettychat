package com.ahfdkun.netty.chat.protocol;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 聊天协议
 */
@AllArgsConstructor
@Getter
public enum IMP {
    /**
     * 系统消息
     */
    SYSTEM("SYSTEM"),
    /**
     * 登录指令
     */
    LOGIN("LOGIN"),
    /**
     * 登出指令
     */
    LOGOUT("LOGOUT"),
    /**
     * 聊天消息
     */
    CHAT("CHAT"),
    /**
     * 送鲜花
     */
    FLOWER("FLOWER");

    private String name;

    /**
     * 判断消息是不是自己能识别的协议内容
     */
    public static boolean isIMP(String content) {
        return content.matches("^(\\[(SYSTEM|LOGIN|LOGOUT|CHAT|FLOWER)\\])+\\w*");
    }

}
