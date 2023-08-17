package cn.huazai.tool.whitelist.access.aspect;

import cn.huazai.tool.whitelist.access.annotation.WhitelistAccess;
import cn.huazai.tool.whitelist.access.exception.IllegalAccessException;
import cn.huazai.tool.whitelist.access.utils.ConfigByEnvironment;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

/**
 * desc: 白名单访问限制切面
 * @author YanAnHuaZai
 * @date 2023-08-15 17:26:21
 */
@Aspect
@Component
public class WhitelistAccessResolveAspect {
    Logger log = LoggerFactory.getLogger(WhitelistAccessResolveAspect.class);

    /** 白名单大开关 */
    private static final String WHITELIST_SWITCH = "whitelist.switch";

    /** 默认分隔字符 */
    private static final String DEFAULT_WHITELIST_SPLIT_KEY = ",";
    /** 默认非白名单异常toast文案 */
    private static final String DEFAULT_WHITELIST_TOAST = "非白名单禁止访问";

    /** 白名单开关 */
    private static final String WHITELIST_KEY_PREFIX = "business.whitelist.switch.";
    /** 白名单校验值 */
    private static final String WHITELIST_VALUE_PREFIX = "business.whitelist.value.";
    /** 白名单校验开始时间key前缀 */
    private static final String WHITELIST_CHECK_BEGIN_AT_PREFIX = "business.whitelist.beginAt.";
    /** 白名单校验结束时间key前缀 */
    private static final String WHITELIST_CHECK_END_AT_PREFIX = "business.whitelist.endAt.";
    /** 白名单弹窗文案 */
    private static final String WHITELIST_TOAST_PREFIX = "business.whitelist.toast.";

    ExpressionParser parser = new SpelExpressionParser();
    LocalVariableTableParameterNameDiscoverer discoverer = new LocalVariableTableParameterNameDiscoverer();

    /**
     * 白名单访问环绕通知
     */
    @Around("@annotation(whitelistAccessPoint)")
    public Object doAround(ProceedingJoinPoint pjp, WhitelistAccess whitelistAccessPoint) throws Throwable {
        if (!Boolean.TRUE.toString().equalsIgnoreCase(ConfigByEnvironment.get(WHITELIST_SWITCH).orElse(null))) {
            // 白名单开关关闭
            return pjp.proceed();
        }

        // 将参数塞入上下文
        Object[] args = pjp.getArgs();
        Method method = ((MethodSignature)pjp.getSignature()).getMethod();
        String[] params = discoverer.getParameterNames(method);
        EvaluationContext context = new StandardEvaluationContext();
        for (int len = 0; len < Objects.requireNonNull(params).length; len++) {
            context.setVariable(params[len], args[len]);
        }

        // 业务键
        String businessKey = whitelistAccessPoint.businessKey();

        // 校验业务开关
        String businessSwitch = ConfigByEnvironment.get(WHITELIST_KEY_PREFIX + businessKey).orElse(null);
        if (!Boolean.TRUE.toString().equalsIgnoreCase(businessSwitch)) {
            // 业务白名单关闭
            log.info("白名单校验关闭 业务键:[{}]", businessKey);
            return pjp.proceed();
        }

        // 判断是否在校验白名单时间内
        if (!checkWhitelistTime(businessKey)) {
            // 非白名单校验时间内
            log.info("白名单校验不在校验时间内 业务键:[{}]", businessKey);
            return pjp.proceed();
        }

        // 解析spel获取校验值
        String checkValueSpel = whitelistAccessPoint.checkValue();
        Expression checkValueExpression = parser.parseExpression(checkValueSpel);
        String checkValue = checkValueExpression.getValue(context, String.class);
        log.info("白名单校验开启 业务键:[{}], 校验值:[{}]", businessKey, checkValue);

        // 校验白名单
        String businessWhitelist = ConfigByEnvironment.get(WHITELIST_VALUE_PREFIX + businessKey).orElse(null);
        if (StringUtils.isEmpty(businessWhitelist) || StringUtils.isEmpty(checkValue) || !checkWhitelist(businessWhitelist, checkValue)) {
            log.info("白名单校验不通过 业务键:[{}], 校验值:[{}]", businessKey, checkValue);
            // 白名单校验不通过
            throw new IllegalAccessException(ConfigByEnvironment.get(WHITELIST_TOAST_PREFIX + businessKey).orElse(DEFAULT_WHITELIST_TOAST));
        }
        log.info("白名单校验通过 业务键:[{}], 校验值:[{}]", businessKey, checkValue);
        return pjp.proceed();
    }

    /**
     * 校验白名单时间
     * 校验通过表示在需要白名单时间范围内；校验不通过表示无需校验白名单；
     * @author YanAnHuaZai
     * @date 2023年08月17日14:58:48
     * @param businessKey 业务键
     * @return true表示需要校验白名单，false表示无需校验白名单(不在白名单校验时间范围内)
     */
    private boolean checkWhitelistTime(String businessKey) {
        // 校验白名单时间，校验通过表示在需要白名单时间范围内，校验不通过表示无需校验白名单
        Long whitelistBeginAt = null;
        Long whitelistEndAt = null;
        String whitelistBeginAtStr = ConfigByEnvironment.get(WHITELIST_CHECK_BEGIN_AT_PREFIX + businessKey).orElse(null);
        if (null != whitelistBeginAtStr) {
            whitelistBeginAt = Long.parseLong(whitelistBeginAtStr);
        }
        String whitelistEndAtStr = ConfigByEnvironment.get(WHITELIST_CHECK_END_AT_PREFIX + businessKey).orElse(null);
        if (null != whitelistEndAtStr) {
            whitelistEndAt = Long.parseLong(whitelistEndAtStr);
        }
        if (null == whitelistBeginAt && null == whitelistEndAt) {
            // 未设置白名单开始、结束时间，表示需要校验白名单
            return true;
        } else if (null != whitelistBeginAt && null != whitelistEndAt) {
            // 白名单开始结束时间均设置
            long current = System.currentTimeMillis();
            // 开始时间小于当前时间 && 结束时间大于当前时间：表示校验通过在需要白名单时间范围内（需要校验白名单）
            return whitelistBeginAt <= current && whitelistEndAt > current;
        } else if (null != whitelistBeginAt) {
            // 仅设置了白名单开始时间
            long current = System.currentTimeMillis();
            // 开始时间小于当前时间：表示校验通过在需要白名单时间范围内（需要白名单校验）
            return whitelistBeginAt <= current;
        } else {
            // 仅设置了白名单结束时间
            long current = System.currentTimeMillis();
            // 开始时间小于当前时间：表示校验通过在需要白名单时间范围内（需要白名单校验）
            return whitelistEndAt > current;
        }
    }

    /**
     * 校验白名单
     * @param businessWhitelist 业务白名单
     * @param checkValue 校验值
     * @return 是否在白名单内
     * @author YanAnHuaZai
     * @date 2023年08月16日18:50:42
     */
    private boolean checkWhitelist(String businessWhitelist, String checkValue) {
        String finalCheckValue = checkValue.trim();
        return Arrays.stream(businessWhitelist.split(DEFAULT_WHITELIST_SPLIT_KEY)).filter(StringUtils::isNotBlank)
                .anyMatch(whitelist -> whitelist.equals(finalCheckValue));
    }

}