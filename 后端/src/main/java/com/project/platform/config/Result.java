package com.project.platform.config;

/**
 * 统一返回结果封装类
 */
public class Result {

    private String code;
    private String msg;
    private Object data;

    public Result() {
    }

    // 成功
    public static Result success() {
        Result result = new Result();
        result.setCode("200");
        result.setMsg("请求成功");
        return result;
    }

    // 成功 + 数据
    public static Result success(Object data) {
        Result result = success();
        result.setData(data);
        return result;
    }

    // 失败
    public static Result error() {
        Result result = new Result();
        result.setCode("500");
        result.setMsg("错误");
        return result;
    }

    // 失败 + 自定义消息
    public static Result error(String msg) {
        Result result = new Result();
        result.setCode("500");
        result.setMsg(msg);
        return result;
    }

    // 失败 + 状态码 + 消息
    public static Result error(String code, String msg) {
        Result result = new Result();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }

    // getter & setter
    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}