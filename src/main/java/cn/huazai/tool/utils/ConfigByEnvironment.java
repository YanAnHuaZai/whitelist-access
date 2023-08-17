package cn.huazai.tool.utils;

import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

/**
 * 配置信息
 * <p>获取properties中的配置信息（支持动态获取nacos中的配置信息）</p>
 * @author YanAnHuaZai
 * @date 2023年08月16日17:56:17
 */
@Component
public class ConfigByEnvironment implements EnvironmentAware {

    private ConfigByEnvironment() {
    }

    private static Environment environment;

    @Override
    public void setEnvironment(@NonNull Environment environment) {
        ConfigByEnvironment.environment = environment;
    }

    public static Optional<String> get(String configKey) {
        String config = environment.getProperty(configKey);
        return Objects.isNull(config) ? Optional.empty() : Optional.of(config);
    }

    public static <T> Optional<T> get(String configKey, Class<T> targetType) {
        T config = environment.getProperty(configKey, targetType);
        return Objects.isNull(config) ? Optional.empty() : Optional.of(config);
    }

    public static String getProperty(String key) {
        return environment.getProperty(key);
    }

    public static <T> T getProperty(String key, Class<T> targetType) {
        return environment.getProperty(key, targetType);
    }

}