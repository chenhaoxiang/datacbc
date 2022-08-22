/**
 * fshows.com
 * Copyright (C) 2013-2018 All Rights Reserved.
 */
package com.datacbc.wallet;

import com.datacbc.exception.ServiceException;
import com.datacbc.util.Base58Check;
import com.datacbc.util.ByteUtils;
import com.datacbc.util.ECDSAUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.bouncycastle.crypto.digests.RIPEMD160Digest;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.security.KeyPair;
import java.util.Arrays;

/**
 * 钱包
 *
 * @author chenhx
 * @version Wallet.java, v 0.1 2018-10-16 下午 6:09
 */

@Data
@Slf4j
public class Wallet implements Serializable {

    private static final long serialVersionUID = 9064268200637906140L;
    /**
     * 校验码长度
     */
    private static final int ADDRESS_CHECKSUM_LEN = 4;
    /**
     * 私钥
     */
    private byte[] privateKey;
    /**
     * 公钥
     */
    private byte[] publicKey;


    public Wallet() {
        initWallet();
    }

    /**
     * 初始化钱包
     */
    private void initWallet() {
        try {
            KeyPair keyPair = ECDSAUtils.newECKeyPair();
            BCECPrivateKey privateKey = (BCECPrivateKey) keyPair.getPrivate();
            BCECPublicKey publicKey = (BCECPublicKey) keyPair.getPublic();

            this.setPrivateKey(ECDSAUtils.getBytesByKey(privateKey));
            this.setPublicKey(ECDSAUtils.getBytesByKey(publicKey));

        } catch (Exception e) {
            log.error("Fail to init wallet ! ", e);
            throw new RuntimeException("Fail to init wallet ! ", e);
        }
    }

    /**
     * 通过公钥获取地址
     * @param publicKey publicKey hex 转换
     * @return
     */
    public static String getAddressByPubkey(String publicKey) {
        // 1. 获取 ripemdHashedKey
        byte[] pubkeyHashBytes = Wallet.getPubkeyHashByPubkey(ByteUtils.hexStringToByte(publicKey));
        return getAddressByPubkeyHash(pubkeyHashBytes);
    }
    public static String getAddressByPubkey(byte[] publicKey) {
        // 1. 获得公钥hash
        byte[] pubkeyHashBytes = Wallet.getPubkeyHashByPubkey(publicKey);
        //公钥hash获取地址
        return getAddressByPubkeyHash(pubkeyHashBytes);
    }

    /**
     * 通过公钥hash获取地址
     * @param pubkeyHash 公钥hash
     * @return
     */
    public static String getAddressByPubkeyHash(String pubkeyHash) {
        try {
            return getAddressByPubkeyHash(ByteUtils.hexStringToByte(pubkeyHash));
        } catch (Exception e) {
            log.error("通过公钥获取钱包失败",e);
            throw new ServiceException("错误的公钥地址");
        }
    }
    private static String getAddressByPubkeyHash(byte[] pubkeyHashBytes) {
        try {
            // 2. 添加版本 0x00
            ByteArrayOutputStream addrStream = new ByteArrayOutputStream();
            addrStream.write((byte) 0);
            addrStream.write(pubkeyHashBytes);
            byte[] versionedPayload = addrStream.toByteArray();

            // 3. 计算校验码
            byte[] checksum = Wallet.checksum(versionedPayload);

            // 4. 得到 version + paylod + checksum 的组合
            addrStream.write(checksum);
            byte[] binaryAddress = addrStream.toByteArray();

            // 5. 执行Base58转换处理
            return Base58Check.rawBytesToBase58(binaryAddress);
        } catch (Exception e) {
            log.error("通过公钥获取钱包失败",e);
            throw new ServiceException("错误的公钥地址");
        }
    }

    /**
     * 公钥
     * @return
     */
    public String getPublicKeyStr(){
        return ByteUtils.bytesToHexString(this.publicKey);
    }

    /**
     * 私钥
     * @return
     */
    public String getPrivateKeyStr(){
        return ByteUtils.bytesToHexString(this.privateKey);
    }


    ///比特币的钱包地址生成方式
    /**
     * 双重Hash
     *
     * @param data
     * @return
     */
    public static byte[] doubleHash(byte[] data) {
        return DigestUtils.sha256(DigestUtils.sha256(data));
    }

    /**
     * ripeMD160Hash
     * 计算公钥的 RIPEMD160 Hash值
     * 通过公钥获取到公钥Hash
     * 获取公钥hash
     * @param pubKey 公钥
     * @return ipeMD160Hash(sha256 ( pubkey))
     */
    public static byte[] getPubkeyHashByPubkey(byte[] pubKey) {
        //1. 先对公钥做 sha256 处理
        byte[] shaHashedKey = DigestUtils.sha256(pubKey);
        RIPEMD160Digest ripemd160 = new RIPEMD160Digest();
        ripemd160.update(shaHashedKey, 0, shaHashedKey.length);
        byte[] output = new byte[ripemd160.getDigestSize()];
        ripemd160.doFinal(output, 0);
        return output;
    }

    /**
     * 通过公钥获取到 pubkeyHash
     * 获取公钥hash
     * @param pubKey
     * @return
     */
    public static byte[] getPubkeyHashByPubkey(String pubKey) {
        return Wallet.getPubkeyHashByPubkey(ByteUtils.hexStringToByte(pubKey));
    }

    /**
     * 生成公钥的校验码
     *
     * @param payload
     * @return
     */
    public static byte[] checksum(byte[] payload) {
        return Arrays.copyOfRange(doubleHash(payload), 0, 4);
    }

    /**
     * 通过钱包地址获取到pubkeyHash
     * @param address
     * @return
     */
    public static String getPubKeyHashByAddress(String address) {
        // 反向转化为 byte 数组
        byte[] versionedPayload = Base58Check.base58ToBytes(address);
        byte[] pubKeyHash = Arrays.copyOfRange(versionedPayload, 1, versionedPayload.length);
        return ByteUtils.bytesToHexString(pubKeyHash);
    }

    public static void main(String[] args) {
        //通过公钥获取公钥hash
        String pubkey = "3056301006072a8648ce3d020106052b8104000a0342000492884cbe035353bd0bec99ea7ea2fc7a50cee26be9346df00fc526c8bb4c7310d7b3d1f9551aeb9cc03aa3f17b46c15365abfce5a02214c15d62f77bdb842eff";
        System.out.printf( ByteUtils.bytesToHexString(Wallet.getPubkeyHashByPubkey(pubkey)));

//        Wallet wallet = new Wallet();
//        log.info("生成的钱包公钥字节：{}", Arrays.toString(wallet.getPublicKey()));
//        log.info("生成的钱包私钥字节：{}", Arrays.toString(wallet.getPrivateKey()));
//        log.info("生成的钱包公钥字符串：{}", wallet.getPublicKeyStr());
//        log.info("生成的钱包私钥字符串：{}", wallet.getPrivateKeyStr());
//
//        WalletVO walletVO = new WalletVO();
//        walletVO.setAddress(Wallet.getAddressByPubkey(wallet.getPublicKey()));
//        walletVO.setPrivateKey(wallet.getPrivateKeyStr());
//        walletVO.setPublicKey(wallet.getPublicKeyStr());
//        walletVO.setPublicKeyHash(ByteUtils.bytesToHexString(Wallet.getPubkeyHashByPubkey(wallet.getPublicKey())));
//        log.info("生成的钱包信息：{}", JSON.toJSONString(walletVO));
//
//        log.info("公钥字符串转公钥字节：{}", Arrays.toString(ByteUtils.hexStringToByte(walletVO.getPublicKey())));
//        log.info("私钥字符串转私钥字节：{}", Arrays.toString(ByteUtils.hexStringToByte(walletVO.getPrivateKey())));
//
//        log.info("公钥生成公钥hash字节：{}", Wallet.getPubkeyHashByPubkey(wallet.getPublicKey()));
//        log.info("公钥生成公钥hash字符串：{}", ByteUtils.bytesToHexString(Wallet.getPubkeyHashByPubkey(wallet.getPublicKey())) );
//        log.info("公钥生成钱包地址：{}", Wallet.getAddressByPubkey(wallet.getPublicKey()));
//
//        log.info("钱包地址获取公钥hash：{}", Wallet.getPubKeyHashByAddress( walletVO.getAddress() ));
//        log.info("公钥hash字节：{}", Arrays.toString(ByteUtils.hexStringToByte(walletVO.getPublicKeyHash())) );
//        log.info("公钥hash字符串：{}", walletVO.getPublicKeyHash() );
//        log.info("公钥hash字节获取钱包地址：{}", Wallet.getAddressByPubkeyHash( ByteUtils.hexStringToByte(walletVO.getPublicKeyHash()) ));
//        log.info("公钥hash字符串获取钱包地址：{}", Wallet.getAddressByPubkeyHash( walletVO.getPublicKeyHash() ));

    }

}
