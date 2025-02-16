package org.rao.source.genid.exception;

/**
 * 不支持生成ID异常
 * @author Rao
 * @Date 2025/2/8
 **/
public class UnsupportedGenerateIdException extends RuntimeException {
    public UnsupportedGenerateIdException(String message) {
        super(message);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
