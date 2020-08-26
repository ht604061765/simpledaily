package com.hunter.base.framework;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;
import java.util.UUID;

/**
 * @description 防止表单重复提交拦截器
 * @author Administrator
 * @date 2020/8/25
 **/
@Aspect
@Component
public class DuplicateSubmitAspect {

    private Log logger = LogFactory.getLog(getClass());

    public static final String  DUPLICATE_TOKEN_KEY="DUPLICATE_TOKEN_KEY";

    @Pointcut("@annotation(com.hunter.base.framework.DuplicateSubmitToken)")
    public void pointcut() {
    }

    @Before("pointcut() && @annotation(token)")
    public void before(final JoinPoint joinPoint, DuplicateSubmitToken token) {
        System.out.println("方法执行前拦截");
        if (!Objects.isNull(token)){
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            assert attributes != null;
            HttpServletRequest request = attributes.getRequest();

            boolean isSaveSession = token.save();
            if (isSaveSession){
                String key = getDuplicateTokenKey(joinPoint);
                Object t = request.getSession().getAttribute(key);
                if (null == t){
                    String uuid = UUID.randomUUID().toString();
                    request.getSession().setAttribute(key,uuid);
                    logger.error("token-key="+key);
                    logger.error("token-value="+uuid);
                }else {
                    System.out.println("请求重复请求重复请求重复请求重复请求重复请求重复请求重复请求重复");
                }
            }
        }

    }

    @AfterReturning("pointcut() && @annotation(token)")
    public void afterReturning(final JoinPoint joinPoint, DuplicateSubmitToken token) {
        System.out.println("方法执行后拦截");
        if (token != null){
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attributes.getRequest();
            boolean isSaveSession=token.save();
            if (isSaveSession){
                String key = getDuplicateTokenKey(joinPoint);
                Object t = request.getSession().getAttribute(key);
                if (null != t){
                    request.getSession(false).removeAttribute(key);
                }
            }
        }
    }

//    @AfterThrowing(pointcut = "pointcut()", throwing = "e")
//    public void doAfterThrowing(JoinPoint joinPoint, Throwable e, DuplicateSubmitToken token) {
//        if (null != token){
//            //处理处理重复提交本身之外的异常
//            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
//            HttpServletRequest request = attributes.getRequest();
//            boolean isSaveSession=token.save();
//            //获得方法名称
//            if (isSaveSession){
//                String key = getDuplicateTokenKey(joinPoint);
//                Object t = request.getSession().getAttribute(key);
//                if (null != t){
//                    //方法执行完毕移除请求重复标记
//                    request.getSession(false).removeAttribute(key);
//                    System.out.println("异常情况--移除标记！");
//                }
//            }
//        }
//    }

    /**
     * 获取重复提交key
     * @param joinPoint
     * @return
     **/
    private String getDuplicateTokenKey(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        return DUPLICATE_TOKEN_KEY + "-" + methodName;
    }

}
