package cn.huazai.tool.whitelist.access.annotation;

import java.lang.annotation.*;

/**
 * desc: 白名单访问
 * @author YanAnHuaZai
 * @date 2023-08-15 17:20:50
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WhitelistAccess {

    /**
     * 业务键
     * <p>根据该字段值从nacos中获取相关配置</p>
     */
    String businessKey();

    /**
     * 校验的值 spel表达式
     * <p>校验该值是否在白名单中</p>
     */
    String checkValue();

}