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

    private Environment environment;

    @Override
    public void setEnvironment(@NonNull Environment environment) {
        this.environment = environment;
    }

    public Optional<String> get(String configKey) {
        String config = environment.getProperty(configKey);
        return Objects.isNull(config) ? Optional.empty() : Optional.of(config);
    }

}