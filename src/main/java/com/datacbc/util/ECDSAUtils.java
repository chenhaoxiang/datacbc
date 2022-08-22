/*
 * chenhx
 * Copyright (C) 2013-2022 All Rights Reserved.
 */
package com.datacbc.util;

import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;

import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

/**
 *
 *
 * @author chenhx
 * @version 0.0.1
 * @className ECDSAUtils.java
 * @date 2022-06-19 18:57
 * @description ECDSA 加解密工具类
 */
public class ECDSAUtils {
    public static void main(String[] args) throws Exception {
        //生成一个公私钥
        KeyPair keyPair = ECDSAUtils.newECKeyPair();
        System.out.println( "私钥：" + ByteUtils.bytesToHexString(ECDSAUtils.getBytesByKey(keyPair.getPrivate())));
        System.out.println( "公钥：" + ByteUtils.bytesToHexString(ECDSAUtils.getBytesByKey(keyPair.getPublic())));
        String key = "eUGMXAp68XfbUcYqa82T";
        byte[] keyBytes = SerializeUtils.serialize(key);
        byte[] keyBytes2 = SerializeUtils.serialize("eUGMXAp68XfbUcYqa82T1");
        System.out.println(Arrays.toString(keyBytes));
        System.out.println(Arrays.toString(keyBytes2));
        //加签
        byte[] sign = ECDSAUtils.sign(keyBytes,ECDSAUtils.getBytesByKey(keyPair.getPrivate()));
        System.out.println("校验结果："+ECDSAUtils.verify(keyBytes,ECDSAUtils.getBytesByKey(keyPair.getPublic()),sign) +"，应该的结果：true");
        System.out.println("校验结果："+ ECDSAUtils.verify(keyBytes2,ECDSAUtils.getBytesByKey(keyPair.getPublic()),sign)+"，应该的结果：false");
        String signStr = ByteUtils.bytesToHexString(sign);
        System.out.println("签名："+signStr);
        System.out.println("校验结果："+ECDSAUtils.verify(keyBytes,ECDSAUtils.getBytesByKey(keyPair.getPublic()),ByteUtils.hexStringToByte(signStr)) +"，应该的结果：true");

    }

    /**
     * 数字签名 密钥算法
     */
    private static final String KEY_ALGORITHM = "ECDSA";
    /**
     * 椭圆曲线（EC）域参数设定
     * bitcoin 为什么会选择 secp256k1，详见：https://bitcointalk.org/index.php?topic=151120.0
     */
    private static final String EC_PARAMETER_SPEC = "secp256k1";

    /**
     * 数字签名 签名/验证算法
     *
     * Bouncy Castle支持以下7种算法
     * NONEwithECDSA
     * RIPEMD160withECDSA
     * SHA1withECDSA
     * SHA224withECDSA
     * SHA256withECDSA
     * SHA384withECDSA
     * SHA512withECDSA
     */
    private static final String SIGNATURE_ALGORITHM = "SHA512withECDSA";

    /**
     * 创建新的密钥对
     * @return
     */
    public static KeyPair newECKeyPair() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        // 注册 BC Provider
        Security.addProvider(new BouncyCastleProvider());
        // 创建椭圆曲线算法的密钥对生成器，算法为 ECDSA
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KEY_ALGORITHM, BouncyCastleProvider.PROVIDER_NAME);
        // 椭圆曲线（EC）域参数设定
        ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec(EC_PARAMETER_SPEC);
        keyPairGenerator.initialize(ecSpec, new SecureRandom());
        return keyPairGenerator.generateKeyPair();
    }


    /**
     * 取得密钥 字节
     * @param key
     *            密钥Map
     * @return byte[]
     */
    public static byte[] getBytesByKey(Key key){
        return key.getEncoded();
    }

    /**
     * 通过字节获取私钥
     * @param privateKeyBytes
     * @return
     * @throws Exception
     */
    public static PrivateKey getPrivateKey(byte[] privateKeyBytes)
            throws Exception {
        // 加入BouncyCastleProvider支持
        Security.addProvider(new BouncyCastleProvider());
        // 转换私钥材料
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        // 实例化密钥工厂
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        // 取私钥匙对象
        PrivateKey priKey = keyFactory.generatePrivate(pkcs8KeySpec);
        return priKey;
    }

    /**
     * 通过字节获取公钥
     * @param publicKeyBytes
     * @return
     * @throws Exception
     */
    public static PublicKey getPublicKey(byte[] publicKeyBytes)
            throws Exception {
        // 加入BouncyCastleProvider支持
        Security.addProvider(new BouncyCastleProvider());
        // 转换公钥材料
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        // 实例化密钥工厂
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        // 生成公钥
        PublicKey pubKey = keyFactory.generatePublic(keySpec);
        return pubKey;
    }

    /**
     * 签名
     * @param data
     *            待签名数据
     * @param privateKey
     *            私钥
     * @return byte[] 数字签名
     * @throws Exception
     */
    public static byte[] sign(byte[] data, byte[] privateKey) throws Exception {
        PrivateKey priKey = ECDSAUtils.getPrivateKey(privateKey);
        // 实例化Signature
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM, BouncyCastleProvider.PROVIDER_NAME);
        // 初始化Signature
        signature.initSign(priKey);
        // 更新
        signature.update(data);
        // 签名
        return signature.sign();
    }

    /**
     * 校验 公钥用于验签
     * @param data
     *            待校验数据
     * @param publicKey
     *            公钥
     * @param sign
     *            数字签名
     * @return boolean 校验成功返回true 失败返回false
     * @throws Exception
     *
     */
    public static boolean verify(byte[] data, byte[] publicKey, byte[] sign)
            throws Exception {
        //获取公钥
        PublicKey pubKey = ECDSAUtils.getPublicKey(publicKey);
        // 实例化Signature
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM, BouncyCastleProvider.PROVIDER_NAME);
        // 初始化Signature
        signature.initVerify(pubKey);
        // 更新
        signature.update(data);
        // 验证
        return signature.verify(sign);
    }
    public static boolean verify(String data,String publicKey, String sign)
            throws Exception {
        return ECDSAUtils.verify(ByteUtils.hexStringToByte(data),ByteUtils.hexStringToByte(publicKey),ByteUtils.hexStringToByte(sign));
    }


}
