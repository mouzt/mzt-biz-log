# 动态数据源

此组件解决的问题是： 根据请求动态切换数据源

> 本组件目前使用的mybatis的默认数据源

## sql 
```
CREATE TABLE `t_order` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `product_no` varchar(63) NOT NULL DEFAULT '' COMMENT '产品号',
  `creator` varchar(63) NOT NULL DEFAULT '' COMMENT '创建人',
  `updater` varchar(63) NOT NULL DEFAULT '' COMMENT '更新人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单'
```

## 验证
TestController 的 /test1 走的test1数据源
                  /test2 走的test2数据源


## Change Log

## Author

mail : mztsmile@163.com
