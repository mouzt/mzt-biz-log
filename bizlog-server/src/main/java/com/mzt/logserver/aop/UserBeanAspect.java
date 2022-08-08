//package com.mzt.logserver.aop;
//
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.annotation.Around;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.annotation.Pointcut;
//import org.springframework.stereotype.Component;
//
///**
// * @author muzhantong
// * create on 2022/8/7 12:01 AM
// */
//@Aspect
//@Component
//public class UserBeanAspect {
//
//    @Pointcut(value = "execution(* com.mzt.logserver.UserQueryService.*(..))")
//    private void myPointCut() {
//    }
//
//    @Around(value = "myPointCut()")
//    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
//        System.out.println("before");
//        Object obj = joinPoint.proceed();
//        System.out.println("after");
//        return obj;
//
//    }
//}
