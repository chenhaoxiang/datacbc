package com.datacbc.exception;


/**
 * 服务（业务）异常如“ 账号或密码错误 ”，该异常只做INFO级别的日志记录 @see WebMvcConfigurer
 */
public class ServiceException extends RuntimeException {

    private Integer code;

    public ServiceException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public ServiceException(String message) {
        super(message);
        this.code = 0;
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
        this.code = 0;
    }

    public ServiceException(Integer code, String message, Throwable cause) {
        super("状态码:" + code + ",信息:" + message, cause);
    }
}
