/*
 * souche.com
 * Copyright (C) 2013-2019 All Rights Reserved.
 */
package com.datacbc.exception;


/**
 * @author chenhx
 * @version ParameterExpression.java, v 0.1 2019-12-08 23:42 chenhx
 */
public class DevelopmentExpression extends RuntimeException {

    private Integer code;

    public Integer getCode() {
        return code;
    }

    public DevelopmentExpression(Integer code, String message) {
        super(message);
        this.code = code;
    }
    public DevelopmentExpression(String message) {
        super(message);
        this.code = 400;
    }

    public DevelopmentExpression(Integer code, String message, Throwable cause) {
        super("状态码:" + code + ",信息:" + message, cause);
    }
}
