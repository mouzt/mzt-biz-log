# Springboot-注解-通用操作日志组件

此组件解决的问题是： 「谁」在「什么时间」对「什么」做了「什么事」

> 本组件目前针对 Spring-boot 做了 Autoconfig，如果是 SpringMVC，也可自己在 xml 初始化 bean

## Change Log

|版本 |状态|
|----|----|
| 1.0.1  |发版 |
| 1.0.4  |支持Context添加变量|

## 使用方式

### 基本使用

#### maven依赖添加SDK依赖

```
        <dependency>
          <groupId>io.github.mouzt</groupId>
          <artifactId>bizlog-sdk</artifactId>
          <version>1.0.4</version>
        </dependency>
```
#### SpringBoot入口打开开关,添加 @EnableLogRecord 注解
tenant是代表租户的标识，一般一个服务或者一个业务下的多个服务都写死一个 tenant 就可以
```java
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@EnableTransactionManagement
@EnableLogRecord(tenant = "com.mzt.test")
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
```
#### 日志埋点
###### 1. 普通的记录日志
* pefix：是拼接在 bizNo 上作为 log 的一个标识。避免 bizNo 都为整数 ID 的时候和其他的业务中的 ID 重复。比如订单 ID、用户 ID 等
* bizNo：就是业务的 ID，比如订单ID，我们查询的时候可以根据 bizNo 查询和它相关的操作日志
* success：方法调用成功后把 success 记录在日志的内容中
* SpEL 表达式：其中用双大括号包围起来的（例如：{{#order.purchaseName}}）#order.purchaseName 是 SpEL表达式。Spring中支持的它都支持的。比如调用静态方法，三目表达式。SpEL 可以使用方法中的任何参数
```
  @LogRecordAnnotation(success = "{{#order.purchaseName}}下了一个订单,购买商品「{{#order.productName}}」,下单结果:{{#_ret}}",
              prefix = LogRecordType.ORDER, bizNo = "{{#order.orderNo}}")
  public boolean createOrder(Order order) {
      log.info("【创建订单】orderNo={}", order.getOrderNo());
      // db insert order
      return true;
  }
```
此时会打印操作日志 "张三下了一个订单,购买商品「超值优惠红烧肉套餐」,下单结果:true"
###### 2. 期望记录失败的日志, 如果抛出异常则记录fail的日志，没有抛出记录 success 的日志
```
    @LogRecordAnnotation(
            fail = "创建订单失败，失败原因：「{{#_errorMsg}}」",
            success = "{{#order.purchaseName}}下了一个订单,购买商品「{{#order.productName}}」,下单结果:{{#_ret}}",
            prefix = LogRecordType.ORDER, bizNo = "{{#order.orderNo}}")
    public boolean createOrder(Order order) {
        log.info("【创建订单】orderNo={}", order.getOrderNo());
        // db insert order
        return true;
    }
```
其中的 #_errorMsg 是取的方法抛出异常后的异常的 errorMessage。
###### 3. 日志支持种类
比如一个订单的操作日志，有些操作日志是用户自己操作的，有些操作是系统运营人员做了修改产生的操作日志，我们系统不希望把运营的操作日志暴露给用户看到，
但是运营期望可以看到用户的日志以及运营自己操作的日志，这些操作日志的bizNo都是订单号，所以为了扩展添加了类型字段,主要是为了对日志做分类，查询方便，支持更多的业务。
```
    @LogRecordAnnotation(
            fail = "创建订单失败，失败原因：「{{#_errorMsg}}」",
            category = "MANAGER",
            success = "{{#order.purchaseName}}下了一个订单,购买商品「{{#order.productName}}」,下单结果:{{#_ret}}",
            prefix = LogRecordType.ORDER, bizNo = "{{#order.orderNo}}")
    public boolean createOrder(Order order) {
        log.info("【创建订单】orderNo={}", order.getOrderNo());
        // db insert order
        return true;
    }
```
###### 4. 支持记录操作的详情或者额外信息
如果一个操作修改了很多字段，但是success的日志模版里面防止过长不能把修改详情全部展示出来，这时候需要把修改的详情保存到 detail 字段，
 detail 是一个 String ，需要自己序列化。这里的 #order.toString() 是调用了 Order 的 toString() 方法。
如果保存 JSON，自己重写一下 Order 的 toString() 方法就可以。
```
 @LogRecordAnnotation(
            fail = "创建订单失败，失败原因：「{{#_errorMsg}}」",
            category = "MANAGER_VIEW",
            detail = "{{#order.toString()}}",
            success = "{{#order.purchaseName}}下了一个订单,购买商品「{{#order.productName}}」,下单结果:{{#_ret}}",
            prefix = LogRecordType.ORDER, bizNo = "{{#order.orderNo}}")
    public boolean createOrder(Order order) {
        log.info("【创建订单】orderNo={}", order.getOrderNo());
        // db insert order
        return true;
    }
```
###### 5. 如何指定操作日志的操作人是什么？ 框架提供了两种方法
 * 第一种：手工在LogRecord的注解上指定。这种需要方法参数上有operator
```
    @LogRecordAnnotation(
            fail = "创建订单失败，失败原因：「{{#_errorMsg}}」",
            category = "MANAGER_VIEW",
            detail = "{{#order.toString()}}",
            operator = "{{#currentUser}}",
            success = "{{#order.purchaseName}}下了一个订单,购买商品「{{#order.productName}}」,下单结果:{{#_ret}}",
            prefix = LogRecordType.ORDER, bizNo = "{{#order.orderNo}}")
    public boolean createOrder(Order order, String currentUser) {
        log.info("【创建订单】orderNo={}", order.getOrderNo());
        // db insert order
        return true;
    }
```
这种方法手工指定，需要方法参数上有 operator 参数，或者通过 SpEL 调用静态方法获取当前用户。
 * 第二种： 通过默认实现类来自动的获取操作人，由于在大部分web应用中当前的用户都是保存在一个线程上下文中的，所以每个注解都加一个operator获取操作人显得有些重复劳动，所以提供了一个扩展接口来获取操作人
 框架提供了一个扩展接口，使用框架的业务可以 implements 这个接口自己实现获取当前用户的逻辑，
 对于使用 Springboot 的只需要实现 IOperatorGetService 接口，然后把这个 Service 作为一个单例放到 Spring 的上下文中。使用 Spring Mvc 的就需要自己手工装配这些 bean 了。
```
@Configuration
public class LogRecordConfiguration {

    @Bean
    public IOperatorGetService operatorGetService() {
        return () -> Optional.of(OrgUserUtils.getCurrentUser())
                .map(a -> new OperatorDO(a.getMisId()))
                .orElseThrow(() -> new IllegalArgumentException("user is null"));
    }
}

//也可以这么搞：
@Service
public class DefaultOperatorGetServiceImpl implements IOperatorGetService {

    @Override
    public OperatorDO getUser() {
        OperatorDO operatorDO = new OperatorDO();
        operatorDO.setOperatorId("SYSTEM");
        return operatorDO;
    }
}
```
###### 6. 日志文案调整
对于更新等方法，方法的参数上大部分都是订单ID、或者产品ID等，
比如下面的例子：日志记录的success内容是："更新了订单{{#orderId}},更新内容为...."，这种对于运营或者产品来说难以理解，所以引入了自定义函数的功能。
使用方法是在原来的变量的两个大括号之间加一个函数名称 例如 "{ORDER{#orderId}}" 其中 ORDER 是一个函数名称。只有一个函数名称是不够的,需要添加这个函数的定义和实现。可以看下面例子
自定义的函数需要实现框架里面的IParseFunction的接口，需要实现两个方法：

 * functionName() 方法就返回注解上面的函数名；

 * apply()函数参数是 "{ORDER{#orderId}}"中SpEL解析的#orderId的值，这里是一个数字1223110，接下来只需要在实现的类中把 ID 转换为可读懂的字符串就可以了，
 一般为了方便排查问题需要把名称和ID都展示出来，例如："订单名称（ID）"的形式。

> 这里有个问题：加了自定义函数后，框架怎么能调用到呢？
答：对于Spring boot应用很简单，只需要把它暴露在Spring的上下文中就可以了，可以加上Spring的 @Component 或者 @Service 很方便😄。Spring mvc 应用需要自己装配 Bean。

```
    // 没有使用自定义函数
    @LogRecordAnnotation(success = "更新了订单{{#orderId}},更新内容为....",
            prefix = LogRecordType.ORDER, bizNo = "{{#order.orderNo}}",
            detail = "{{#order.toString()}}")
    public boolean update(Long orderId, Order order) {
        return false;
    }

    //使用了自定义函数，主要是在 {{#orderId}} 的大括号中间加了 functionName
    @LogRecordAnnotation(success = "更新了订单{ORDER{#orderId}},更新内容为...",
            prefix = LogRecordType.ORDER, bizNo = "{{#order.orderNo}}",
            detail = "{{#order.toString()}}")
    public boolean update(Long orderId, Order order) {
        return false;
    }

    // 还需要加上函数的实现
    @Component
    public class OrderParseFunction implements IParseFunction {
        @Resource
        @Lazy //为了避免类加载顺序的问题 最好为Lazy，没有问题也可以不加
        private OrderQueryService orderQueryService;
        
        @Override 
        public String functionName() {
            //  函数名称为 ORDER
            return "ORDER";
        }
    
        @Override
        //这里的 value 可以吧 Order 的JSON对象的传递过来，然后反解析拼接一个定制的操作日志内容
        public String apply(String value) {
            if(StringUtils.isEmpty(value)){
                return value;
            }
            Order order = orderQueryService.queryOrder(Long.parseLong(value));
            //把订单产品名称加上便于理解，加上 ID 便于查问题
            return order.getProductName().concat("(").concat(value).concat(")");
        }
    }
```
###### 7. 日志文案调整 使用 SpEL 三目表达式
```
    @LogRecordAnnotation(prefix = LogRecordTypeConstant.CUSTOM_ATTRIBUTE, bizNo = "{{#businessLineId}}",
            success = "{{#disable ? '停用' : '启用'}}了自定义属性{ATTRIBUTE{#attributeId}}")
    public CustomAttributeVO disableAttribute(Long businessLineId, Long attributeId, boolean disable) {
    	return xxx;
    }
```
###### 8. 日志文案调整 模版中使用方法参数之外的变量&函数中也可以使用Context中变量
可以在方法中通过 LogRecordContext.putVariable(variableName, Object) 的方法添加变量，第一个对象为变量名称，后面为变量的对象，
然后我们就可以使用 SpEL 使用这个变量了，例如：例子中的 {{#innerOrder.productName}} 是在方法中设置的变量，除此之外，在上面提到的自定义函数中也可以使用LogRecordContext中的变量。
（注意：LogRecordContext中变量的生命周期为这个方法，超出这个方法，方法中set到Context的变量就获取不到了）
```
    @Override
    @LogRecordAnnotation(
            success = "{{#order.purchaseName}}下了一个订单,购买商品「{{#order.productName}}」,测试变量「{{#innerOrder.productName}}」,下单结果:{{#_ret}}",
            prefix = LogRecordType.ORDER, bizNo = "{{#order.orderNo}}")
    public boolean createOrder(Order order) {
        log.info("【创建订单】orderNo={}", order.getOrderNo());
        // db insert order
        Order order1 = new Order();
        order1.setProductName("内部变量测试");
        LogRecordContext.putVariable("innerOrder", order1);
        return true;
    }
```

###### 9. 函数中使用LogRecordContext的变量

使用 LogRecordContext.putVariable(variableName, Object) 添加的变量除了可以在注解的 SpEL 表达式上使用，还可以在自定义函数中使用 这种方式比较复杂，下面例子中示意了列表的变化，比如
从[A,B,C] 改到 [B,D] 那么日志显示：「删除了A，增加了D」

```
    @LogRecord(success = "{DIFF_LIST{'文档地址'}}", bizNo = "{{#id}}", prefix = REQUIREMENT)
    public void updateRequirementDocLink(String currentMisId, Long id, List<String> docLinks) {
        RequirementDO requirementDO = getRequirementDOById(id);
        LogRecordContext.putVariable("oldList", requirementDO.getDocLinks());
        LogRecordContext.putVariable("newList", docLinks);

        requirementModule.updateById("docLinks", RequirementUpdateDO.builder()
                .id(id)
                .docLinks(docLinks)
                .updater(currentMisId)
                .updateTime(new Date())
                .build());
    }
    
    
    @Component
    public class DiffListParseFunction implements IParseFunction {
    
        @Override
        public String functionName() {
            return "DIFF_LIST";
        }
    
        @SuppressWarnings("unchecked")
        @Override
        public String apply(String value) {
            if (StringUtils.isBlank(value)) {
                return value;
            }
            List<String> oldList = (List<String>) LogRecordContext.getVariable("oldList");
            List<String> newList = (List<String>) LogRecordContext.getVariable("newList");
            oldList = oldList == null ? Lists.newArrayList() : oldList;
            newList = newList == null ? Lists.newArrayList() : newList;
            Set<String> deletedSets = Sets.difference(Sets.newHashSet(oldList), Sets.newHashSet(newList));
            Set<String> addSets = Sets.difference(Sets.newHashSet(newList), Sets.newHashSet(oldList));
            StringBuilder stringBuilder = new StringBuilder();
            if (CollectionUtils.isNotEmpty(addSets)) {
                stringBuilder.append("新增了 <b>").append(value).append("</b>：");
                for (String item : addSets) {
                    stringBuilder.append(item).append("，");
                }
            }
            if (CollectionUtils.isNotEmpty(deletedSets)) {
                stringBuilder.append("删除了 <b>").append(value).append("</b>：");
                for (String item : deletedSets) {
                    stringBuilder.append(item).append("，");
                }
            }
            return StringUtils.isBlank(stringBuilder) ? null : stringBuilder.substring(0, stringBuilder.length() - 1);
        }
    }
```

#### 框架的扩展点

* 重写OperatorGetServiceImpl通过上下文获取用户的扩展，例子如下

```
@Service
public class DefaultOperatorGetServiceImpl implements IOperatorGetService {

    @Override
    public Operator getUser() {
         return Optional.ofNullable(UserUtils.getUser())
                        .map(a -> new Operator(a.getName(), a.getLogin()))
                        .orElseThrow(()->new IllegalArgumentException("user is null"));
       
    }
}
```
* ILogRecordService 保存/查询日志的例子,使用者可以根据数据量保存到合适的存储介质上，比如保存在数据库/或者ES。自己实现保存和删除就可以了
> 也可以只实现保存的接口，毕竟已经保存在业务的存储上了，查询业务可以自己实现，不走 ILogRecordService 这个接口，毕竟产品经理会提一些千奇百怪的查询需求。
```
@Service
public class DbLogRecordServiceImpl implements ILogRecordService {

    @Resource
    private LogRecordMapper logRecordMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(LogRecord logRecord) {
        log.info("【logRecord】log={}", logRecord);
        LogRecordPO logRecordPO = LogRecordPO.toPo(logRecord);
        logRecordMapper.insert(logRecordPO);
    }

    @Override
    public List<LogRecord> queryLog(String bizKey, Collection<String> types) {
        return Lists.newArrayList();
    }

    @Override
    public PageDO<LogRecord> queryLogByBizNo(String bizNo, Collection<String> types, PageRequestDO pageRequestDO) {
        return logRecordMapper.selectByBizNoAndCategory(bizNo, types, pageRequestDO);
    }
}
```
* IParseFunction 自定义转换函数的接口，可以实现IParseFunction 实现对LogRecord注解中使用的函数扩展
例子：
```
@Component
public class UserParseFunction implements IParseFunction {
    private final Splitter splitter = Splitter.on(",").trimResults();

    @Resource
    @Lazy
    private UserQueryService userQueryService;

    @Override
    public String functionName() {
        return "USER";
    }

    @Override
    // 11,12 返回 11(小明，张三)
    public String apply(String value) {
        if (StringUtils.isEmpty(value)) {
            return value;
        }
        List<String> userIds = Lists.newArrayList(splitter.split(value));
        List<User> misDOList = userQueryService.getUserList(userIds);
        Map<String, User> userMap = StreamUtil.extractMap(misDOList, User::getId);
        StringBuilder stringBuilder = new StringBuilder();
        for (String userId : userIds) {
            stringBuilder.append(userId);
            if (userMap.get(userId) != null) {
                stringBuilder.append("(").append(userMap.get(userId).getUsername()).append(")");
            }
            stringBuilder.append(",");
        }
        return stringBuilder.toString().replaceAll(",$", "");
    }
}
```

#### 变量相关

> LogRecordAnnotation 可以使用的变量出了参数也可以使用返回值#_ret变量，以及异常的错误信息#_errorMsg，也可以通过SpEL的 T 方式调用静态方法噢

#### Change Log & TODO

| 名称 |状态 |
|----|----| 
| 支持Context添加变量|1.0.4 已经支持 | 
|支持对象的diff|TODO| 
| 支持List的日志记录| TODO |

#### 注意点：

⚠️ 整体日志拦截是在方法执行之后记录的，所以对于方法内部修改了方法参数之后，LogRecordAnnotation 的注解上的 SpEL 对变量的取值是修改后的值哦～

## Author

mail : mztsmile@163.com
