/*
 * chenhx
 * Copyright (C) 2013-2022 All Rights Reserved.
 */
package com.datacbc.util;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Objects;

/**
 * @author chenhx
 * @version 0.0.1
 * @className FileUtils.java
 * @date 2022-06-21 03:14
 * @description
 */
public class FileUtils {
    /**
     * 删除文件或者文件夹
     *
     * @param file
     */
    public static void deleteFileOrDirectory(File file) {
        if (null != file) {
            if (!file.exists()) {
                return;
            }
            int i;
            // file 是文件
            if (file.isFile()) {
                boolean result = file.delete();
                // 限制循环次数，避免死循环
                for (i = 0; !result && i++ < 10; result = file.delete()) {
                    // 垃圾回收
                    System.gc();
                }

                return;
            }
            // file 是目录
            File[] files = file.listFiles();
            if (null != files) {
                for (i = 0; i < files.length; ++i) {
                    deleteFileOrDirectory(files[i]);
                }
            }
            file.delete();
        }

    }

    /**
     * 删除文件或者文件夹
     *
     * @param filePath
     */
    public static void deleteFileOrDirectory(String filePath) {
        if (StringUtils.isEmpty(filePath)) {
            return;
        }
        File file = new File(filePath);
        deleteFileOrDirectory(file);
    }


    /**
     * 取得文件夹大小
     * 递归取得文件夹（包括子目录）中所有文件的大小
     *
     * @param f
     * @return
     */
    public static long getFileSize(File f) {
        long size = 0L;
        File[] fliest = f.listFiles();
        if(fliest==null || fliest.length==0){
            return size;
        }
        for (int i = 0; i < Objects.requireNonNull(fliest).length; i++) {
            if (fliest[i].isDirectory()) {
                size = size + getFileSize(fliest[i]);
            } else {
                size = size + fliest[i].length();
            }
        }
        return size;
    }

    /**
     * 转换文件大小
     *
     * @param fileS
     * @return
     */
    public static String formatFileSize(long fileS) {
        if(fileS==0){
            return "0B";
        }
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "K";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "M";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "G";
        }
        return fileSizeString;
    }

    public static void main(String[] args) {
        System.out.println( formatFileSize(getFileSize( new File("/Users/chenhx/Desktop/github/blockchain-data/blockchain/nftweilai"))));
        System.out.println(formatFileSize(getFileSize(new File("/Users/chenhx/Desktop/github/blockchain-data/blockchain/nftweilai/blockchain.db"))));
        System.out.println(formatFileSize(getFileSize(new File("/Users/chenhx/Desktop/github/blockchain-data/blockchain/nftweilai/cache"))));
        System.out.println(formatFileSize(getFileSize(new File("/Users/chenhx/Desktop/github/blockchain-data/blockchain/nftweilai/transaction-no.db"))));
    }

}
