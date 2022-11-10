package com.mzt.logserver.shiro;

import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author muzhantong
 * create on 2022/10/21 5:41 PM
 */
@Configuration
public class ShiroConfig {
    @Bean
    public ShiroFilterFactoryBean filterFactoryBean() {
        ShiroFilterFactoryBean factoryBean = new ShiroFilterFactoryBean();
        factoryBean.setSecurityManager(manager());
        factoryBean.setLoginUrl("/login");
        factoryBean.setSuccessUrl("/index");
        factoryBean.setUnauthorizedUrl("/unauthorizedurl");
        Map<String, String> filterMap = new HashMap<>();
        filterMap.put("/doLogin", "anon");
        filterMap.put("/**", "authc");
        factoryBean.setFilterChainDefinitionMap(filterMap);
        return factoryBean;
    }


    @Bean
    public DefaultWebSecurityManager manager() {
        DefaultWebSecurityManager manager = new DefaultWebSecurityManager();
        manager.setRealm(shiroTestRealm());
        return manager;
    }

    @Bean
    public ShiroTestRealm shiroTestRealm() {
        return new ShiroTestRealm();
    }

    @Bean
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator = new DefaultAdvisorAutoProxyCreator();
        defaultAdvisorAutoProxyCreator.setProxyTargetClass(true);
        return defaultAdvisorAutoProxyCreator;
    }

}
