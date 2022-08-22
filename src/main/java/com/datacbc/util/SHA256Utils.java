/*
 * chenhx
 * Copyright (C) 2013-2022 All Rights Reserved.
 */
package com.datacbc.util;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * @author chenhx
 * @version 0.0.1
 * @className SHA256Utils.java
 * @date 2022-06-21 01:19
 * @description
 */
public class SHA256Utils {

    /**
     * 字节流转 sha256
     * @param files
     * @return
     */
    public static String sha256Hex(byte[] files) {
        return DigestUtils.sha256Hex(files);
    }
    public static String sha256Hex(String str) {
        return DigestUtils.sha256Hex(str);
    }
}
