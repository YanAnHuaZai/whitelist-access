package cn.huazai.tool.exception;

/**
 * 非法访问异常
 * @author YanAnHuaZai
 * @date 2023-08-17 21:03:13
 */
public class IllegalAccessException extends RuntimeException {
    private static final long serialVersionUID = -847650985320860067L;

    public IllegalAccessException(String message) {
        super(message);
    }

    public IllegalAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}