/**
 * fshows.com
 * Copyright (C) 2013-2018 All Rights Reserved.
 */
package com.datacbc.util;

import com.alibaba.fastjson.JSON;
import com.datacbc.exception.ParameterExpression;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.ArrayUtils;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * 字节数组工具类
 * @author chenhx
 * @version ByteUtils.java, v 0.1 2018-10-11 下午 9:17
 */
@Slf4j
public class ByteUtils {

    public static final byte[] EMPTY_ARRAY = new byte[0];

    /**
     * 将多个字节数组合并成一个字节数组
     *
     * @param bytes
     * @return
     */
    public static byte[] merge(byte[]... bytes) {
        Stream<Byte> stream = Stream.of();
        for (byte[] b : bytes) {
            stream = Stream.concat(stream, Arrays.stream(ArrayUtils.toObject(b)));
        }
        return ArrayUtils.toPrimitive(stream.toArray(Byte[]::new));
    }

    /**
     * long 类型转 byte[]
     *
     * @param val
     * @return
     */
    public static byte[] toBytes(long val) {
        return ByteBuffer.allocate(Long.BYTES).putLong(val).array();
    }

    /**
     * int 类型转 byte[]
     *
     * @param val
     * @return
     */
    public static byte[] toBytes(int val) {
        return ByteBuffer.allocate(Integer.BYTES).putInt(val).array();
    }

    /**
     * byte[] 转化为 int
     *
     * @param bytes
     * @return
     */
    public static int toInt(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getInt();
    }

    /**
     * 字节数组转16进制字符串
     *
     * @param src
     * @return
     */
    public static String bytesToHexString(byte[] src) {
        return Hex.encodeHexString(src);
//        if (src == null || src.length <= 0) {
//            return null;
//        }
//        StringBuilder stringBuilder = new StringBuilder("");
//        for (int i = 0; i < src.length; i++) {
//            int v = src[i] & 0xFF;
//            String hex = Integer.toHexString(v);
//            if (hex.length() < 2) {
//                stringBuilder.append(0);
//            }
//            stringBuilder.append(hex);
//        }
//        return stringBuilder.toString();
    }

    /**
     * 16进制字符串转换成字节数组
     *
     * @param hex
     * @return
     */
    public static byte[] hexStringToByte(String hex) {
        if(hex==null){
            return null;
        }
        try {
            return Hex.decodeHex(hex);
        } catch (DecoderException e) {
            log.error("转换失败 hex="+hex,e);
            throw new ParameterExpression("格式错误");
        }
//        if(hex==null){
//            return null;
//        }
//        byte[] b = new byte[hex.length() / 2];
//        int j = 0;
//        for (int i = 0; i < b.length; i++) {
//            char c0 = hex.charAt(j++);
//            char c1 = hex.charAt(j++);
//            b[i] = (byte) ((parse(c0) << 4) | parse(c1));
//        }
//        return b;
    }

    private static int parse(char c) {
        if (c >= 'a') {
            return (c - 'a' + 10) & 0x0f;
        }
        if (c >= 'A') {
            return (c - 'A' + 10) & 0x0f;
        }
        return (c - '0') & 0x0f;
    }

    public static void main(String[] args) {
        String str= "0bca4df3213f467a906f6bb821f71006fb9c4b065f6bf282e1bd4c79b23b5a10";
        System.out.println(JSON.toJSONString(ByteUtils.hexStringToByte(str)));
        try {
            System.out.println(JSON.toJSONString(Hex.decodeHex(str)));
        } catch (DecoderException e) {
            e.printStackTrace();
        }
    }

}
