package com.mzt.logserver.aop;

import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.service.impl.DiffParseFunction;
import com.mzt.logserver.pojo.User;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * @author muzhantong
 * create on 2022/8/7 12:01 AM
 */
@Aspect
@Component
public class UserBeanAspect {

    @Pointcut(value = "execution(* com.mzt.logserver.impl.UserServiceImpl.testGlobalVariableDiff(..))")
    private void myPointCut() {
    }

    @Before(value = "myPointCut()")
    public void before(JoinPoint joinPoint) throws Throwable {
        User user = new User();
        user.setId(1L);
        user.setName("张三");
        user.setSex("男");
        user.setAge(18);
        User.Address address = new User.Address();
        address.setProvinceName("湖北省");
        address.setCityName("武汉市");
        user.setAddress(address);
        LogRecordContext.putGlobalVariable(DiffParseFunction.OLD_OBJECT, user);
    }
}
