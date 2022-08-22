/*
 * chenhx
 * Copyright (C) 2013-2022 All Rights Reserved.
 */
package com.datacbc.util;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

/**
 * @author chenhx
 * @version 0.0.1
 * @className IpUtils.java
 * @date 2022-04-24 6:27 下午
 * @description 获取本机ip地址
 */
@Slf4j
public class IpUtils {

    public static void main(String[] args) {
        System.out.println(getRemoteIp());
    }

    /**
     * IP 地址校验的正则表达式
     */
    private static final Pattern IPV4_PATTERN = Pattern.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    /**
     * 获取 IP 地址的服务列表
     */
    private static final String[] IPV4_SERVICES = {
            "http://checkip.amazonaws.com/",
            "https://v6r.ipip.net/",
            "https://ipv4.icanhazip.com/",
            "http://www.pubyun.com/dyndns/getip",
            "https://ipinfo.io/ip",
            "https://ipecho.net/plain"
//            "http://bot.whatismyipaddress.com/"
            // and so on ...
    };

    public static String getLocalIp() {
        try {
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip = null;
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
                if (netInterface.isLoopback() || netInterface.isVirtual() || !netInterface.isUp()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    ip = addresses.nextElement();
                    if (ip instanceof Inet4Address) {
                        return ip.getHostAddress();
                    }
                }
            }
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            log.error("IP地址获取失败", e);
        }
        return "";
    }

    /**
     * 获取公网ip地址
     *
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static String getRemoteIp(){
        List<Callable<String>> callables = new ArrayList<>();
        for (String ipService : IPV4_SERVICES) {
            callables.add(() -> get(ipService));
        }

        ExecutorService executorService = Executors.newCachedThreadPool();
        try {
            // 返回第一个成功获取的 IP
            return executorService.invokeAny(callables);
        }catch (Exception e){
            log.error("getRemoteIp异常",e);
        }finally {
            executorService.shutdown();
        }
        log.warn("获取远程ip失败，使用本机ip地址");
        return getLocalIp();
    }

    private static String get(String url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        //设置超时时间
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String ip = in.readLine();
            if (IPV4_PATTERN.matcher(ip).matches()) {
                return ip;
            } else {
                throw new IOException("invalid IPv4 address: " + ip);
            }
        }
    }


}
