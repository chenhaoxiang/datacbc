/**
 * fshows.com
 * Copyright (C) 2013-2018 All Rights Reserved.
 */
package com.datacbc.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.Arrays;

/**
 * Base58 转化工具
 *
 * @author chenhx
 * @version Base58Check.java, v 0.1 2018-10-16 下午 6:08
 */
@Slf4j
public final class Base58Check {
    /**
     * 去掉了一些容易错的字符
     */
    private static final String ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
    private static final BigInteger ALPHABET_SIZE = BigInteger.valueOf(ALPHABET.length());


    private Base58Check() {
    }  // Not instantiable

    /**
     * 添加校验码并转化为 Base58 字符串
     *
     * @param data
     * @return
     */
    public static String bytesToBase58(byte[] data) {
        return rawBytesToBase58(addCheckHash(data));
    }

    /**
     * 转化为 Base58 字符串
     *
     * @param data
     * @return
     */
    public static String rawBytesToBase58(byte[] data) {
        // Convert to base-58 string
        StringBuilder sb = new StringBuilder();
        BigInteger num = new BigInteger(1, data);
        while (num.signum() != 0) {
            BigInteger[] quotrem = num.divideAndRemainder(ALPHABET_SIZE);
            sb.append(ALPHABET.charAt(quotrem[1].intValue()));
            num = quotrem[0];
        }

        // Add '1' characters for leading 0-value bytes
        for (int i = 0; i < data.length && data[i] == 0; i++) {
            sb.append(ALPHABET.charAt(0));
        }
        return sb.reverse().toString();
    }


    /*---- Class constants ----*/

    /**
     * 添加校验码并返回待有校验码的原生数据
     *
     * @param data
     * @return
     */
    static byte[] addCheckHash(byte[] data) {
        try {
            byte[] hash = Arrays.copyOf(doubleHash(data), 4);
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            buf.write(data);
            buf.write(hash);
            return buf.toByteArray();
        } catch (Exception e) {
            log.error("添加校验码并返回待有校验码的原生数据异常",e);
            throw new AssertionError("添加校验码并返回待有校验码的原生数据异常");
        }
    }

    /**
     * 将 Base58Check 字符串转化为 byte 数组，并校验其校验码
     * 返回的byte数组带有版本号，但不带有校验码
     *
     * @param address
     * @return
     */
    public static byte[] base58ToBytes(String address) {
        if(StringUtils.isEmpty(address)){
            throw new IllegalArgumentException("address is empty ");
        }
        byte[] concat = base58ToRawBytes(address);
        byte[] data = Arrays.copyOf(concat, concat.length - 4);
        byte[] hash = Arrays.copyOfRange(concat, concat.length - 4, concat.length);
        byte[] rehash = Arrays.copyOf(doubleHash(data), 4);
        if (!Arrays.equals(rehash, hash)) {
            throw new IllegalArgumentException("Checksum mismatch");
        }
        return data;
    }


    /*---- Miscellaneous ----*/

    /**
     * 将 Base58Check 字符串反转为 byte 数组
     *
     * @param s
     * @return
     */
    static byte[] base58ToRawBytes(String s) {
        // Parse base-58 string
        BigInteger num = BigInteger.ZERO;
        for (int i = 0; i < s.length(); i++) {
            num = num.multiply(ALPHABET_SIZE);
            int digit = ALPHABET.indexOf(s.charAt(i));
            if (digit == -1) {
                throw new IllegalArgumentException("Invalid character for Base58Check");
            }
            num = num.add(BigInteger.valueOf(digit));
        }
        // Strip possible leading zero due to mandatory sign bit
        byte[] b = num.toByteArray();
        if (b[0] == 0) {
            b = Arrays.copyOfRange(b, 1, b.length);
        }
        try {
            // Convert leading '1' characters to leading 0-value bytes
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            for (int i = 0; i < s.length() && s.charAt(i) == ALPHABET.charAt(0); i++) {
                buf.write(0);
            }
            buf.write(b);
            return buf.toByteArray();
        } catch (Exception e) {
            log.error("将 Base58Check 字符串反转为 byte 数组异常",e);
            throw new AssertionError("将 Base58Check 字符串反转为 byte 数组异常");
        }
    }

    /**
     * 双重Hash
     *
     * @param data
     * @return
     */
    public static byte[] doubleHash(byte[] data) {
        return DigestUtils.sha256(DigestUtils.sha256(data));
    }

}
