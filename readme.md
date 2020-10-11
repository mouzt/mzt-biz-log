# Springboot-注解型-通用操作日志组件
此组件解决的问题是：
「谁」在「什么时间」对「什么」做了「什么事」

>本组件目前针对Spring-boot做了Autoconfig，如果是SpringMVC，也可自己在xml初始化bean

## 使用方式

### 基本使用
* maven依赖添加SDK依赖
```
        <dependency>
            <groupId>io.github.mouzt</groupId>
            <artifactId>bizlog-sdk</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
```
* SpringBoot入口打开开关,添加@EnableLogRecord注解
```java
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@ComponentScan("com.mzt")
@EnableTransactionManagement
@EnableLogRecord(bizLine = "com.mzt.test")
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
```
* 此时在需要添加日志的方法上埋点
```
   @LogRecordAnnotation(success = "{{#order.purchaseName}}下了一个订单,购买商品「{{#order.productName}}」,下单结果:{{#_ret}}",
            bizKey = LogRecordType.ORDER +"{{#order.orderNo}}", bizNo = "{{#order.orderNo}}")
    public boolean createOrder(Order order) {
        log.info("【创建订单】orderNo={}", order.getOrderNo());
        // db insert order
        return true;
    }
```
此时会打印操作日志 "张三下了一个订单,购买商品「超值优惠红烧肉套餐」,下单结果:true"

#### 重写log持久化和获取登陆用户的Service
> 组件有两个Default实现分别是DefaultOperatorGetServiceImpl 和 DefaultLogRecordServiceImpl
> 使用者可以自定义持久化方式自己实现ILogRecordService 的接口来保持log，也可以自己实现IOperatorGetService从自己系统的登陆信息中
> 获取到操作人的ID和名字，也可以在方法上的@LogRecordAnnotation注解上的operator和operatorId属性显示的传递操作者

OperatorGetServiceImpl通过上下文获取用户的例子
```
public class DefaultOperatorGetServiceImpl implements IOperatorGetService {

    @Override
    public Operator getUser() {
         return Optional.ofNullable(UserUtils.getUser())
                        .map(a -> new Operator(a.getName(), a.getLogin()))
                        .orElseThrow(()->new IllegalArgumentException("user is null"));
       
    }
}
```
ILogRecordService 保存数据库的例子,使用者可以根据数据量保存到合适的存储介质上
```java
public class DefaultLogRecordServiceImpl implements ILogRecordService {

    @Resource
    private LogRecordMapper logRecordMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(LogRecord logRecord) {
        log.info("【logRecord】log={}", logRecord);
        logRecordMapper.insertSelective(logRecord);
    }

    @Override
    public List<LogRecord> queryLog(String bizKey) {
        return logRecordMapper.queryByBizKey(bizKey);
    }

    @Override
    public List<LogRecord> queryLogByBizNo(String bizNo) {

        return logRecordMapper.queryByBizNo(bizNo);
    }
}
```
> 查询接口使用者可以根据存储自己实现
#### 变量相关
> 可以使用的变量出了参数也可以使用返回值#_ret变量，以及异常的错误信息#_errorMsg

## Author
misId:   muzhantong   
mail : muzhantong@meituan.com
