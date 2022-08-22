/**
 * fshows.com
 * Copyright (C) 2013-2018 All Rights Reserved.
 */
package com.datacbc.util;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.DefaultSerializers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 序列化工具类
 * @author chenhx
 * @version SerializeUtils.java, v 0.1 2018-10-11 下午 7:40
 */
public class SerializeUtils {
    /* Kryo有三组读写对象的方法
     * 1.如果不知道对象的具体类，且对象可以为null： kryo.writeClassAndObject(output, object); Object object = kryo.readClassAndObject(input);
     * 2.如果类已知且对象可以为null： kryo.writeObjectOrNull(output, someObject); SomeClass someObject = kryo.readObjectOrNull(input, SomeClass.class);
     * 3.如果类已知且对象不能为null:  kryo.writeObject(output, someObject); SomeClass someObject = kryo.readObject(input, SomeClass.class);
     */
    static private final ThreadLocal<Kryo> kryoThreadLocal = new ThreadLocal<Kryo>() {
        protected Kryo initialValue() {
            Kryo kryo = new Kryo();
            //支持对象循环引用（否则会栈溢出）
            kryo.setReferences(true); //默认值就是 true，添加此行的目的是为了提醒维护者，不要改变这个配置
            //不强制要求注册类（注册行为无法保证多个 JVM 内同一个类的注册编号相同；而且业务系统中大量的 Class 也难以一一注册）
            kryo.setRegistrationRequired(false); //默认值就是 false，添加此行的目的是为了提醒维护者，不要改变这个配置
            // 在此处配置kryo对象的使用示例，如循环引用等
            kryo.register(CopyOnWriteArraySet.class, 1);
            kryo.register(CopyOnWriteArrayList.class, 2);
            kryo.register(ConcurrentHashMap.class, 3);
//            kryo.register(Block.class, 4);
//            kryo.register(Transaction.class, 5);
//            kryo.register(TXInput.class, 6);
//            kryo.register(TXOutput.class, 7);
            kryo.register(HashMap.class, 9);
            kryo.register(String.class, 10);
            kryo.register(ArrayList.class, 11);
            kryo.register(HashSet.class, 12);
//            kryo.register(AuthInfoResp.class, 13);
//            kryo.register(AuthInfoReq.class, 14);
//            kryo.register(Byte[].class, DefaultArraySerializers.ByteArraySerializer.class,101);
//            kryo.register(Character[].class, DefaultArraySerializers.CharArraySerializer.class);
//            kryo.register(Short[].class, DefaultArraySerializers.ShortArraySerializer.class);
//            kryo.register(Integer[].class, DefaultArraySerializers.IntArraySerializer.class);
//            kryo.register(Long[].class, DefaultArraySerializers.LongArraySerializer.class);
//            kryo.register(Float[].class, DefaultArraySerializers.FloatArraySerializer.class);
//            kryo.register(Double[].class, DefaultArraySerializers.DoubleArraySerializer.class);
//            kryo.register(Boolean[].class, DefaultArraySerializers.BooleanArraySerializer.class);

            kryo.register(Integer.class, new DefaultSerializers.IntSerializer());
            kryo.register(Float.class, new DefaultSerializers.FloatSerializer());
            kryo.register(Boolean.class, new DefaultSerializers.BooleanSerializer());
            kryo.register(Byte.class, new DefaultSerializers.IntSerializer());
            kryo.register(Character.class, new DefaultSerializers.IntSerializer());
            kryo.register(Short.class, new DefaultSerializers.ShortSerializer());
            kryo.register(Long.class, new DefaultSerializers.LongSerializer());
            kryo.register(Double.class, new DefaultSerializers.DoubleSerializer());
            return kryo;
        }
        ;
    };

    /**
     * 反序列化
     *
     * @param bytes 对象对应的字节数组
     * @return
     */
    public static Object deserialize(byte[] bytes) {
        if(bytes==null){
            return null;
        }
        Input input = new Input(bytes);
        Object obj = kryoThreadLocal.get().readClassAndObject(input);
        input.close();
        return obj;
    }

    /**
     * 反序列化 成对象
     * @param bytes 对象对应的字节数组
     * @return
     */
    public static <T> T deserialize(byte[] bytes,Class<T> aClass) {
        if(bytes==null){
            return null;
        }
        Input input = new Input(bytes);
        T obj = kryoThreadLocal.get().readObjectOrNull(input,aClass);
        input.close();
        return obj;
    }

    /**
     * 序列化
     *
     * @param object 需要序列化的对象
     * @return
     */
    public static byte[] serialize(Object object) {
        if(object==null){
            return null;
        }
        Output output = new Output(4096, -1);
        kryoThreadLocal.get().writeClassAndObject(output, object);
        byte[] bytes = output.toBytes();
        output.close();
        return bytes;
    }
    /**
     * 序列化
     *
     * @param object 需要序列化的对象
     * @return
     */
    public static <T> byte[] serialize(Object object,Class<T> tClass) {
        if(object==null){
            return null;
        }
        Output output = new Output(4096, -1);
        kryoThreadLocal.get().writeObjectOrNull(output, object,tClass);
        byte[] bytes = output.toBytes();
        output.close();
        return bytes;
    }

}
