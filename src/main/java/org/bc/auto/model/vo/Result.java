package org.bc.auto.model.vo;

import lombok.Data;
import org.bc.auto.constant.ResultCode;
import org.bc.auto.constant.impl.SystemResultCode;

@Data
public class Result<T> {

    private Integer code;
    private String msg;
    private T data;

    public Result() {
    }

    public Result(Integer code) {
        this.code = code;
    }

    public Result(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public Result(ResultCode ResultCode) {
        this(ResultCode.getCode(), ResultCode.getMsg());
    }

    public Result(ResultCode ResultCode, T data) {
        this(ResultCode.getCode(), ResultCode.getMsg(), data);
    }

    public static Result fail() {
        return new Result(SystemResultCode.SYSTEM_ERROR);
    }

    public static Result fail(ResultCode errorType) {
        return new Result(errorType);
    }

    public static <T> Result<T> fail(ResultCode errorType, T data) {
        return new Result<>(errorType, data);
    }

    public static Result success() {
        return new Result(SystemResultCode.SYSTEM_SUCCESS);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(SystemResultCode.SYSTEM_SUCCESS, data);
    }

    public static Result status(ResultCode ResultCode) {
        return new Result(ResultCode);
    }


    public void setResultCode(ResultCode ResultCode) {
        this.code = ResultCode.getCode();
        this.msg = ResultCode.getMsg();
    }


}
