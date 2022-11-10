package com.mzt.logserver.shiro;

import com.mzt.logapi.starter.annotation.LogRecord;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author muzhantong
 * create on 2022/10/21 5:46 PM
 */
@RestController
@Slf4j
public class ShiroTestController {

    //1. 登录 http://127.0.0.1:8080/doLogin?username=mzt&password=123
    //2. 访问 hello 方法
    @GetMapping("/doLogin")
    public String login(String username, String password) {
        Subject subject = SecurityUtils.getSubject();
        AuthenticationToken token = new UsernamePasswordToken(username, password);
        try {
            subject.login(token);
            return "index";
        } catch (Exception e) {
            log.error("log record", e);
            return "error";
        }
    }

    @GetMapping("/hello")
    @LogRecord(success = "success hello hello", type = "shiro.login", bizNo = "mztt99901091")
    public String hello() {
        return "hello";
    }

    @GetMapping("/login")
    public String login() {
        return "please login!";
    }
}
