package com.project.platform.exception;

import cn.hutool.core.io.IORuntimeException;
import com.project.platform.config.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 处理自定义异常
    @ExceptionHandler(value = CustomException.class)
    @ResponseBody
    public Result customError(CustomException e) {
        log.error("自定义异常：{}", e.getMessage());
        return Result.error(e.getMessage());
    }
    @ExceptionHandler(IORuntimeException.class)
    @ResponseBody
    public Result handleIoException(IORuntimeException e) {
        if (e.getMessage().contains("File not exist")) {
            return Result.error("文件不存在或已删除");
        }
        return Result.error("文件操作异常");
    }
    // 处理 404 接口不存在
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseBody
    public Result notFoundError(NoHandlerFoundException e) {
        log.error("404 接口不存在：{}", e.getMessage());
        return Result.error("404", "接口不存在");
    }

    // 处理所有其他异常（空指针、参数错误等）
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result globalError(Exception e) {
        log.error("系统异常：", e);
        return Result.error("服务器异常");
    }
}