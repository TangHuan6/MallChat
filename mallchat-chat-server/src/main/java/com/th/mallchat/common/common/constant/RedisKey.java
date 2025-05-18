package com.th.mallchat.common.common.constant;

public class RedisKey {
    public static final String BASE_KEY="mallchat:chat";

    public static final String USER_TOKEN_STRING="userToken:uid_%d";

    public static String getKey(String key,Object... o) {
        return BASE_KEY + String.format(key, o);
    }
}
