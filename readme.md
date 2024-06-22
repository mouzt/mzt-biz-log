# Springboot-注解-通用操作日志组件

此组件解决的问题是： 「谁」在「什么时间」对「什么」做了「什么事」

## Star History
[![Star History Chart](https://api.star-history.com/svg?repos=mouzt/mzt-biz-log&type=Date)](https://star-history.com/#mouzt/mzt-biz-log&Date)

> 本组件目前针对 Spring-boot 做了 Autoconfig，如果是 SpringMVC，也可自己在 xml 初始化 bean

## Change Log

2.0.0版本修改了一些变量名称，而且做的使向下不兼容的修改，如果大家不想改，可以一直使用1.x的版本，后续还会迭代的， 如果第一次接入推荐大家使用最新版本 3.X ～～ 1.x 文档: ./doc/document-1.x.md

修改点：

1. 把注解 @LogRecordAnnotation 修改为了@LogRecord
2. 把注解 @LogRecordAnnotation 的prefix 修改为type字段
3. 把注解 @LogRecordAnnotation 的category修改为subType字段
4. 把注解 @LogRecordAnnotation 的detail修改为extra字段
5. 把LogRecord实体的字段prefix、category、detail修改为 type、subtype、extra
6. 实现了默认的 server 端，采用的使用数据库存储

### 最近主要修改 (！！创建了技术交流群，微信二维码在在文章末尾，欢迎大家加入一起探讨技术！！！)


| 版本             | 状态                                                                                                                                                       |
|----------------|----------------------------------------------------------------------------------------------------------------------------------------------------------|
| 3.0.7-SNAPSHOT | 1. 修复diff内容相同在某些情况下，依然记录日志的问题 https://github.com/mouzt/mzt-biz-log/issues/153   2. 其他优化 3. 单测优化                                                          |
| 3.0.6          | 提供global变量清除方法，用户需要根据实际自己清除global变量                                                                                                                      |
| 3.0.5          | 1.修复参数全局传递未获取到值 2.diffLog未生效 3.优化文案比对格式,支持重载 toString 和 equals 方法                                                                                        |
| 3.0.4          | 1.修复fix:修复LocalDateTime diff (#111, #114), 2. 固定文案判断错误                                                                                                   |
| 3.0.3          | 1.修复日志打印两次的问题 2.方法支持多注解(#98) 3.相同对象diff不记录日志 详细使用方式见 IOrderServiceTest                                                                                   |
| 3.0.2          | 1.修复 DIffLogIgnore注解在集合类型上失效问题 2.支持跨方法的全局变量 3. 支持日志记录异常与业务逻辑一起回滚的逻辑，默认日志记录不影响业务逻辑                                                                        |
| 3.0.1          | diff 功能支持了数组(https://github.com/mouzt/mzt-biz-log/issues/75) ，增加判断是否成功的条件表达式，增加 @DiffLogAllFields、@DIffLogIgnore 注解支持                                    |
| 3.0.0          | 暂时删除了list实现优化中,增加了xml的方式,增加了性能监控接口,修复了function 内的 service 需要添加 @Lazy 的问题                                                                                 || 2.0.2 | 1.修复了 LogFunctionParser 的NPE，2. 注解上添加了ElementType.TYPE，3.记录了当前执行方法的Class和Method 4. 重新fix了没有加EnableTransactionManagement 切面不生效的逻辑 5. 增加了 Subtype 的 SpEl解析 |
| 2.0.2          | 1.修复了 LogFunctionParser 的NPE，2. 注解上添加了ElementType.TYPE，3.记录了当前执行方法的Class和Method 4. 重新fix了没有加EnableTransactionManagement 切面不生效的逻辑 5. 增加了 Subtype 的 SpEl解析 |
| 2.0.1          | 修复了接口上的注解不能被拦截的问题                                                                                                                                        |
| 2.0.0          | 1.修改了@LogRecordAnnotation 注解的名字 到LogRecord                                                                                                               |
| 1.1.1          | 1. 修复了自定义函数返回美元符号解析失败问题，2. 修复before自定义函数bug，3.删除了diff最后一个分隔符                                                                                             |
| 1.1.0          | 1. 支持了对象DIFF，release 稳定下再发版 2.Function 的参数从 String修改为 Object了，可以给自定函数传递对象啦~~ 3. fix了没有加EnableTransactionManagement 切面不生效的逻辑 4. 添加了fail标志，代表是否成功          |
| 1.0.8          | 自定义函数支持 在业务的方法运行前执行                                                                                                                                      |
| 1.0.5          | 支持 condition；修复https://github.com/mouzt/mzt-biz-log/issues/18                                                                                            |
| 1.0.4          | 支持 Context 添加变量                                                                                                                                          |
| 1.0.1          | 发版                                                                                                                                                       |

## 使用方式(对象DIFF功能终于支持了)

### 基本使用

#### maven依赖添加SDK依赖

```
        <dependency>
          <groupId>io.github.mouzt</groupId>
          <artifactId>bizlog-sdk</artifactId>
          <version>3.0.7-SNAPSHOT</version>
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

* type：是拼接在 bizNo 上作为 log 的一个标识。避免 bizNo 都为整数 ID 的时候和其他的业务中的 ID 重复。比如订单 ID、用户 ID 等，type可以是订单或者用户
* bizNo：就是业务的 ID，比如订单ID，我们查询的时候可以根据 bizNo 查询和它相关的操作日志
* success：方法调用成功后把 success 记录在日志的内容中
* SpEL 表达式：其中用双大括号包围起来的（例如：{{#order.purchaseName}}）#order.purchaseName 是 SpEL表达式。Spring中支持的它都支持的。比如调用静态方法，三目表达式。SpEL 可以使用方法中的任何参数

```
    @LogRecord(
            success = "{{#order.purchaseName}}下了一个订单,购买商品「{{#order.productName}}」,测试变量「{{#innerOrder.productName}}」,下单结果:{{#_ret}}",
            type = LogRecordType.ORDER, bizNo = "{{#order.orderNo}}")
    public boolean createOrder(Order order) {
        log.info("【创建订单】orderNo={}", order.getOrderNo());
        // db insert order
        Order order1 = new Order();
        order1.setProductName("内部变量测试");
        LogRecordContext.putVariable("innerOrder", order1);
        return true;
    }
```

此时会打印操作日志 "张三下了一个订单,购买商品「超值优惠红烧肉套餐」,测试变量「内部变量测试」,下单结果:true"

###### 2. 期望记录失败的日志, 如果抛出异常则记录fail的日志，没有抛出记录 success 的日志。从 1.1.0-SNAPSHOT 版本开始，在LogRecord实体中添加了 fail 标志，可以通过这个标志区分方法是否执行成功了

```
    @LogRecord(
            fail = "创建订单失败，失败原因：「{{#_errorMsg}}」",
            success = "{{#order.purchaseName}}下了一个订单,购买商品「{{#order.productName}}」,测试变量「{{#innerOrder.productName}}」,下单结果:{{#_ret}}",
            type = LogRecordType.ORDER, bizNo = "{{#order.orderNo}}")
    public boolean createOrder(Order order) {
        log.info("【创建订单】orderNo={}", order.getOrderNo());
        // db insert order
        Order order1 = new Order();
        order1.setProductName("内部变量测试");
        LogRecordContext.putVariable("innerOrder", order1);
        return true;
    }
```
其中的 #_errorMsg 是取的方法抛出异常后的异常的 errorMessage。

###### 3. 日志支持子类型

比如一个订单的操作日志，有些操作日志是用户自己操作的，有些操作是系统运营人员做了修改产生的操作日志，我们系统不希望把运营的操作日志暴露给用户看到，
但是运营期望可以看到用户的日志以及运营自己操作的日志，这些操作日志的bizNo都是订单号，所以为了扩展添加了子类型字段,主要是为了对日志做分类，查询方便，支持更多的业务。

```
    @LogRecord(
            subType = "MANAGER_VIEW",
            success = "{{#order.purchaseName}}下了一个订单,购买商品「{{#order.productName}}」,测试变量「{{#innerOrder.productName}}」,下单结果:{{#_ret}}",
            type = LogRecordType.ORDER, bizNo = "{{#order.orderNo}}")
    public boolean createOrder(Order order) {
        log.info("【创建订单】orderNo={}", order.getOrderNo());
        // db insert order
        Order order1 = new Order();
        order1.setProductName("内部变量测试");
        LogRecordContext.putVariable("innerOrder", order1);
        return true;
    }
```
###### 4. 支持记录操作的详情或者额外信息

如果一个操作修改了很多字段，但是success的日志模版里面防止过长不能把修改详情全部展示出来，这时候需要把修改的详情保存到 extra 字段， extra 是一个 String ，需要自己序列化。这里的 #order.toString()
是调用了 Order 的 toString() 方法。 如果保存 JSON，自己重写一下 Order 的 toString() 方法就可以。

```
    @LogRecord(
            extra = "{{#order.toString()}}",
            success = "{{#order.purchaseName}}下了一个订单,购买商品「{{#order.productName}}」,测试变量「{{#innerOrder.productName}}」,下单结果:{{#_ret}}",
            type = LogRecordType.ORDER, bizNo = "{{#order.orderNo}}")
    public boolean createOrder(Order order) {
        log.info("【创建订单】orderNo={}", order.getOrderNo());
        // db insert order
        Order order1 = new Order();
        order1.setProductName("内部变量测试");
        LogRecordContext.putVariable("innerOrder", order1);
        return true;
    }
```
###### 5. 如何指定操作日志的操作人是什么？ 框架提供了两种方法

* 第一种：手工在LogRecord的注解上指定。这种需要方法参数上有operator
```
    @LogRecord(
            operator = "{{#currentUser}}",
            success = "{{#order.purchaseName}}下了一个订单,购买商品「{{#order.productName}}」,下单结果:{{#_ret}}",
            type = LogRecordType.ORDER, bizNo = "{{#order.orderNo}}")
    public boolean createOrder(Order order, String currentUser) {
        log.info("【创建订单】orderNo={}", order.getOrderNo());
        // db insert order
        return true;
    }
```
这种方法手工指定，需要方法参数上有 operator 参数，或者通过 SpEL 调用静态方法获取当前用户。

* 第二种： 通过默认实现类来自动的获取操作人，由于在大部分web应用中当前的用户都是保存在一个线程上下文中的，所以每个注解都加一个operator获取操作人显得有些重复劳动，所以提供了一个扩展接口来获取操作人
  框架提供了一个扩展接口，使用框架的业务可以 implements 这个接口自己实现获取当前用户的逻辑， 对于使用 Springboot 的只需要实现 IOperatorGetService 接口，然后把这个 Service
  作为一个单例放到 Spring 的上下文中。使用 Spring Mvc 的就需要自己手工装配这些 bean 了。
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

* executeBefore() true：这个函数解析在注解方法执行之前运行，false：方法执行之后。有些更新方法，需要在更新之前查询出数据，这时候可以吧executeBefore返回true，
  executeBefore为true的时候函数内不能使用_ret和errorMsg的内置变量

* apply()函数参数是 "{ORDER{#orderId}}"中SpEL解析的#orderId的值，这里是一个数字1223110，接下来只需要在实现的类中把 ID 转换为可读懂的字符串就可以了，
  一般为了方便排查问题需要把名称和ID都展示出来，例如："订单名称（ID）"的形式。

> 这里有个问题：加了自定义函数后，框架怎么能调用到呢？ 答：对于Spring boot应用很简单，只需要把它暴露在Spring的上下文中就可以了，可以加上Spring的 @Component 或者 @Service 很方便😄。Spring mvc 应用需要自己装配 Bean。

> ！！！自定义函数 的参数 从 1.1.0 开始，从String 更改为了Object，老版本需要修改一下定义

```
    // 没有使用自定义函数
    @LogRecord(success = "更新了订单{{#orderId}},更新内容为....",
            type = LogRecordType.ORDER, bizNo = "{{#order.orderNo}}",
            extra = "{{#order.toString()}}")
    public boolean update(Long orderId, Order order) {
        return false;
    }

    //使用了自定义函数，主要是在 {{#orderId}} 的大括号中间加了 functionName
    @LogRecord(success = "更新了订单{ORDER{#orderId}},更新内容为...",
            type = LogRecordType.ORDER, bizNo = "{{#order.orderNo}}",
            extra = "{{#order.toString()}}")
    public boolean update(Long orderId, Order order) {
        return false;
    }

    // 还需要加上函数的实现
    @Slf4j
    @Component
    public class OrderParseFunction implements IParseFunction {
    
        @Override
        public boolean executeBefore() {
            return true;
        }
    
        @Override
        public String functionName() {
            return "ORDER";
        }
    
        @Override
        public String apply(Object value) {
            log.info("@@@@@@@@");
            if (StringUtils.isEmpty(value)) {
                return "";
            }
            log.info("###########,{}", value);
            Order order = new Order();
            order.setProductName("xxxx");
            return order.getProductName().concat("(").concat(value.toString()).concat(")");
        }
    }
```
###### 7. 日志文案调整 使用 SpEL 三目表达式

```
    @LogRecord(type = LogRecordTypeConstant.CUSTOM_ATTRIBUTE, bizNo = "{{#businessLineId}}",
            success = "{{#disable ? '停用' : '启用'}}了自定义属性{ATTRIBUTE{#attributeId}}")
    public CustomAttributeVO disableAttribute(Long businessLineId, Long attributeId, boolean disable) {
    	return xxx;
    }
```

###### 8. 日志文案调整 模版中使用方法参数之外的变量&函数中也可以使用Context中变量

可以在方法中通过 LogRecordContext.putVariable(variableName, Object) 的方法添加变量，第一个对象为变量名称，后面为变量的对象， 然后我们就可以使用 SpEL 使用这个变量了，例如：例子中的
{{#innerOrder.productName}} 是在方法中设置的变量，除此之外，在上面提到的自定义函数中也可以使用LogRecordContext中的变量。
~~（注意：LogRecordContext中变量的生命周期为这个方法，超出这个方法，方法中set到Context的变量就获取不到了）~~

若想跨方法使用，可通过LogRecordContext.putGlobalVariable(variableName, Object) 放入上下文中，此优先级为最低，若方法上下文中存在相同的变量，则会覆盖

```
    @Override
    @LogRecord(
            success = "{{#order.purchaseName}}下了一个订单,购买商品「{{#order.productName}}」,测试变量「{{#innerOrder.productName}}」,下单结果:{{#_ret}}",
            type = LogRecordType.ORDER, bizNo = "{{#order.orderNo}}")
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
    @LogRecord(success = "{DIFF_LIST{'文档地址'}}", bizNo = "{{#id}}", type = REQUIREMENT)
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

###### 10. 使用 condition，满足条件的时候才记录日志

比如下面的例子：condition 变量为空的情况 才记录日志；condition 中的 SpEL 表达式必须是 bool 类型才生效。不配置 condition 默认日志都记录

```
    @LogRecord(success = "更新了订单ORDER{#orderId}},更新内容为...",
            type = LogRecordType.ORDER, bizNo = "{{#order.orderNo}}",
            extra = "{{#order.toString()}}", condition = "{{#condition == null}}")
    public boolean testCondition(Long orderId, Order order, String condition) {
        return false;
    }
```

###### 11. 使用对象 diff 功能

我们经常会遇到下面这样的情况，一个对象，一下更新了好几个字段，然后传入到方法中，这时候操作日志要记录的是：对象中所有字段的值 具体例子如下： Order对象里面包含了 List 类型的 Field，以及自定义对象 UserDO。这里使用了
@DiffLogField注解，可以指定中文的名字，还可以指定 field 值的function函数，这个函数就是第9点提到的函数， 也就是函数不仅仅在方法注解上可以使用，还可以在@DiffLogField上使用。
使用方式是：在注解上使用 __DIFF 函数，这个函数可以生成一行文本，
__DIFF有重载的两种使用方式:
下面的例子。__DIFF 函数传递了两个参数，一个是修改之前的对象，一个是修改之后的对象

```
@LogRecord(success = "更新了订单{_DIFF{#oldOrder, #newOrder}}",
            type = LogRecordType.ORDER, bizNo = "{{#newOrder.orderNo}}",
            extra = "{{#newOrder.toString()}}")
    public boolean diff(Order oldOrder, Order newOrder) {

        return false;
    }
```

下面的例子。__DIFF 函数传递了一个参数，传递的参数是修改之后的对象，这种方式需要在方法内部向 LogRecordContext 中 put 一个变量，代表是之前的对象，这个对象可以是null

```
@LogRecord(success = "更新了订单{_DIFF{#newOrder}}",
            type = LogRecordType.ORDER, bizNo = "{{#newOrder.orderNo}}",
            extra = "{{#newOrder.toString()}}")
    @Override
    public boolean diff1(Order newOrder) {

        LogRecordContext.putVariable(DiffParseFunction.OLD_OBJECT, null);
        return false;
    }
```

下面给出了需要DIFF的对象的例子，需要在参与DIFF的对象上添加上 @DiffLogField 注解，name：是生成的 DIFF 文案中 Field 的中文， function： 跟前面提到的
function一样，例如可以把用户ID映射成用户姓名。

```
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @DiffLogField(name = "订单ID", function = "ORDER")
    private Long orderId;
    @DiffLogField(name = "订单号")
    private String orderNo;
    @DiffLogField(name = "创建时间")
    private Date createTime;

    @DiffLogField(name = "创建人")
    private UserDO creator;
    @DiffLogField(name = "更新人")
    private UserDO updater;
    @DiffLogField(name = "列表项", function = "ORDER")
    private List<String> items;

    @Data
    public static class UserDO {
        @DiffLogField(name = "用户ID")
        private Long userId;
        @DiffLogField(name = "用户姓名")
        private String userName;
    }
}
```

看下源码中的 test 示例：

```
    @Test
    public void testDiff1() {
        Order order = new Order();
        order.setOrderId(99L);
        order.setOrderNo("MT0000011");
        order.setProductName("超值优惠红烧肉套餐");
        order.setPurchaseName("张三");
        Order.UserDO userDO = new Order.UserDO();
        userDO.setUserId(9001L);
        userDO.setUserName("用户1");
        order.setCreator(userDO);
        order.setItems(Lists.newArrayList("123", "bbb"));


        Order order1 = new Order();
        order1.setOrderId(88L);
        order1.setOrderNo("MT0000099");
        order1.setProductName("麻辣烫套餐");
        order1.setPurchaseName("赵四");
        Order.UserDO userDO1 = new Order.UserDO();
        userDO1.setUserId(9002L);
        userDO1.setUserName("用户2");
        order1.setCreator(userDO1);
        order1.setItems(Lists.newArrayList("123", "aaa"));
        orderService.diff(order, order1);

        List<LogRecord> logRecordList = logRecordService.queryLog("xxx");
        Assert.assertEquals(1, logRecordList.size());
        LogRecord logRecord = logRecordList.get(0);
        Assert.assertEquals(logRecord.getAction(), "更新了订单【创建人的用户ID】从【9001】修改为【9002】；【创建人的用户姓名】从【用户1】修改为【用户2】；【列表项】添加了【xxxx(aaa)】删除了【xxxx(bbb)】；【订单ID】从【xxxx(99)】修改为【xxxx(88)】；【订单号】从【MT0000011】修改为【MT0000099】；");
        Assert.assertNotNull(logRecord.getExtra());
        Assert.assertEquals(logRecord.getOperator(), "111");
        Assert.assertEquals(logRecord.getBizNo(), order1.getOrderNo());
        logRecordService.clean();
    }
    
```

最后打印的日志内容：

```
更新了订单【创建人的用户ID】从【9001】修改为【9002】；【创建人的用户姓名】从【用户1】修改为【用户2】；【列表项】添加了【xxxx(aaa)】删除了【xxxx(bbb)】；【订单ID】从【xxxx(99)】修改为【xxxx(88)】；【订单号】从【MT0000011】修改为【MT0000099】；
```

如果用户需要记录的对象字段过多不想每个字段都增加 @DiffLogField 注解，框架还提供了 @DiffLogAllFields 注解，默认就使用属性名来做日志记录，也提供了 @DIffLogIgnore 注解来忽略字段。

这时对象可以有如下写法：

```java
@Data
@DiffLogAllFields
public class User {

    private Long id;
    /**
     * 姓名
     */
    private String name;

    /**
     * 年龄
     */
    @DIffLogIgnore
    private Integer age;

    /**
     * 性别
     */
    @DiffLogField(name = "性别", function = "SEX")
    private String sex;

    /**
     * 用户地址
     */
    private Address address;

    @Data
    public static class Address {
        /**
         * 省名称
         */
        private String provinceName;

        /**
         * 市名称
         */
        private String cityName;

        /**
         * 区/县名称
         */
        private String areaName;
    }
}
```

源码中的 test 示例：

```
    @Test
    @Sql(scripts = "/sql/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void diffUser() {
        User user = new User();
        user.setId(1L);
        user.setName("张三");
        user.setSex("男");
        user.setAge(18);
        User.Address address = new User.Address();
        address.setProvinceName("湖北省");
        address.setCityName("武汉市");
        user.setAddress(address);

        User newUser = new User();
        newUser.setId(1L);
        newUser.setName("李四");
        newUser.setSex("女");
        newUser.setAge(20);
        User.Address newAddress = new User.Address();
        newAddress.setProvinceName("湖南省");
        newAddress.setCityName("长沙市");
        newUser.setAddress(newAddress);
        userService.diffUser(user, newUser);

        List<LogRecord> logRecordList = logRecordService.queryLog(String.valueOf(user.getId()), LogRecordType.USER);
        Assert.assertEquals(1, logRecordList.size());
        LogRecord logRecord = logRecordList.get(0);
        Assert.assertEquals(logRecord.getAction(), "更新了用户信息【address的cityName】从【武汉市】修改为【长沙市】；【address的provinceName】从【湖北省】修改为【湖南省】；【name】从【张三】修改为【李四】；【性别】从【男333】修改为【女333】");
        Assert.assertNotNull(logRecord.getExtra());
        Assert.assertEquals(logRecord.getOperator(), "111");
        Assert.assertEquals(logRecord.getId(), user.getId());
        logRecordService.clean();
    }
```

最后打印的日志内容：

```
更新了用户信息【address的cityName】从【武汉市】修改为【长沙市】；【address的provinceName】从【湖北省】修改为【湖南省】；【name】从【张三】修改为【李四】；【性别】从【男333】修改为【女333】
```

如果用户不想使用这样的文案怎么办呢？ 可以在配置文件中配置：其中__fieldName是：字段名称的替换变量，其他内置替换变量可以看 LogRecordProperties 的源码注释

```
mzt:
  log:
    record:
      updateTemplate: __fieldName 从 __sourceValue 修改为 __targetValue
      ### 加了配置，name更新的模板就是 "用户姓名 从 张三 变为 李四" 其中的 __fieldName 、 __sourceValue以及__targetValue 都是替换的变量
```

###### 12. 增加了操作日志 Monitor 监控接口

用户可以自己实现 ILogRecordPerformanceMonitor 接口，实现对日志性能的监控。默认是 DefaultLogRecordPerformanceMonitor 需要开启 debug 才能打印日志

```
//开启debug方法：
logging:
  level:
    com.mzt.logapi.service.impl: debug


//日志打印例子：
---------------------------------------------
ns         %     Task name
---------------------------------------------
000111278  003%  before-execute
003277960  097%  after-execute
```

###### 13.记录成功日志的条件

默认逻辑：被注解的方法不抛出异常会记录 success 的日志内容，抛出异常会记录 fail 的日志内容， 当指定了 successCondition 后 successCondition 表达式为true的时候才会记录
success内容，否则记录 fail 内容

```
    @LogRecord(success = "更新成功了订单{ORDER{#orderId}},更新内容为...",
            fail = "更新失败了订单{ORDER{#orderId}},更新内容为...",
            type = LogRecordType.ORDER, bizNo = "{{#order.orderNo}}",
            successCondition = "{{#result.code == 200}}")
    public Result<Boolean> testResultOnSuccess(Long orderId, Order order) {
        Result<Boolean> result = new Result<>(200, "成功", true);
        LogRecordContext.putVariable("result", result);
        return result;
    }
```

###### 14.日志记录与业务逻辑一起回滚

默认日志记录错误不影响业务的流程，若希望日志记录过程如果出现异常，让业务逻辑也一起回滚，在 @EnableLogRecord 中 joinTransaction 属性设置为 true，
另外 @EnableTransactionManagement order 属性设置为0 (让事务的优先级在@EnableLogRecord之前)
```
@EnableLogRecord(tenant = "com.mzt.test", joinTransaction = true)
@EnableTransactionManagement(order = 0)
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
```

###### 15.方法记录多条日志

若希望一个方法记录多条日志，在方法上重复写两个注解即可，前提是两个注解**不相同**
```
    @LogRecord(
            subType = "MANAGER_VIEW", extra = "{{#order.toString()}}",
            success = "{{#order.purchaseName}}下了一个订单,购买商品「{{#order.productName}}」,下单结果:{{#_ret}}",
            type = LogRecordType.ORDER, bizNo = "{{#order.orderNo}}")
    @LogRecord(
            subType = "USER_VIEW",
            success = "{{#order.purchaseName}}下了一个订单,购买商品「{{#order.productName}}」,下单结果:{{#_ret}}",
            type = LogRecordType.USER, bizNo = "{{#order.orderNo}}")
    public boolean createOrders(Order order) {
        log.info("【创建订单】orderNo={}", order.getOrderNo());
        return true;
    }
```

###### 16.用对象的`equals`和`toString`
框架给到用户的比对结果可能不符合用户预期，在此框架提供重载比对方法。
如在`LocalDate`比对中，默认输出结果为：
> 【localDate的dayOfMonth】从【1】修改为【4】；【localDate的dayOfWeek】从【WEDNESDAY】修改为【SATURDAY】；【localDate的dayOfYear】从【32】修改为【35】

在配置文件中加入，`mzt.log.record.useEqualsMethod`，**需要填入类的全路径，多个类用英文逗号分割**
```
mzt:
  log:
    record:
      useEqualsMethod: java.time.LocalDate,java.time.Instant
```
重载后的比对结果为：
>【localDate】从【2023-02-24】修改为【-999999999-01-01】


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
* IParseFunction 自定义转换函数的接口，可以实现IParseFunction 实现对LogRecord注解中使用的函数扩展 例子：
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
* IDiffItemsToLogContentService 用户可以自己实现这个接口实现 对象的diff功能，只需要继承这个接口加上 @Service 然后放在 Spring 容器中就可以覆盖默认的实现了

#### 变量相关

> LogRecord 可以使用的变量出了参数也可以使用返回值 #_ret 变量，以及异常的错误信息 #_errorMsg，也可以通过 SpEL 的 T 方式调用静态方法噢


#### 注意点：

⚠️ 整体日志拦截是在方法执行之后记录的，所以对于方法内部修改了方法参数之后，LogRecord 的注解上的 SpEL 对变量的取值是修改后的值哦～

#### 常见问题：
- 为什么有的类注解生效了，有的类注解未生效？
> 此问题和bean的生命周期相关，确定未生效的类是否被提前初始化，即在`BeanFactoryLogRecordAdvisor`之前已经被加载

- 为何没记录日志？
> 1. 默认比对对象无变动时不记录日志，可通过配置文件`mzt.log.record.diffLog`修改，默认为false，无变动时不记录日志
> 2. 当`mzt.log.record.diffLog=false`时，且文案中包含#，对象比对后未发生改变，会跳过日志

- 如何提问？
> 提问前请确定已经阅读上面使用文档，一个好的问题请参考：[提問的智慧](https://github.com/ryanhanwu/How-To-Ask-Questions-The-Smart-Way)


## Author

mail : mztsmile@163.com

## 加微信我们一起讨论技术吧~~，一起进步(*❦ω❦)！！！

我的微信：
![联系我](https://img-blog.csdnimg.cn/a6803dde49e94de396340179c33f8d79.jpeg#pic_center)

群微信(如果群图片过期，加我上面👆🏻微信，拉您进群)

![联系我](https://img-blog.csdnimg.cn/cc53cbc32e81479abc872bdf86ee5601.jpeg#pic_center)

